import java.util.Objects;
import java.util.Optional;

/**
 * Simple implementation of a {@link Redactor}, responsible for stripping undesirable contents from
 * an item of text. The redactor uses a {@link RedactionConfiguration} to specify its functionality.
 */
public class SimpleTextRedactor implements Redactor {

  private final RedactionConfiguration configuration;

  /**
   * Creates a new text redactor, responsible for stripping undesirable contents from an item of
   * text.
   * @param configuration The configuration that specifies what and how redactions should
   * be found and replaced.
   * @throws NullPointerException Thrown if {@code redactionConfiguration == null}.
   */
  public SimpleTextRedactor(RedactionConfiguration configuration) throws NullPointerException {
    this.configuration = Objects.requireNonNull(configuration, "Configuration is null");
  }

  @Override
  public String redact(String text) throws NullPointerException {
    Objects.requireNonNull(text, "Text is null");

    // Create an instance to hold the text that will be continually updated and the index that its
    // been updated to
    SequentiallyModifiedString result = new SequentiallyModifiedString(text);

    // Sequentially loop through the text until all of the redactions have been applied. This
    // algorithm only loops through the text once, which is convenient for long items of text,
    // particularly where the number of redacted phrases is low
    while (result.index < text.length()) {
      // The text is updated within the invoked method. I decided not to update the index in the
      // same why as it made it more difficult to see how the index was being modified within the
      // context of this while loop. Now it should be clearer to see when the loop will terminate
      result.index += tryRedactionFromIndex(result);
    }

    // Return the result
    return result.string;
  }

  /**
   * Attempts to redact text from the index. The redaction may be in the form of a redacted phrase,
   * or an automatically detected proper noun.
   * @param text The text to redact. Its index will be updated as part of this method, and its
   * string will be updated too if a redaction is applied.
   * @return The number of characters redacted.
   */
  private int tryRedactionFromIndex(SequentiallyModifiedString text) {
    // Try to do phrase matching first as noun detection may otherwise obscure some matches
    int increment = replacePhrasesFromIndex(text);

    // If there was a match from performing phrase matching, return the number of characters that
    // can be skipped
    if (increment > 0) {
      return increment;
    }

    // Perform proper noun redaction
    increment = redactProperNounsFromIndex(text);

    // If there was a match from performing proper noun matching, return the number of characters
    // that can be skipped
    if (increment > 0) {
      return increment;
    }

    // No match occurred - just move onto the next character
    return 1;
  }

  /**
   * Redacts phrases at the given index, if detected.
   * @param text The text to replace. This method will update the text and set the index to the end
   * of the match if a match is found.
   * @return The number of characters redacted.
   */
  private int replacePhrasesFromIndex(SequentiallyModifiedString text) {
    // Loop through each phrase that should be redacted
    for (String redactedPhrase : configuration.getRedactedPhrases()) {
      // Check if the redacted phrase can be found at this index
      Optional<Integer> matchLength = getLengthOfPhraseMatchAtIndex(text, redactedPhrase);
      if (matchLength.isPresent()) {
        return matchLength.get();
      }
    }

    // No phrase match was detected
    return 0;
  }

