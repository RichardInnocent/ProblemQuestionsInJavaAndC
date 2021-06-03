/**
 * The "homemade" approach to solving the coursework. Rather than using any of Java's built-in
 * libraries, this uses custom-built Date objects based on the information provided in the brief.
 */
public class HomemadeTuesdayCounter implements TuesdayCounter {

  @Override
  public int countTuesdays() {

    // Start at the start date as defined by the brief
    Date date = new Date(1901, Month.JANUARY, 1);
    int tuesdays = 0;

    // Keep incrementing the year until we reach the 21st Century
    while (date.getYear() < 2001) {
      // Is the current day of the week a Tuesday?
      if (DayOfWeek.TUESDAY.equals(date.getDayOfWeek())) {
        tuesdays++; // Yes, so increment the count
      }

      // Move onto the next month
      date.plusMonths(1);
    }

    // Return the number of Tuesdays we found
    return tuesdays;
  }
}
