/**
 * Responsible for stripping undesirable contents from text.
 */
public interface Redactor {

  /**
   * Redacts undesirable contents from the text.
   * @param text The text to be stripped of undesirable content.
   * @return The result of the redaction.
   */
  String redact(String text);

}
