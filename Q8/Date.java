import java.util.Objects;

/**
 * An implementation of a date. This is not by any means a robust date implementation, but should be
 * sufficient based on the coursework brief.
 */
public class Date {

  /**
   * The "epoch" here is 1900-01-01 - not to be confused with the Unix epoch.
   */
  private int daysSinceEpoch = 0;

  private int dayOfMonth = 1;
  private Month month = Month.JANUARY;
  private int year = 1900;

  /**
   * Creates a new date at the epoch time of 1900-01-01.
   */
  public Date() {}

  /**
   * Creates a date instance at the given date.
   * @param year The year.
   * @param month The month of the year.
   * @param dayOfMonth The day of the month.
   * @throws NullPointerException Thrown if {@code month == null}.
   * @throws IllegalArgumentException Thrown if {@code year < 1900} or {@code dayOfMonth} is not
   * appropriate for the specified month (e.g. 32nd January).
   */
  public Date(int year, Month month, int dayOfMonth)
      throws NullPointerException, IllegalArgumentException {
    // Make sure that we're not going before the epoch time - we don't need to support this
    if (year < 1900) {
      throw new IllegalArgumentException("Invalid year. Must be >= 1900");
    }

    // Make sure the month was provided
    Objects.requireNonNull(month, "Month is null");

    // Make sure the day of the month is valid
    if (dayOfMonth < 1 || dayOfMonth > month.getDaysInMonth(year)) {
      throw new IllegalArgumentException("Invalid number of days in the specified month");
    }

    // Add the number of years between the epoch time and the specified year. For example, if the
    // desired year is 1904, add 4 years.
    plusYears(year - this.year);

    // Increment the months until we reach the desired month
    while (this.month != month) {
      plusMonths(1);
    }

    // Finally, add the days until we reach the desired date
    plusDays(dayOfMonth - this.dayOfMonth);
  }

  /**
   * Gets the day of the week.
   * @return The day of the week.
   */
  public DayOfWeek getDayOfWeek() {
    // Given that the epoch time was a Monday and no days have been skipped or repeated since, we
    // can retrieve the day with a simple modulo. There's no need to keep track of this in the same
    // way we need to keep track of the month and year.
    return DayOfWeek.values()[daysSinceEpoch % 7];
  }

  /**
   * Gets the day of the month.
   * @return The day of the month.
   */
  public int getDayOfMonth() {
    return dayOfMonth;
  }

  /**
   * Gets the month of the year.
   * @return The month of the year.
   */
  public Month getMonth() {
    return month;
  }

  /**
   * Gets the year.
   * @return The year.
   */
  public int getYear() {
    return year;
  }

  /**
   * Increments the date by the specified number of years.
   * @param years The number of year to increment by.
   * @throws IllegalArgumentException Thrown if {@code years < 0}.
   */
  public void plusYears(int years) throws IllegalArgumentException {
    // Check that the time isn't being decremented - that isn't required for this coursework
    if (years < 0) {
      throw new IllegalArgumentException("Cannot step backwards through time");
    }

    // Incrementing by 2 years is the same as incrementing by 2 * 12 = 24 months
    plusMonths(years * 12);
  }

  /**
   * Increments the date by the specified number of months.
   * @param months The number of months to increment by.
   * @throws IllegalArgumentException Thrown if {@code months < 0}.
   */
  public void plusMonths(int months) throws IllegalArgumentException {
    if (months < 0) {
      throw new IllegalArgumentException("Cannot step backwards through time");
    }
    // Keep adding the number of days in the current month until we've done this enough time to have
    // added the desired number of months
    for (int i = 0; i < months; i++) {
      // This could be called a lot so skip the sign check for efficiency. We know this will always
      // be positive
      plusDaysAndDoNotCheckSign(month.getDaysInMonth(year));
    }
  }

  /**
   * Increments the date by the specified number of days.
   * @param days The number of days to increment by.
   * @throws IllegalArgumentException Thrown if {@code days < 0}.
   */
  public void plusDays(int days) throws IllegalArgumentException {
    if (days < 0) {
      throw new IllegalArgumentException("Cannot step backwards through time");
    }
    plusDaysAndDoNotCheckSign(days);
  }

  private void plusDaysAndDoNotCheckSign(int days) {
    // Increment the day of the month by the given amount
    dayOfMonth += days;

    // Does this increment overflow beyond the month's end? If so, continually advance the month
    // until we reach the correct month
    while (dayOfMonth > month.getDaysInMonth(year)) {
      // Subtract the days in this month before we advance. This means we'll effectively "start
      // again" from the 1st of the next month, but the number of days to add will now be lower
      dayOfMonth -= month.getDaysInMonth(year);

      // Will incrementing the month cause the year to rollover? If so, increment the year
      if (month.isLastMonthOfYear()) {
        year++;
      }

      // Increment the month
      this.month = Month.monthAfter(month);
    }

    // Increment the epoch time so that we can keep track of the day of the week
    daysSinceEpoch += days;
  }

}
