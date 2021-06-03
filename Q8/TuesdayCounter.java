/**
 * An interface to count the number of Tuesdays that occurred on the first day of the month over
 * every month in the 20th Century.
 */
@FunctionalInterface
public interface TuesdayCounter {

  /**
   * Counts the number of Tuesdays that occurred on the first day of the month over every month in
   * the 20th Century.
   * @return The number of Tuesdays.
   */
  int countTuesdays();

}
