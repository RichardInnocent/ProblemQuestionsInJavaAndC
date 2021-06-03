/*
 ============================================================================
 Name        : CWK2Q7.c
 Author      : Anonymous (DO NOT CHANGE)
 Description :
 Implement a Columnar Transposition Cipher in C to encrypt a message of any
 length. A Columnar Transposition Cipher is transposition cipher that follows
 a simple rule for mixing up the characters in the plaintext to form the
 ciphertext.

 As an example, to encrypt the message ATTACKATDAWN with the keyword KEYS,
 we first write out our message as shown below,
    K	E	Y	S
    A	T	T	A
    C	K	A	T
    D	A	W	N

 Note: if the message to encode does not fit into the grid, you should pad
 the message with x's or random characters for example, ATTACKNOW with the
 keyword KEYS might look like below,
    K	E	Y	S
    A	T	T	A
    C	K	N	O
    W	X	X	X

 Once you have constructed your table, the columns are now reordered such
 that the letters in the keyword are ordered alphabetically,
    E	K	S	Y
    T	A	A	T
    K	C	T	A
    A	D	N	W

 The ciphertext is now read off along the columns, so in our example above,
 the ciphertext is TAATKCTAADNW.

 You should demonstrate your implementation by encrypting the file in the
 folder Q7 using the keyword - LOVELACE.

 ============================================================================
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <ctype.h>

#define BLOCK_LENGTH_MULTIPLIER 32

/**
 * Contains an array of size_t elements.
 */
typedef struct SizeTArray
{
  /**
   * The array of size_t elements.
   */
  size_t *values;

  /**
   * The number of elements in the array.
   */
  size_t number_of_elements;
} SizeTArray;

/**
 * Contains a character and its index.
 */
typedef struct IndexedCharacter
{
  /**
   * The character.
   */
  char character;

  /**
   * The character's index.
   */
  size_t index;
} IndexedCharacter;

static void encrypt_columnar_from_file(FILE*, const char*, char**);
static bool fill_with_text(FILE*, size_t, char**);
static bool is_alphanumeric(char);
static void reorder_by_column_positions(char *char_array, const SizeTArray *positions);
static void get_sorted_column_positions(const char*, size_t, SizeTArray**);
static void create_indexed_characters(const char *key, size_t key_length, IndexedCharacter***);
static void sort(IndexedCharacter**, size_t);

/**
 * Applies the Columnar Transposition Cipher to the contents of the file at the specified path, and
 * outputs the results to a character array. The outputs have been tested against
 * <a href="https://www.dcode.fr/columnar-transposition-cipher">this</a> online columnar
 * transposition cipher tool, in "Write by rows, read by rows" mode (accessed 16/05/2021).
 * @param message_filename The filename for the input file.
 * @param key The key.
 * @param result Where the results are stored. This is allocated as part of the function, so will
 * need freeing after.
 */
void encrypt_columnar(const char *message_filename, const char *key, char **result)
{
  // Open the file
  FILE *input = fopen(message_filename, "r");

  if (!input)
  {
    fprintf(stderr, "Could not find file at path %s", message_filename);
    exit(1);
  }

  // Apply the Columnar Transposition Cipher
  encrypt_columnar_from_file(input, key, result);

  // Close the file
  fclose(input);
}

/**
 * Applies the Columnar Transposition Cipher to the contents of the file, and outputs the results to
 * a character array.
 * @param input The input file.
 * @param key The key.
 * @param result Where the results are stored. This is allocated as part of the function, so will
 * need freeing after.
 */
