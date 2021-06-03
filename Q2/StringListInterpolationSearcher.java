import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of a {@link StringListSearcher} that allows the list to be searched via
 * interpolation.
 */
public class StringListInterpolationSearcher implements StringListSearcher {

  // Responsible for converting the strings to a numerical value
  private static final StringToAlphabeticalValueConverter CONVERTER =
      new StringToAlphabeticalValueConverter();

  private static final Comparator<String> STRING_COMPARATOR =
      Comparator.comparingLong(CONVERTER::toNumericalValue).thenComparing(str -> str);

  private final List<StringAndValue> stringAndValues;

  /**
   * Creates a new {@link StringListSearcher} that allows the list to be searched via interpolation.
   * @param values The list to be searched.
   * @throws IllegalArgumentException Thrown if the list is not order according to the comparator
   * returned from {@link #getComparator()}. The list should be sorted prior to calling this method
   * to ensure there are no unexpected side-effects from calling this constructor. The list can be
   * sorted using {@link #sort(List)}.
   */
  public StringListInterpolationSearcher(List<String> values) throws IllegalArgumentException {
    ensureSorted(values);
    stringAndValues = values.stream().map(this::toStringAndValue).collect(Collectors.toList());
  }

  /**
   * Sorts a list of string according the comparator that will be used by the interpolation search
   * algorithm. This is <em>probably</em> equivalent to the results of calling {@code
   * Collections.sort(values)}, but we can't guarantee it so this method is provided.
   * @param values The list to sort.
   */
  public static void sort(List<String> values) {
    values.sort(getComparator());
  }

  /**
   * Gets the comparator that will be used to perform the interpolation search.
   * @return The comparator that will be used to perform the interpolation search.
   */
  public static Comparator<String> getComparator() {
    return STRING_COMPARATOR;
  }

  /**
   * Ensures that the provided list is sorted in accordance with the {@link #STRING_COMPARATOR}.
   * @param values The list to verify.
   * @throws IllegalArgumentException Thrown if the list is not in order.
   */
  private void ensureSorted(List<String> values) throws IllegalArgumentException {
    // 0-length and single element arrays will always be in order
    if (values.size() < 2) {
      return;
    }

    // Loop through each of the values in the string
    String previous = null;
    for (String value : values) {
      // If the previous value is greater than this value, it's not in order according to our
      // comparator
      if (previous != null && STRING_COMPARATOR.compare(previous, value) > 0) {
        throw new IllegalArgumentException(
            "The provided list is not sorted. Please use StringListInterpolationSearcher.sort to "
                + "sort the list."
        );
      }
      previous = value;
    }
  }

  /**
   * Searches the list for a given value.
   * @param input The item to search for.
   * @return The index of the item in the underlying collection, or {@code -1} if the item could not
   * be found.
   * @throws NullPointerException Thrown if {@code input == null}.
   * @throws IllegalArgumentException Thrown if any of the characters in {@code input} have a value
   * greater than 255.
   */
  @Override
  public int search(String input) throws NullPointerException, IllegalArgumentException {

    // If the underlying list is empty, it will never contain the input.
    // If it has a size of 1, it must match the first element
    switch (stringAndValues.size()) {
      case 0:
        return -1;
      case 1:
        return input.equals(stringAndValues.get(0).string) ? 0 : -1;
    }

    // Calculate the desired numerical value
    long desiredValue = CONVERTER.toNumericalValue(input);

    // Start the interpolation search, looking at the values between the start and end of the list
    int lowIndex = 0;
    int highIndex = stringAndValues.size() - 1;

    // Keep searching unless the desired value falls on the wrong side of one of the bounds of our
    // search. This could happen if the list does not contain the desired value. If the lower bound
    // had a numeric value just lower than the searched value, when the lower bound is incremented
    // it may exceed the searched value. In this case, we know we don't have a match, so stop
    // looping.
    while (
        lowIndex != highIndex
            && desiredValue >= stringAndValues.get(lowIndex).value
            && desiredValue <= stringAndValues.get(highIndex).value
    ) {

      // If the value matches the lower bound, return a match
      if (input.equals(stringAndValues.get(lowIndex).string)) {
        return lowIndex;
      }

      // If the value matches the upper bound, return a match
      if (input.equals(stringAndValues.get(highIndex).string)) {
        return highIndex;
      }

      long denominator = stringAndValues.get(highIndex).value - stringAndValues.get(lowIndex).value;

      int probe;

      if (denominator == 0L) {
        // If the denominator is 0 then the all strings in the considered range must now have the
        // same first 8 characters. In this case, we can't continue to do interpolation search, and
        // must switch to using binary search.
        probe = lowIndex + (highIndex - lowIndex) / 2;
      } else {
        // Perform the interpolation to get the probed element. I used this site for guidance with the
        // formula: https://www.baeldung.com/java-interpolation-search

        // We need to be careful of overflows here on the numerator here, so use BigIntegers to be
        // safe
        BigInteger numerator = BigInteger
            .valueOf(highIndex - lowIndex)
            .multiply(BigInteger.valueOf(desiredValue - stringAndValues.get(lowIndex).value));

        long divisionResult = numerator.divide(BigInteger.valueOf(denominator)).longValue();
        // Get the index to probe
        probe = (int) (lowIndex + divisionResult);
      }

      // Get the value at the probe
      StringAndValue valueAtProbe = stringAndValues.get(probe);

      // Is the probe the value we're looking for?
      if (input.equals(valueAtProbe.string)) {
        // Yes, return the probed index
        return probe;
      }

      // No, so keep looking...

      // Is the value we're looking for "greater" than the probe?
      if (getComparator().compare(input, valueAtProbe.string) > 0) {
        // Yes, so we only need to consider the array from the probe to the high index. As we know
        // the probe doesn't match, we can ignore this element
        lowIndex = probe + 1;
      } else {
        // No, so we only need to consider the array from the low index to the probe. As we know the
        // probe doesn't match, we can ignore this element
        highIndex = probe - 1;
      }
    }

    // No match was found.
    return -1;
  }

  /**
   * Converts a string into a {@link StringAndValue} instance.
   * @param input The input string.
   * @return The {@link StringAndValue} instance.
   */
  private StringAndValue toStringAndValue(String input) {
    return new StringAndValue(input, CONVERTER.toNumericalValue(input));
  }

  /**
   * Another struct-like class to hold a string and its numeric value so we don't have to keep
   * re-evaluating it. This inner class is not exposed anywhere outside of this class so we're safe
   * to use it like a struct, without obfuscating the calling logic with getters and setters.
   */
  private static class StringAndValue {
    private final String string;
    private final long value;

    /**
     * Creates a new instance with the given string and value.
     * @param string The string.
     * @param value The numeric representation of the string.
     */
    public StringAndValue(String string, long value) {
      this.string = string;
      this.value = value;
    }
  }
}
