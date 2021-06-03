/**
 * <p>In some case, a full list of phrases that should be redacted is provided. However, this may
 * not always be the case (as specified in the coursework description), additional support for
 * compiling this list of phrases should be provided by the program.</p>
 * <p>This enum defines whether, and how proper nouns should be detected and subsequently redacted
 * from the original text.</p>
 */
public enum ProperNounDetection {

  /**
   * Automatic proper noun detection will not be performed.
   */
  DISABLED,

  /**
   * Proper nouns are detected as any capitalised string, where the first letter of the word is a
   * capital letter and the subsequent characters are all lowercase.
   */
  CAPITALISED,

  /**
   * Proper nouns are detected as any capitalised string, where the first letter of the word is a
   * capital letter, and all subsequent characters are in lowercase. However, this attempts to
   * exclude sentence case, where capitalised words appear at the start of sentences.
   */
  CAPITALISED_EXCLUDING_START_OF_SENTENCES
}