static void encrypt_columnar_from_file(FILE *input, const char *key, char **result)
{
  // Positions stores the order of columns in the cipher according to the alphabetical ordering of
  // the key
  SizeTArray *positions = malloc(sizeof(SizeTArray));

  if (!positions)
  {
    fprintf(stderr, "Could not allocate space for positions");
    exit(1);
  }

  // The "row" represents a row in the cipher. Each row is the same size as the key
  size_t key_length = strlen(key);

  get_sorted_column_positions(key, key_length, &positions);

  char *row = calloc((key_length + 1), sizeof(char)); // This will add the NULL terminator

  if (!row)
  {
    fprintf(stderr, "Could not allocate space for row\n");
    exit(1);
  }

  size_t chars_in_result = 0;

  // The initial size of the result. We know that this must be a multiple of the key length, as it
  // will be padded with Xs if the file terminates early. Start off with space for 32 * key_length
  // characters. This can be expanded later if needs be
  size_t result_size = key_length * BLOCK_LENGTH_MULTIPLIER;
  *result = calloc(result_size + 1, sizeof(char));

  if (!*result) {
    fprintf(stderr, "Could not allocate space for result\n");
    exit(1);
  }

  // Keep looping until the end of the input file. In each loop, the row is filled with letters from
  // the file, and padded with Xs until it meets the desired length
  while (fill_with_text(input, key_length, &row))
  {
    // Reorder the columns according to the alphabetical ordering of the key
    reorder_by_column_positions(row, positions);

    // Is the current result big enough to hold the new characters?
    if (chars_in_result + key_length >= result_size)
    {
      // The result array is not big enough. Add room for another 32 * key_length characters
      result_size += key_length * BLOCK_LENGTH_MULTIPLIER;
      *result = realloc(*result, (result_size + 1) * sizeof(char));

      if (!*result)
      {
        fprintf(stderr, "Could not allocate space for result\n");
        exit(1);
      }
    }

    // Put the value from the row into the result array
    for (size_t i = 0; i < key_length; i++, chars_in_result++)
      (*result)[chars_in_result] = row[i];
  }

  // Cleaning up allocated memory
  free(positions->values);
  free(positions);
  positions = NULL;

  free(row);
  row = NULL;
}

/**
 * Fills up a buffer with alphanumeric characters from the file. Any non-alphanumeric characters
 * are discarded and do not appear in the buffer. If the file terminates before the buffer has
 * been filled, the buffer is padded with Xs until it is full.
 * @param input The input file to read from.
 * @param length The size of the buffer.
 * @param buffer The buffer to add characters to.
 * @return <code>true</code> if the buffer was filled, or <code>false</code> if an EOF was
 * encountered before any of the buffer was filled.
 */
static bool fill_with_text(FILE *input, const size_t length, char **buffer)
{
  size_t items_filled = 0;
  bool end_of_file_detected = false;

  // Keep filling until the buffer has been completely filled (Xs will be padded if the end of the
  // string file is reached before the buffer has been completely filled)
  while (items_filled < length)
  {
    if (end_of_file_detected)
    {
      // If the end of the file has previously been detected, just fill in an X here
      (*buffer)[items_filled] = 'X';
      items_filled++;
    } else
    {
      // The end of the file has not yet been detected, so get the next character from the file
      int character = fgetc(input);
      if (character == EOF)
      {
        // The next character is an end of file, so set the flag to true
        end_of_file_detected = true;

        // If no items were filled, then the buffer has not been modified. Return false to indicate
        // this to the caller
        if (items_filled == 0)
          return false;
      } else
      {
        // Not at the end of the file yet, but we only want to keep alphanumeric values
        if (is_alphanumeric((char) character))
        {
          // The character is alphanumeric. Convert it to uppercase (makes the encryption more
          // secure as case contains valuable information about the sentence structure), then add
          // it to the buffer in the appropriate position
          (*buffer)[items_filled] = (char) toupper(character);
          items_filled++;
        }
      }
    }
  }

  // The buffer buffer was filled so return true
  return true;
}

/**
 * Determines if a character is alphanumeric.
 * @param character The character to check.
 * @return {@code true} if the character is alphanumeric.
 */
static bool is_alphanumeric(const char character)
{
  return isalpha(character) || isdigit(character);
}

/**
 * Reorders the characters in the character array according to the new indices supplied.
 * @param char_array The array to be re-ordered.
 * @param positions The new indices.
 */
static void reorder_by_column_positions(char *char_array, const SizeTArray *positions)
{
  // Copy the original array into a new one. I could do this without using an additional array, but
  // the complexity of doing so would obfuscate the purpose of the code and that kind of
  // micro-optimisation is probably beyond the scope of this coursework
  char *copied_array = malloc((strlen(char_array) + 1) * sizeof(char));
  strcpy(copied_array, char_array);

  // Rearrange the existing array by inserting the values at the new indexes
  for (size_t i = 0; i < positions->number_of_elements; i++)
    char_array[i] = copied_array[positions->values[i]];

  // Clean up
  free(copied_array);
}