  /**
   * Determines the length of the match between the text and the index provided in {@code text} and
   * the redacted phrase, or an empty optional if no match was determined. The length of that match
   * may be different to {@code redactedPhrase.length()} if the redacted phrase contains a space,
   * and the matching phrase also contains whitespace but in a different quantity. For example,
   * matching the redacted phrase "United Kingdom" in the text "United   Kingdom" would return a
   * match length of 16.
   * @param text The text to compare, containing the index at which the comparison should occur.
   * @param redactedPhrase The redacted phrase.
   * @return THe length of the matched phrase.
   */
  private Optional<Integer> getLengthOfPhraseMatchAtIndex(
      SequentiallyModifiedString text, String redactedPhrase
  ) {
    // If word matching is used, make sure that this is the start of a word
    if (configuration.isFullWordMatching() && !isAtStartOfWord(text.string, text.index)) {
      // Not the start of a word
      return Optional.empty();
    }

    int textIndex = text.index;
    int phraseIndex = 0;

    // Loop through the text from the start index and iteratively compare the characters in the text
    // and the redacted phrase
    while (textIndex < text.string.length() && phraseIndex < redactedPhrase.length()) {

      char characterInText = text.string.charAt(textIndex);
      char characterInPhrase = redactedPhrase.charAt(phraseIndex);

      // Whitespace in phrases will always be replaced with single spaces for simplicity.
      // Check if the character in the phrase is whitespace...
      if (characterInPhrase == ' ') {

        // If the character in the text is not also whitespace, then there's no match
        if (!Character.isWhitespace(characterInText)) {
          return Optional.empty();
        }

        // Continue through the text, skipping all other whitespace
        for (; textIndex < text.string.length(); textIndex++) {
          if (!Character.isWhitespace(text.string.charAt(textIndex))) {
            break; // Found the first non-whitespace character so stop skipping elements
          }
        }
      } else {
        // Compare the characters
        if (!charactersAreEqual(text.string.charAt(textIndex), redactedPhrase.charAt(phraseIndex))) {
          // Characters aren't equal to this isn't a match
          return Optional.empty();
        }
        // The characters matched, so move onto the next ones
        textIndex++;
      }

      phraseIndex++;
    }

    // Check if the phrase was encountered before the end of the text string
    if (phraseIndex != redactedPhrase.length()) {
      return Optional.empty();
    }

    // Ensure that word matching is not required, or that it's at the end of a word
    if (!configuration.isFullWordMatching()
        || isWordSeparatorOrBeyondEndOfText(text.string, textIndex)
    ) {
      text.string = redactCharactersBetweenIndices(text.string, text.index, textIndex);
      return Optional.of(textIndex - text.index);
    }
    return Optional.empty();
  }

  /**
   * Checks if a character is at the start of the word.
   * @param text The text.
   * @param index The index of the character.
   * @return {@code true} if the index is at the start of the string, or the character before it
   * is a word separator as defined by {@link RedactionConfiguration#isWordSeparator(char)}.
   */
  private boolean isAtStartOfWord(String text, int index) {
    return index == 0 || configuration.isWordSeparator(text.charAt(index - 1));
  }

  /**
   * Checks if the index provided is beyond the bounds of the text or if the character at the index
   * is a word separator as defined by the {@link RedactionConfiguration}.
   * @param text The text.
   * @param index The index of the character.
   * @return {@code true} if {@code index} is beyond the bounds of the text or if the character at
   * the index is a word separator.
   */
  private boolean isWordSeparatorOrBeyondEndOfText(String text, int index) {
    return index >= text.length()
        || configuration.isWordSeparator(text.charAt(index));
  }

  /**
   * Checks if two characters are equal. If {@link RedactionConfiguration#isMatchRedactedWordCase()}
   * is disabled, the characters will be matched case insensitively.
   * @param char1 The first character.
   * @param char2 The second character.
   * @return {@code true} if the characters are equal according to the {@link
   * RedactionConfiguration}.
   */
  private boolean charactersAreEqual(char char1, char char2) {
    return configuration.isMatchRedactedWordCase() ?
        char1 == char2 : Character.toLowerCase(char1) == Character.toLowerCase(char2);
  }

  /**
   * Redacts the specified number of characters from the given string at the given index.
   * @param text The text that should be redacted.
   * @param startIndex The index that the redaction should start at.
   * @param numberOfCharactersToRedact The number of characters to redact.
   * @return The text with the redaction applied.
   * @throws IndexOutOfBoundsException Thrown if {@code startIndex < 0 || startIndex >=
   * text.length()}, or {@code startIndex + numberOfCharactersToRedact < 0 || startIndex +
   * numberOfCharactersToRedact >= text.length()}.
   */
  private String redactCharactersFromIndex(
      String text, int startIndex, int numberOfCharactersToRedact
  ) throws IndexOutOfBoundsException {
    String redactionString = buildRedactionString(numberOfCharactersToRedact);
    return text.substring(0, startIndex)
        + redactionString
        + text.substring(startIndex + numberOfCharactersToRedact);
  }

