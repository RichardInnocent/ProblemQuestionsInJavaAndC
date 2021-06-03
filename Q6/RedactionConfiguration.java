import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * The configuration that should be applied when applying redactions to text.
 */
public class RedactionConfiguration {

  private final List<String> redactedPhrases;
  private final boolean matchRedactedWordCase;
  private final boolean fullWordMatching;
  private final String wordSeparatorRegex;
  private final ProperNounDetection properNounDetection;
  private final char replacementCharacter;

  // Retrieve the values from the builder to initialise the class
  private RedactionConfiguration(Builder builder) {
    this.redactedPhrases = buildRedactedPhrases(builder);

    // Phrases are sorted so that the longest phrases will appear first when retrieved. See the
    // getRedactedPhrases() Javadoc for info on why
    sortPhrases();

    this.matchRedactedWordCase = builder.matchRedactedWordCase;
    this.fullWordMatching = builder.fullWordMatching;
    this.wordSeparatorRegex = builder.wordSeparatorRegex;
    this.properNounDetection = builder.properNounDetection;
    this.replacementCharacter = builder.replacementCharacter;
  }

  private List<String> buildRedactedPhrases(Builder builder) {
    // If sub-phrase matching is enabled, just break down each phrase into its separate words
    // (according to whitespace) and use these as the redacted phrases
    if (builder.subPhraseMatching) {
      return builder
          .redactedPhrases
          .stream()
          .map(phrase -> phrase.split("\\s+")) // split by whitespace
          .flatMap(Arrays::stream)
          .distinct()
          .collect(Collectors.toList());
    }

    // Otherwise, just return a new collection of the redacted phrases
    return new ArrayList<>(builder.redactedPhrases);
  }

  /**
   * The phrases should be sort in reverse alphabetical order of their size. This accounts for
   * sub-phrases being part of other redacted phrases.
   * @see #getRedactedPhrases()
   */
  private void sortPhrases() {
    redactedPhrases.sort((o1, o2) -> Integer.compare(o2.length(), o1.length()));
  }

  /**
   * Checks if the given character is a word separator according to this configuration. This should
   * only be used if {@link #isFullWordMatching()}.
   * @param character The character to assess.
   * @return {@code true} if and only if the given character is a word separator.
   */
  public boolean isWordSeparator(char character) {
    return Character.toString(character).matches(wordSeparatorRegex);
  }

  /**
   * <p>Gets the collection of phrases that should be redacted from text.</p>
   * <p>The phrases are sorted in reverse alphabetical order of their size. This accounts for sub-
   * phrases being part of other redacted phrases. For example, consider the following redacted
   * phrases:
   * <ul>
   *   <li>Charles</li>
   *   <li>Charles Dickens</li>
   * </ul>
   * Then consider apply this redaction configuration to the following passage:
   * <blockquote>Oliver Twist was written by Charles Dickens.</blockquote>
   * Clearly the desired output in the above phrase is as follows:
   * <blockquote>Oliver Twist was written by ***************.</blockquote></p>
   * <p>However, If "Charles" was searched for and redacted in the text first, the passage would
   * become:
   * <blockquote>Oliver Twist was written by ******* Dickens.</blockquote>
   * Subsequently searching for "Charles Dickens" in the text would result in no match.</p>
   * <p>By returning the longest phrases first, the caller can safely apply these redactions without
   * having to worry too much about the order in which they are applied.</p>
   * @return The collection of phrases that should be redacted from text.
   */
  public Collection<String> getRedactedPhrases() {
    return new ArrayList<>(redactedPhrases);
  }

  /**
   * Determines whether redacted phrases should be matched case-sensitively or case-insensitively.
   * @return {@code true} if redacted phrase matching should be performed case-sensitively.
   */
  public boolean isMatchRedactedWordCase() {
    return matchRedactedWordCase;
  }

  /**
   * Determines whether phrases should be matched as full words/phrases only. For example, consider
   * the redacted word "app", applied to a passage containing the word "trapped". Without word
   * matching, the result should be "tr***ed". However, by forcing word matching, the characters
   * either side of the phrase should be checked and used to deem whether it's part of a word or
   * a word/phrase in its own right.
   * @return {@code true} if full word matching should be performed.
   * @see #isWordSeparator(char)
   */
  public boolean isFullWordMatching() {
    return fullWordMatching;
  }

  /**
   * <p>The type of proper noun detection to perform.</p>
   * <p>In some case, a full list of phrases that should be redacted is provided. However, this may
   * not always be the case (as specified in the coursework description), additional support for
   * compiling this list of phrases should be provided by the program.</p>
   * <p>This particular option allows the caller to specify whether, and how proper nouns should be
   * detected and subsequently redacted from the original text.</p>
   * @return The proper noun detection to perform.
   */
  public ProperNounDetection getProperNounDetection() {
    return properNounDetection;
  }

  /**
   * When phrases are redacted, the redacted contents should be replaced with an equivalent number
   * of masking characters. This method returns the character that should be used to replace these
   * phrases.
   * @return The character that should be used to replace redacted phrases in the original text.
   */
  public char getReplacementCharacter() {
    return replacementCharacter;
  }

  /**
   * Creates a builder for {@link RedactionConfiguration} objects.
   * @return A new builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RedactionConfiguration)) {
      return false;
    }
    RedactionConfiguration that = (RedactionConfiguration) o;
    return matchRedactedWordCase == that.matchRedactedWordCase
        && fullWordMatching == that.fullWordMatching
        && replacementCharacter == that.replacementCharacter
        && Objects.equals(redactedPhrases, that.redactedPhrases)
        && Objects.equals(wordSeparatorRegex, that.wordSeparatorRegex)
        && properNounDetection == that.properNounDetection;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        redactedPhrases, matchRedactedWordCase, fullWordMatching, wordSeparatorRegex,
        properNounDetection, replacementCharacter
    );
  }

  /**
   * A builder for {@link RedactionConfiguration} objects.
   */
  public static class Builder {