/**
 * Fills an array with the indexes of the characters in the key after the key has been sorted
 * alphabetically.
 * @param key The key to sort.
 * @param key_length The length of the key.
 * @param positions The array that will contain the indexes of the characters in the key after the
 * key has been sorted alphabetically.
 */
static void get_sorted_column_positions(
    const char *key, const size_t key_length, SizeTArray **positions)
{
  // Get an array of each character with its associated index in the key
  IndexedCharacter **indexed_characters = NULL;
  create_indexed_characters(key, key_length, &indexed_characters);

  // Sort the characters in the key
  sort(indexed_characters, key_length);

  // Create an array of the new indexes of each of the characters in the key after being sorted
  *positions = malloc(sizeof(SizeTArray));
  (*positions)->number_of_elements = key_length;
  (*positions)->values = malloc(key_length * sizeof(size_t));

  // Sequentially add the new indexes to the positions array, and clean up the indexed_characters
  // while this occurs
  for (int i = 0; i < key_length; i++)
  {
    (*positions)->values[i] = indexed_characters[i]->index;
    free(indexed_characters[i]);
  }

  // Clean up
  free(indexed_characters);
  indexed_characters = NULL;
}

/**
 * Creates the indexed characters based on the given key.
 * @param key The key whose characters should be indexed.
 * @param key_length The length of the key.
 * @param indexed_characters The pointer to the IndexedCharacter array. After running this function,
 * this will point to the created array.
 */
static void create_indexed_characters(
    const char *key, size_t key_length, IndexedCharacter ***indexed_characters)
{
  // Create space for the array
  *indexed_characters = malloc(key_length * sizeof(IndexedCharacter*));

  // Loop through each character in the key and add an IndexedCharacter to the array
  for (int i = 0; i < key_length; i++)
  {
    // Create the indexed character
    IndexedCharacter *indexed_character = malloc(sizeof(IndexedCharacter));
    indexed_character->character = key[i];
    indexed_character->index = i;

    // Add it to the array
    (*indexed_characters)[i] = indexed_character;
  }
}

/**
 * Sorts the array of indexed characters by the alphabetical value of the character. As this is
 * used to sort the characters in the key, the number of values is likely to be quite low.
 * Therefore, it seemed sensible to implement a simple sorting algorithm that is fast for small
 * arrays. As such, the sorting itself if performed using insertion sort. The implementation was
 * inspired by <a href="https://www.geeksforgeeks.org/insertion-sort/">this article</a>, accessed
 * 15/05/2021.
 * @param characters The indexed characters to sort.
 * @param number_of_characters The number of indexed characters in <code>characters</code>.
 */
static void sort(IndexedCharacter **characters, size_t number_of_characters)
{
  int current_char_index, compared_char_index;
  IndexedCharacter *current_value;

  // Iterate over the string, starting from the first character
  for (current_char_index = 1; current_char_index < number_of_characters; current_char_index++)
  {
    current_value = characters[current_char_index];

    // Start by comparing the character with the character before the current position
    compared_char_index = current_char_index - 1;

    // Compare the character with all of the characters before it, from the back to the front.
    // Stop when we get to the start of a string, or the compared character is less than this one
    while (compared_char_index >= 0
        && characters[compared_char_index]->character > current_value->character)
    {
      // The compared character is greater than the current character, so it needs to be shifted
      // over to the right
      characters[compared_char_index + 1] = characters[compared_char_index];
      compared_char_index--;
    }

    // Insert the current value in its correct position
    characters[compared_char_index + 1] = current_value;
  }
}

int main(int argc, char *argv[])
{
  const char *example_message = "./text.txt";
  const char *example_key = "LOVELACE";
  char *encrypted_message = NULL;

  encrypt_columnar(example_message, example_key, &encrypted_message);
  printf("Encrypted message = %s\n", encrypted_message);

  if (encrypted_message)
    free(encrypted_message);

	return EXIT_SUCCESS;
}