  /**
   * Builds the redaction replacement string. The character that is used is specified by {@link
   * RedactionConfiguration#getReplacementCharacter()}.
   * @param length The length of the redaction replacement string.
   * @return The redaction replacement string.
   */
  private String buildRedactionString(int length) {
    return String.valueOf(configuration.getReplacementCharacter()).repeat(length);
  }

  /**
   * Redacts the characters between the given indices of the text. All non-word separators between
   * these indices are replaced with {@link
   * RedactionConfiguration#getReplacementCharacter()}.
   * @param text The original text.
   * @param startIndex The start index (inclusive) of the substring to redact.
   * @param endIndex The end index (exclusive) of the substring to redact.
   * @return The original text with the redaction applied.
   */
  private String redactCharactersBetweenIndices(String text, int startIndex, int endIndex) {
    // Build the redaction to be inserted
    String redactionString = buildRedactionString(text.substring(startIndex, endIndex));

    // Insert the redaction in place of the offending part of the original text
    return text.substring(0, startIndex) + redactionString + text.substring(endIndex);
  }

  /**
   * Builds a redaction for the given substring. All non-word separators will be replace with {@link
   * RedactionConfiguration#getReplacementCharacter()}.
   * @param substringToBeMasked The substring to be masked.
   * @return The redacted form of the substring.
   */
  private String buildRedactionString(String substringToBeMasked) {
    StringBuilder redactionBuilder = new StringBuilder();

    // Loop through each character in the substring
    for (int i = 0; i < substringToBeMasked.length(); i++) {
      char characterToBeMasked = substringToBeMasked.charAt(i);

      // If the character is whitespace, leave it as is. Otherwise, replace it with the masking
      // character
      redactionBuilder.append(
          Character.isWhitespace(characterToBeMasked) ?
              characterToBeMasked : configuration.getReplacementCharacter()
      );
    }

    // Return the result
    return redactionBuilder.toString();
  }

  /**
   * Attempts to redact proper nouns from the text, if appropriate. The proper noun detection that
   * is performed is specified by the {@link RedactionConfiguration}.
   * @param text The text to apply the redactions to.
   * @return The number of characters redacted.
   */
  private int redactProperNounsFromIndex(SequentiallyModifiedString text) {
    // If proper noun detection is disabled, don't do anything
    if (ProperNounDetection.DISABLED.equals(configuration.getProperNounDetection())) {
      return 0;
    }

    // If sentence case should be accounted for, check if the character at the index is at the start
    // of a sentence. If so, don't continue redacting
    if (ProperNounDetection.CAPITALISED_EXCLUDING_START_OF_SENTENCES
            .equals(configuration.getProperNounDetection())
          && isFirstAlphabeticCharacterInSentence(text.string, text.index)
    ) {
      return 0;
    }

    // If the word does not start with a capital letter, or the character is not at the start of a
    // word, then this isn't a proper noun so stop redacting.
    if (!Character.isUpperCase(text.string.charAt(text.index))
        || !isAtStartOfWord(text.string, text.index)
    ) {
      return 0;
    }

    // Get the number of additional characters (other than the first) in the proper noun
    Optional<Integer> additionalCharacterInWordOptional =
        getLengthOfCurrentWordIfAllLowercase(text.string, text.index + 1);

    // If any uppercase characters were detected in the additional characters, it wasn't a proper
    // noun, so don't perform any further actions
    if (additionalCharacterInWordOptional.isEmpty()) {
      return 0;
    }

    // Get the complete length of the proper noun
    int lengthOfProperNoun = 1 + additionalCharacterInWordOptional.get();

    // Exclude single-letter names like "I"
    if (lengthOfProperNoun <= 1) {
      return 0;
    }

    // Perform the redaction
    text.string = redactCharactersFromIndex(text.string, text.index, lengthOfProperNoun);

    return lengthOfProperNoun;
  }

