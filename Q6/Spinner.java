/**
 * The redaction process can take a little while. This spinner is a simple runnable class that
 * prints a spinner to the terminal so that the user knows that a process is occurring.
 */
public class Spinner implements Runnable {

  // The full cycle of spinner positions, in order
  private static final char[] SPINNER_POSITIONS = {'|', '/', '-', '\\', '|', '/', '-', '\\'};

  private final String prefix;

  /**
   * Creates a new spinner.
   * @param prefix The text that will appear before the spinner. If this is {@code null}, no text
   * will appear before the spinner.
   */
  public Spinner(String prefix) {
    this.prefix = prefix == null ? "" : prefix;
  }

  @Override
  public void run() {
    boolean running = true;
    int spinnerPositionIndex = 0;

    // Keep printing until the thread terminates
    while (running) {

      // Print the spinner
      System.out.print("\r" + prefix + SPINNER_POSITIONS[spinnerPositionIndex]);

      // Increment the spinner position
      spinnerPositionIndex = nextSpinnerPositionIndex(spinnerPositionIndex);

      // Pause for a quarter of a second
      try {
        Thread.sleep(250L);
      } catch (InterruptedException e) {
        running = false;
      }
    }
  }

  private int nextSpinnerPositionIndex(int currentIndex) {
    int result = currentIndex + 1;
    return result < SPINNER_POSITIONS.length ? result : 0;
  }
}
