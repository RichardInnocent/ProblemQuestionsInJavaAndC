/*
 ============================================================================
 Name        : CWK2Q5.c
 Author      : Anonymous (DO NOT CHANGE)
 Description :
 Implement an algorithm in C which given a file containing a block of text as
 input, redacts all words from a given set of “redactable” words (also from a
 file), and outputs the result to a file called “result.txt”. For example,
 given the block of text:
    The quick brown fox jumps over the lazy dog

 and the redactable set of words:
    the, jumps, lazy

 the output text in “result.txt” should be
    *** quick brown fox ***** over *** **** dog

 Note that the number of stars in the redacted text is the same as the number
 of letters in the word that has been redacted, and that capitalization is
 ignored. You should not use any of the string libraries to answer this
 question. You should also test your program using the example files
 provided.
 ============================================================================
*/

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

/**
 * Type used to store an array of strings.
 */
typedef struct StringArray
{
  /**
   * The array of strings.
   */
  char** array;

  /**
   * The number of items in the array.
   */
  size_t size;
} StringArray;

static int print_with_redactions(char*, StringArray*, FILE*);
static bool get_redacted_words(FILE*, StringArray*);
static void sort_by_length_desc(char**, size_t);
static size_t read_line(FILE*, size_t, char**);
static void redact_all(char*, const char**, size_t);
static void redact(char*, const char*);
static void redact_chars(char*, size_t, size_t);
static int matches_whole_word_at_index(const char*, const char*, size_t);
static int matches_ignore_case(char, char);
static size_t string_length(const char*);
static int is_alphabetic(char);
static int is_word_separator(const char*, size_t);
static char to_lower_case(char);

void redact_words(const char *text_filename, const char *redact_words_filename)
{
  // Open the file containing the redacted words (in read mode)
  FILE *redaction_file = fopen(redact_words_filename, "r");

  // If the file can't be found, print an error and exit
  if (!redaction_file)
  {
    fprintf(
        stderr,
        "Could not find text file with redacted words at file path: %s",
        redact_words_filename
    );
    return;
  }

  // Get the redacted words
  StringArray redacted_words;
  if (!get_redacted_words(redaction_file, &redacted_words))
    fprintf(stderr, "Redaction failed\n");

  // Close the path to the redacted words as this is no longer required
  fclose(redaction_file);

  // Open the file containing the text (in read mode)
  FILE *text_file = fopen(text_filename, "r");

  // If the file can't be found, print an error and exit
  if (!text_file)
  {
    fprintf(
        stderr, "Could not find text file to apply redaction to at file path: %s", text_filename
    );
    return;
  }

  // Open the file that will contain the result (in write mode)
  char *result_filename = "./result.txt";
  FILE *result_file = fopen(result_filename, "w");

  // Make sure that we can create a file here. If not, print an error and exit
  if (!result_file)
  {
    fprintf(stderr, "Could not open or create result file at file path: %s", result_filename);
    fclose(text_file);
    return;
  }

  // Iterate through the input file line by line. For each line, apply the redactions and print it
  // to the result file. This alleviates the need to read the entire passage into memory.
  size_t buffer_size = 64;

  // The line may be resized, so should be created on the heap
  char *line = calloc(buffer_size, sizeof(char));
  if (!line)
  {
    fprintf(stderr, "Could not allocate space for input line\n");
    return;
  }

  // Read each line in the file and add process it
  while ((buffer_size = read_line(text_file, buffer_size, &line)) != 0)
  {
    // Attempt to write the result to the results file. If not successful, terminate
    if (print_with_redactions(line, &redacted_words, result_file) < 0)
    {
      fprintf(stderr, "An error occurred writing to the file at %s. Terminating.", result_filename);
      break;
    }
  }

  // Free up resources
  free(line);
  line = NULL;

  for (int i = 0; i < redacted_words.size; i++)
    free(redacted_words.array[i]);

  fclose(text_file);
  fclose(result_file);
}