  /**
   * Checks if the character at the given index is at the start of a sentence.
   * @param text The text.
   * @param index The index of the character to check.
   * @return {@code true} if the character at the given index is at the start of a sentence.
   */
  private boolean isFirstAlphabeticCharacterInSentence(String text, int index) {
    // No need to determine if it's at the start of a sentence if the character is non-alphabetic
    return Character.isAlphabetic(text.charAt(index))
        && isPrecededByStartOfStringOrSentenceTerminatorThenWhitespace(text, index);
  }

  /**
   * Checks if the character at the given index is preceded by a period then a sequence of one or
   * more whitespace characters.
   * @param text The text.
   * @param index The index of the character.
   * @return {@code true} if the character is preceded by a period then a sequence of one or more
   * whitespace characters.
   */
  private boolean isPrecededByStartOfStringOrSentenceTerminatorThenWhitespace(
      String text, int index
  ) {
    // If the index is 0 then it's at the start of the string
    if (index < 1) {
      return true;
    }

    // Ensure there's at least one word separator before the period. This saves us from catching
    // things like hello.there
    char previousCharacter = text.charAt(index-1);
    if (!configuration.isWordSeparator(previousCharacter)) {
      return false;
    }

    // Iteratively loop back through the string until we get to the start
    for (int i = index-2; i > 0; i--) {
      char character = text.charAt(i);
      // If the character is a marks the end of the previous sentence, then this must be the start
      // of a new sentence
      if (isSentenceTerminator(character)) {
        return true;
      }

      // If we hit any other character that isn't classified as a word separator, then it's not the
      // start of a sentence
      if (!configuration.isWordSeparator(character)) {
        return false;
      }
    }

    // The case when the string is preceded by a load of word separators (e.g. whitespace) but
    // there's no sentence terminator. We should still assume that this is the start of sentence
    return true;
  }

  /**
   * Checks if a character marks the end of a sentence.
   * @param character The character to assess.
   * @return {@code true} if the character marks the end of a sentence.
   */
  private boolean isSentenceTerminator(char character) {
    return character == '.' || character == '!' || character == '?';
  }

  /**
   * Counts the number of lowercase letters until the end of a string or a word separator is
   * encountered.
   * @param text The text.
   * @param wordStartIndex The start position for counting.
   * @return The length of the lowercase word, or an empty optional if the word is not all in
   * lowercase.
   */
  private Optional<Integer> getLengthOfCurrentWordIfAllLowercase(
      String text, int wordStartIndex
  ) {
    int length = 0;

    // Loop through the text from the index until the end of the string
    for (int index = wordStartIndex; index < text.length(); index++) {
      char currentCharacter = text.charAt(index);
      // If a word separator is detected, return the length of the string
      if (configuration.isWordSeparator(currentCharacter)) {
        return Optional.of(length);
      }
      // If we come across a non-alphabetic character, it's not classified as a word separator or it
      // would have been caught above. Therefore we only have to make sure that, if it's alphabetic,
      // it's lowercase. If so, it's not a lowercase word so return an empty optional.
      if (Character.isAlphabetic(currentCharacter) && Character.isUpperCase(currentCharacter)) {
        return Optional.empty();
      }

      // The character is a valid part of the word, so increment the word length
      length++;
    }
    return Optional.of(length);
  }

  /**
   * <p>Holds a string and the index that the string has been modified up until.</p>
   * <p>This is effectively just a struct, and is only used internally. As a result, I've chosen to
   * treat as such and to not complicate the calling code by forcing the use of getters and setters.
   * </p>
   */
  private static class SequentiallyModifiedString {
    private String string;
    private int index = 0;

    // Initialises the instance with the given string
    private SequentiallyModifiedString(String string) {
      this.string = string;
    }
  }
}
