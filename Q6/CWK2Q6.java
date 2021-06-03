import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *  @author Anonymous (do not change)
 *
 *  Question 6:
 *
 *  Implement in Java a similar algorithm to that in Q5, i.e. given a
 *  block of text your algorithm should be able to redact words from
 *  a given set and outputs the result to a file called â€œresult.txtâ€�.
 *  However, in this implementation of the algorithm all
 *  redactable words will be proper nouns (i.e. a name used for an
 *  individual person, place, or organisation, spelled with an initial
 *  capital letter) and your algorithm should take into account that
 *  the list of redactable words might not be complete. For example,
 *  given the block of text:
 *      It was in July, 1805, and the speaker was the well-known Anna
 *      Pavlovna Scherer, maid of honor and favorite of the Empress
 *      Marya Fedorovna. With these words she greeted Prince Vasili
 *      Kuragin, a man of high rank and importance, who was the first
 *      to arrive at her reception. Anna Pavlovna had had a cough for
 *      some days. She was, as she said, suffering from la grippe;
 *      grippe being then a new word in St. Petersburg, used only by
 *      the elite.
 *
 *  and the redactable set of words
 *      Anna Pavlovna Scherer, St. Petersburg, Marya Fedorovna
 *
 *  the output text should be
 *      It was in ****, 1805, and the speaker was the well-known ****
 *      ******** *******, maid of honor and favorite of the *******
 *      ***** *********. With these words she greeted ****** ******
 *      *******, a man of high rank and importance, who was the first
 *      to arrive at her reception. **** ******** had had a cough for
 *      some days. She was, as she said, suffering from la grippe;
 *      grippe being then a new word in *** **********, used only by
 *      the elite.
 *
 *  You should test your program using the example files provided.
*/

public class CWK2Q6 {