/**
 * Writes the text out, where the redactions are applied.
 * @param text The text to write.
 * @param redacted_words The words/phrases to be redacted from <code>text</code>.
 * @param output The file to write the results to.
 * @return The number of characters written, or a negative number if the write operation was not
 * successful.
 */
static int print_with_redactions(char *text, StringArray *redacted_words, FILE *output)
{
  for (size_t i = 0; i < redacted_words->size; i++)
    redact_all(text, (const char **) redacted_words->array, redacted_words->size);
  return fprintf(output, "%s\n", text) < 0;
}

/**
 * Gets the words to redact, reading them line by line from the given file.
 * @param file The file to read from.
 * @param result The memory location where the redacted words will be stored.
 * @return The array of words to redact.
 */
static bool get_redacted_words(FILE *file, StringArray* result)
{
  // Start with an array size of 8 - if we receive more words, double the size
  size_t number_of_words = 0;
  size_t current_size = 8;

  // Create space for the redacted words
  char **redacted_words = malloc(current_size * sizeof(char*));
  if (!redacted_words) {
    fprintf(stderr, "Could not create space for redacted words\n");
    return false;
  }

  // Read the lines one by one until the end of the file is reached
  size_t buffer_size = 32;
  char *line = calloc(buffer_size, sizeof(char));
  if (!line)
  {
    fprintf(stderr, "Could not create space for redacted words\n");
    return false;
  }

  while (read_line(file, buffer_size, &line) != 0)
  {
    // Check if the buffer needs to be resized
    if (number_of_words >= current_size)
    {
      // Double the size of the buffer
      current_size *= 2;
      redacted_words = realloc(redacted_words, current_size * sizeof(char*));
      if (!redacted_words) {
        fprintf(stderr, "Could not create space for redacted words\n");
        return false;
      }
    }

    // Add the redacted word to the buffer
    redacted_words[number_of_words] = line;
    number_of_words++;

    line = calloc(buffer_size, sizeof(char));
    if (!line)
    {
      fprintf(stderr, "Could not create space for redacted words\n");
      return false;
    }
  }

  free(line); // Last line is an empty buffer and contains no content so can be freed

  // Sort the redacted words by their length so that the longest words appear first. This ensures
  // that the words are always redacted according to this hierarchy, so that the inclusion of the
  // redacted word "shop" would not prevent the successful redaction of the word "shopping".
  sort_by_length_desc(redacted_words, number_of_words);

  // Create the result array object and return it
  result->array = redacted_words;
  result->size = number_of_words;

  return true;
}

/**
 * Sorts an array of strings in descending order of length. Implementation inspired by
 * <a href="https://www.geeksforgeeks.org/insertion-sort/">this article</a>, accessed 15/05/2021.
 * @param char_array The array to sort.
 * @param number_of_elements The number of items in the array.
 */
static void sort_by_length_desc(char **char_array, const size_t number_of_elements)
{
  int current_index, compared_index;
  char *key;

  // Iterate over the array
  for (current_index = 1; current_index < number_of_elements; current_index++)
  {
    key = char_array[current_index];

    // Start by comparing the string with the string before the current position
    compared_index = current_index - 1;

    // Compare the string with all of the strings before it, from the back to the front.
    // Stop when we get to the start of the array, or the compared string has a greater length than
    // this one
    while (compared_index >= 0 && string_length(char_array[compared_index]) < string_length(key))
    {
      // The compared string is shorter than the current string, so it needs to be shifted over to
      // the right
      char_array[compared_index + 1] = char_array[compared_index];
      compared_index--;
    }

    // Insert the current value in its correct position
    char_array[compared_index + 1] = key;
  }
}

