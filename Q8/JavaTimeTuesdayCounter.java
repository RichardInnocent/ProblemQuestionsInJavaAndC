import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * The more standardised approach to solving the coursework. This uses Java's in-built java.time
 * package.
 */
public class JavaTimeTuesdayCounter implements TuesdayCounter {

  @Override
  public int countTuesdays() {
    // Start at the start date specified in the brief
    LocalDate date = LocalDate.of(1901, 1, 1);
    int tuesdays = 0;

    // Continually increment the month until we reach the 21st Century.
    while (date.getYear() < 2001) {
      // Is the day a Tuesday?
      if (DayOfWeek.TUESDAY.equals(date.getDayOfWeek())) {
        tuesdays++; // Yes, so increment the number of Tuesdays
      }
      // Increment the month (LocalDate is immutable, hence the reassignment)
      date = date.plusMonths(1);
    }

    // Return the count
    return tuesdays;
  }
}