	/**
	 * Applies the redaction, writing the results out to "result.txt".
	 * @param textFilename The filename of the file that should be redacted.
	 * @param redactedPhrasesFilename The filename of the file that contains the phrases that should
	 * be redacted.
	 */
	public static void redactWords(String textFilename, String redactedPhrasesFilename) {
		try {
			redactWordsAndThrowExceptions(textFilename, redactedPhrasesFilename);
			System.out.println(System.lineSeparator() + "Redaction complete");
		} catch (IOException e) {
			System.err.println("Problem encountered reading from or writing to the result file");
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to apply the redaction, and write the results out to "result.txt".
	 * @param textFilename The filename of the file that should be redacted.
	 * @param redactedPhrasesFilename The filename of the file that contains the phrases that should
	 * be redacted.
	 * @throws IOException Thrown if there is problem accessing any of the files.
	 */
	private static void redactWordsAndThrowExceptions(
			String textFilename, String redactedPhrasesFilename
	) throws IOException {
		RedactionConfiguration redactionConfiguration =
				buildRedactionConfigurationFromFile(redactedPhrasesFilename);
		Redactor redactor = new SimpleTextRedactor(redactionConfiguration);
		writeRedactionToFile(textFilename, "result.txt", redactor);
	}

	/**
	 * Builds the redaction configuration with the appropriate settings for this task.
	 * @param redactedPhrasesFilename The filename for the file that contains the redacted phrases.
	 * @return The appropriate redaction configuration.
	 * @throws IOException Thrown if there is a problem reading from the redacted phrases file.
	 */
	private static RedactionConfiguration buildRedactionConfigurationFromFile(
			String redactedPhrasesFilename
	) throws IOException {
		return RedactionConfiguration
				.builder()
				.withRedactedPhrases(getRedactedPhrases(redactedPhrasesFilename))
				.withMatchRedactedWordCase(false)
				.withFullWordMatching(true)
				.withProperNounDetection(ProperNounDetection.CAPITALISED_EXCLUDING_START_OF_SENTENCES)
				.withReplacementCharacter('*')
				.build();
	}

	/**
	 * Gets the phrases that should be redacted.
	 * @param redactedPhrasesFilename The filename for the file that contains the redacted phrases.
	 * @return The phrases that should be redacted.
	 * @throws IOException Thrown if there is a problem reading from the redacted phrases file.
	 */
	private static Collection<String> getRedactedPhrases(String redactedPhrasesFilename)
			throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(redactedPhrasesFilename))) {
			return getRedactedPhrases(reader);
		}
	}

	/**
	 * Gets the phrases that should be redacted.
	 * @param fileReader The source of the redacted phrases.
	 * @return The phrases that should be redacted.
	 * @throws IOException Thrown if there is a problem reading from the file.
	 */
	private static Collection<String> getRedactedPhrases(BufferedReader fileReader)
			throws IOException {
		String line;
		Set<String> redactedWords = new HashSet<>();

		// Loop through the file until the end, and add all redacted words to the collection
		while ((line = fileReader.readLine()) != null) {
			redactedWords.add(line);
		}
		return redactedWords;
	}

	/**
	 * Performs the redaction, reading the input file and writing out the results.
	 * @param textFilename The filename for the input file.
	 * @param outputFilename The filename for the output file.
	 * @param redactor The instance that performs the redactions.
	 * @throws IOException Thrown if there is a problem writing to or reading from the files.
	 */
	private static void writeRedactionToFile(
			String textFilename, String outputFilename, Redactor redactor
	) throws IOException {
		Thread spinnerThread = null;

		// Initialise the readers
		try (
				BufferedReader textReader = new BufferedReader(new FileReader(textFilename));
				BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFilename))
		) {
			// Looks like the files exist, so start a spinner so the user knows that the redaction is in
			// progress...
			Spinner spinner = new Spinner("Redacting contents from file. Please wait... ");
			spinnerThread = new Thread(spinner);
			spinnerThread.start();

			// Perform the redaction
			writeRedactionToFile(textReader, outputWriter, redactor);
		} finally {
			if (spinnerThread != null) {
				spinnerThread.interrupt();
			}
		}
	}

	/**
	 * Performs the redaction, reading the input file and writing out the results.
	 * @param textReader The reader for the input file.
	 * @param outputWriter The writer for the output file.
	 * @param redactor The instance that performs the redactions.
	 * @throws IOException Thrown if there is a problem writing to or reading from the files.
	 */
	private static void writeRedactionToFile(
			BufferedReader textReader, BufferedWriter outputWriter, Redactor redactor
	) throws IOException {
		String line;

		// With the example file being so large, we really want to avoid pulling the entire thing into
		// memory at once. Unfortunately, processing the file line by line would cause issues as content
		// will wrap over multiple lines, which would mess up things like sentence detection. As a
		// compromise, text is processed paragraph by paragraph, where a paragraph is any body of text
		// separated by a blank line

		StringBuilder paragraphBuilder = null;

		// Iteratively read every line
		while ((line = textReader.readLine()) != null) {

			// Check if the line is blank.
			if (line.isBlank()) {

				// Process the paragraph (if there is one before this empty line) and write out the results
				if (paragraphBuilder != null) {
					outputWriter.append(redactor.redact(paragraphBuilder.toString()));
					paragraphBuilder = null;
				}

				// Write out this line as is. The line may have whitespace content so preserve it
				outputWriter.append(line);
				outputWriter.newLine();
			} else {
				// It's not a new line. If we haven't started building a paragraph, start now
				if (paragraphBuilder == null) {
					paragraphBuilder = new StringBuilder();
				}
				// Add the line to the paragraph
				paragraphBuilder.append(line).append(System.lineSeparator());
			}
		}

		// If the text didn't end with a line break, make sure the final paragraph is written out
		if (paragraphBuilder != null) {
			outputWriter.append(redactor.redact(paragraphBuilder.toString()));
		}
	}

	public static void main(String[] args) {
		String inputFile = "./warandpeace.txt";
		String redactFile = "./redact.txt";
		redactWords(inputFile, redactFile);
	}
}