/**
 * <p>Reads the next line from the file. Specifically, this reads from the first character up until
 * (but not including) the first line break or <code>EOF</code>.</p>
 * <p>This function uses a resizable buffer to store the line. Initially, the line will be attempted
 * to be stored in a line size equal to <code>initial_buffer_size</code>. If this is not possible,
 * the size of the buffer will be doubled continually until it fits the line.</p>
 * @param file The file to read from.
 * @param initial_buffer_size The initial size of the buffer.
 * @param line A pointer to where the line will be stored.
 * @return The number of character in the buffer size for <code>line</code>, or <code>0</code> if
 * the line is empty and the end of the file has been reached.
 */
size_t read_line(FILE *file, const size_t initial_buffer_size, char **line)
{

  // Get the first character
  int character = fgetc(file);

  // If the first character is an EOF, then there's no line here so indicate that no content was
  // read
  if (character == EOF) {
    return 0;
  }

  // Create the initial character array buffer
  size_t buffer_size = initial_buffer_size;

  // Continue reading from the file, character by character, until a line break or EOF is
  // encountered
  size_t index = 0;
  do
  {
    // Check if the buffer needs to be resized to accommodate the new character
    if (index >= buffer_size-1) // Account for the NULL terminator
    {
      // Reached the size of the buffer so we need to resize. Double the size of the buffer
      buffer_size *= 2;
      *line = realloc(*line, buffer_size * sizeof(char));

      if (!*line)
      {
        fprintf(stderr, "Could not allocate memory for line\n");
        exit(1);
      }
    }

    // Add the newly read character to the buffer
    (*line)[index] = (char) character;

    // Read the next character, ready for the next iteration
    character = fgetc(file);
    index++;
  } while (character != EOF && character != '\n');

  // Add the null terminator to the char array and return it
  (*line)[index] = '\0';

  return buffer_size;
}

/**
 * <p>Redacts whole-word occurrences of the redacted words from text. If no instances of any of the
 * redacted words are found, <code>text</code> will be unmodified.</p>
 * <p>To reiterate, this function only redacts words based on a whole word match of the redacted
 * words, where a word is defined (for simplicity) as any substring that is between the start of a
 * string, the end of a string, non-alphabetic characters, or any combination thereof.</p>
 * <p>The rationale for this is that redaction filters may filter out parts of larger words that
 * actually have little to do with the occurrence detected. For example, consider a filter that aims
 * to anonymise text by removing references to person names. Even if "Tom" is included in the
 * filter, we would not expect the word "stomach" to be partly redacted.</p>
 * @param text The text that should be modified with the redactions, if appropriate.
 * @param redacted_words The words that should be redacted from <code>text</code>.
 * @param num_redacted_words The number of redacted words in <code>redacted_words</code>.
 */
static void redact_all(char *text, const char **redacted_words, const size_t num_redacted_words)
{
  for (size_t i = 0; i < num_redacted_words; i++)
    redact(text, redacted_words[i]);
}

/**
 * <p>Redacts whole-word occurrences of <code>redacted_word</code> from text. If no instances of
 * <code>redacted_word</code> are found, <code>text</code> will be unmodified.</p>
 * <p>To reiterate, this function only redacts words based on a whole word match of<code>
 * redacted_word</code>, where a word is defined (for simplicity) as any substring that is between
 * the start of a string, the end of a string, non-alphabetic characters, or any combination
 * thereof.</p>
 * <p>The rationale for this is that redaction filters may filter out parts of larger words that
 * actually have little to do with the occurrence detected. For example, consider a filter that aims
 * to anonymise text by removing references to person names. Even if "Tom" is included in the
 * filter, we would not expect the word "stomach" to be partly redacted.</p>
 * @param text The text that should be modified with the redaction, if appropriate.
 * @param redacted_word The word to redact, if present.
 */
