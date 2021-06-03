import java.util.function.Function;

/**
 * Defines a month in a year.
 */
public enum Month {

  JANUARY(year -> 31),
  FEBRUARY(year -> {
    // If the year is not divisible by 4, it's definitely not a leap year
    if (year % 4 != 0) {
      return 28;
    }
    // It's divisible by 4 so...
    // If it's divisible by 100 too then it's not a leap year, unless it happens to be divisible by
    // 400
    return year % 100 != 0 || year % 400 == 0 ? 29 : 28;
  }),
  MARCH(year -> 31),
  APRIL(year -> 30),
  MAY(year -> 31),
  JUNE(year -> 30),
  JULY(year -> 31),
  AUGUST(year -> 31),
  SEPTEMBER(year -> 30),
  OCTOBER(year -> 31),
  NOVEMBER(year -> 30),
  DECEMBER(year -> 31, true);

  private final Function<Integer, Integer> yearToDaysFunction;
  private final boolean lastMonthOfYear;

  Month(Function<Integer, Integer> yearToDaysFunction) {
    this(yearToDaysFunction, false);
  }

  Month(Function<Integer, Integer> yearToDaysFunction, boolean lastMonthOfYear) {
    this.yearToDaysFunction = yearToDaysFunction;
    this.lastMonthOfYear = lastMonthOfYear;
  }

  /**
   * Gets the number of days in the month according to the specified year. In most cases, the year
   * won't affect the number of days, however this will be used when determining the number of days
   * in February due to leap years.
   * @param year The year that contains the month.
   * @return The number of days in the month.
   */
  public int getDaysInMonth(int year) {
    return yearToDaysFunction.apply(year);
  }

  /**
   * Determines if this is the last month of the year.
   * @return {@code true} if this is the last month of the year.
   */
  public boolean isLastMonthOfYear() {
    return lastMonthOfYear;
  }

  /**
   * Gets the month after the one specified.
   * @param month The current month.
   * @return The month after.
   */
  public static Month monthAfter(Month month) {
    int nextOrdinal = month.ordinal() + 1;

    // Does an enum exist with that ordinal? If not, it must be the last month so get the first one
    // as it must be the next year.
    return nextOrdinal >= Month.values().length ? Month.values()[0] : Month.values()[nextOrdinal];
  }

}