    private final Collection<String> redactedPhrases = new HashSet<>();
    private boolean matchRedactedWordCase = false;
    private boolean fullWordMatching = true;
    private boolean subPhraseMatching = false;
    private String wordSeparatorRegex = "[^a-zA-Z\\d]";
    private ProperNounDetection properNounDetection =
        ProperNounDetection.CAPITALISED_EXCLUDING_START_OF_SENTENCES;
    private char replacementCharacter = '*';

    /**
     * Sets phrases that should be redacted from text. If unspecified, no phrases will be
     * configured.
     * @param redactedWords The phrases that should be redacted from text.
     * @return This builder.
     */
    public Builder withRedactedPhrases(String... redactedWords) {
      if (redactedWords != null) {
        withRedactedPhrases(Arrays.asList(redactedWords));
      }
      return this;
    }

    /**
     * Sets the phrases that should be redacted from text. If unspecified, no phrases will be
     * configured.
     * @param redactedPhrases The phrases that should be redacted from text.
     * @return This builder.
     */
    public Builder withRedactedPhrases(Collection<String> redactedPhrases) {
      if (redactedPhrases != null) {
        redactedPhrases
            .stream()
            // St. Petersburg has a trailing space which doesn't need to be matched, so remove it
            .map(String::trim)
            .map(phrase -> phrase.replaceAll("\\s+", " "))
            .forEach(this.redactedPhrases::add);
      }
      return this;
    }

    /**
     * Specifies whether redacted phrases should be matched case-sensitively or case-insensitively.
     * If unspecified, this will be {@code true}.
     * @param matchCase Should be set to {@code true} if redacted phrase matching should be
     * performed case-sensitively.
     */
    public Builder withMatchRedactedWordCase(boolean matchCase) {
      this.matchRedactedWordCase = matchCase;
      return this;
    }

    /**
     * Specifies whether phrases should be matched as full words/phrases only. For example, consider
     * the redacted word "app", applied to a passage containing the word "trapped". Without word
     * matching, the result should be "tr***ed". However, by forcing word matching, the characters
     * either side of the phrase should be checked and used to deem whether it's part of a word or
     * a word/phrase in its own right. If unspecified, this will be {@code true}.
     * @param fullWordMatching Should be set to {@code true} if full word matching should be
     * performed.
     * @see #withWordSeparatorCharacterRegex(String)
     */
    public Builder withFullWordMatching(boolean fullWordMatching) {
      this.fullWordMatching = fullWordMatching;
      return this;
    }

    /**
     * Specifies whether sub-phrases should be matched. For example, given a redacted phrase of
     * "Hello world", should "Hello" be redacted from the sentence "Hello John"? This can be useful
     * for name inputs, although should be used with caution. If unspecified, sub-phrase matching
     * will be disabled.
     * @param subPhraseMatching Should be {@code true} if sub-phrases should be matched.
     * @return This builder.
     */
    public Builder withSubPhraseMatching(boolean subPhraseMatching) {
      this.subPhraseMatching = subPhraseMatching;
      return this;
    }

    /**
     * Specifies that regex that will be used to determine whether a character is a word separator.
     * @param wordSeparatorRegex The regex that will be used to determine whether a character is a
     * word separator. If unspecified, this will be {@code [^a-zA-Z\d]}, i.e. a word separator is
     * defined as any non-ASCII or non-alphanumeric character.
     * @return This builder.
     * @throws NullPointerException Thrown if {@code wordSeparatorRegex == null}.
     * @throws PatternSyntaxException Thrown if the regex is invalid.
     */
    public Builder withWordSeparatorCharacterRegex(String wordSeparatorRegex)
        throws NullPointerException, PatternSyntaxException {
      Pattern.compile(wordSeparatorRegex); // Make sure the pattern is valid before saving it
      this.wordSeparatorRegex = Objects.requireNonNull(wordSeparatorRegex);
      return this;
    }

    /**
     * <p>Specifies the type of proper noun detection to perform.</p>
     * <p>In some case, a full list of phrases that should be redacted is provided. However, this
     * may not always be the case (as specified in the coursework description), additional support
     * for compiling this list of phrases should be provided by the program.</p>
     * <p>This particular option allows the caller to specify whether, and how proper nouns
     * be detected and subsequently redacted from the original text.</p>
     * <p>If unspecified, this will be {@link
     * ProperNounDetection#CAPITALISED_EXCLUDING_START_OF_SENTENCES}.</p>
     * @param properNounDetection The type of proper noun detection to perform.
     * @return This builder.
     * @throws NullPointerException Thrown if {@code properNounDetection == null}.
     */
    public Builder withProperNounDetection(ProperNounDetection properNounDetection)
        throws NullPointerException {
      this.properNounDetection = properNounDetection;
      return this;
    }

    /**
     * When phrases are redacted, the redacted contents should be replaced with an equivalent number
     * of masking characters. This specifies returns the character that should be used to replace
     * these phrases. If unspecified, this will be {@code *}.
     * @param replacementCharacter The character that will be used to mask redactions.
     * @return This builder.
     */
    public Builder withReplacementCharacter(char replacementCharacter) {
      this.replacementCharacter = replacementCharacter;
      return this;
    }

    /**
     * Builds the configuration.
     * @return The configuration.
     */
    public RedactionConfiguration build() {
      return new RedactionConfiguration(this);
    }
  }
}