static void redact(char *text, const char *redacted_word)
{
  // Only set this if we have a match - this is a micro-optimisation, assuming that most text won't
  // contain instances of the redacted word
  size_t redacted_word_length = -1;

  size_t text_length = string_length(text);
  size_t text_index = 0;

  // Iterate through the text
  while (text_index < text_length)
  {
    // Check if the redacted word matches the text at the current index
    if (matches_whole_word_at_index(redacted_word, text, text_index))
    {
      // Match found! If this is the first time we're coming across a match, we need to calculate
      // the length of the the redacted word
      if (redacted_word_length == -1)
        redacted_word_length = string_length(redacted_word);

      // Apply the redaction
      redact_chars(text, text_index, redacted_word_length);

      // We're not going to get a match again until at least the end of the string, so skip a few
      // characters up to that point
      text_index += redacted_word_length;
    } else
    {
      // Didn't find a match at this position, so move onto the next one
      text_index++;
    }
  }
}

/**
 * Replaces characters in the text with an asterisk.
 * @param text The text to modify.
 * @param start_index The index to start modifying from.
 * @param redacted_chars The number of characters that should be redacted.
 */
static void redact_chars(char *text, const size_t start_index, const size_t redacted_chars)
{
  for (int i = 0; i < redacted_chars; i++)
    text[i + start_index] = '*';
}

/**
 * Checks if the word is matched in the body of text at the given index. Note that this performs
 * <em>whole word searching</em>, such that a match is only detected if the searched word forms a
 * whole word at that index. For simplicity, a "whole word" is defined as any substring that is
 * between the start of a string, the end of a string, non-alphabetic characters, or any combination
 * thereof.
 * @param word The word to search for.
 * @param text The text to search in.
 * @param start_index The index in <code>text</code> to check.
 * @return <code>1</code> if a match is detected, or <code>0</code> if not.
 */
static int matches_whole_word_at_index(const char *word, const char *text, size_t start_index)
{
  size_t word_index = 0;
  char word_char;

  // Is the character before a word separator?
  if (!is_word_separator(text, start_index-1))
  {
    return 0;
  }

  while ((word_char = word[word_index]) != '\0')
  {
    if (!matches_ignore_case(word_char, text[word_index + start_index]))
      return 0;
    word_index++;
  }

  // We've matched the string, but we also need to check that the character after is a word
  // separator
  return is_word_separator(text, start_index + word_index);
}

static int is_alphabetic(const char character)
{
  char lower_case_char = to_lower_case(character);
  return lower_case_char >= 'a' && lower_case_char <= 'z';
}

/**
 * Gets the length of a string.
 * @param string The string to size.
 * @return The length of the string.
 */
static size_t string_length(const char* string)
{
  size_t index = 0;
  while (string[index] != '\0')
    index++;
  return index;
}

/**
 * Performs a case-insensitive match between the two given characters.
 * @param char1 The first character to compare.
 * @param char2 The second character to compare.
 * @return <code>1</code> if the characters match, or <code>0</code> if they don't.
 */
static int matches_ignore_case(char char1, char char2)
{
  return char1 == char2 ? 1 : to_lower_case(char1) == to_lower_case(char2);
}

/**
 * Converts the character to lowercase. Non-ASCII or non-alphabetic characters will not be affected.
 * @param char1 The character to convert to lowercase.
 * @return The character in lowercase.
 */
static char to_lower_case(const char char1)
{
  return (char) (char1 >= 'A' && char1 <= 'Z' ? char1 - ('A' - 'a') : char1);
}

/**
 * Checks if the character at the given index is classified as a word separator. For simplicity,
 * text is only considered a "word" if it surrounded on both sides by non-alphabetic characters or
 * the start/end of a string.
 * @param text The text.
 * @param index The index of the character to check.
 * @return <code>1</code> if the character is classified as a word separator, or <code>0</code> if
 * not.
 */
static int is_word_separator(const char *text, const size_t index)
{
  return index < 0 || !(is_alphabetic(text[index]));
}

int main(int argc, char *argv[]) {
  const char *input_file = "./debate.txt";
  const char *redact_file = "./redact.txt";
  redact_words(input_file, redact_file);
  return EXIT_SUCCESS;
}
