/**
 * <p>Responsible for generating a numerical value for a string that will return a result that
 * corresponds to the natural ordering of the string. This converter can only be used for strings
 * that do not contain any characters outside of the extended ASCII values. In other words, every
 * character in the string must have a value lower than 256.</p>
 * <p>This converter uses the bytes in the long as follows:
 * <table>
 *   <tr>
 *     <th>Byte Position</th>
 *     <th>Value</th>
 *   </tr>
 *   <tr>
 *     <td>0</td>
 *     <td>The integer value of the <em>first</em> character</td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>The integer value of the <em>second</em> character</td>
 *   </tr>
 *   <tr>
 *     <td>...</td>
 *     <td>...</td>
 *   </tr>
 *   <tr>
 *     <td>7</td>
 *     <td>The integer value of the <em>eighth</em> character</td>
 *   </tr>
 * </table>
 * </p>
 * <p>For example, for the string "{@code hello!}"...
 * {@code ['h', 'e', 'l', 'l', 'o']} is {@code [104, 101, 108, 108, 111, 33]} according to the ASCII
 * value of each character. Therefore, the bytes in the returned value would be:
 * {@code [104, 101, 108, 108, 111, 33, 0, 0]}, which corresponds to a value of
 * 7,522,537,965,568,983,040.
 * </p>
 * <p>Characters beyond the eighth position are ignored and do not form part of the result, so as a
 * result the returned value will collide for any two strings that are identical for the first eight
 * characters.</p>
 * <p>Using this method of number generation ensures that the values always correspond to the
 * natural order of the characters.</p>
 * <p>Note: There are almost definitely ways of generating the more distributive values based on the
 * string inputs (using {@link Object#hashCode()} could be a good candidate), however I'm under the
 * impression that arrays must be sorted in <em>alphabetical</em> order.</p>
 */
public class StringToAlphabeticalValueConverter {

  /**
   * Converts the input to a numeric value that correlates to its natural order.
   * @param input The value to convert.
   * @return The numeric value.
   * @throws NullPointerException Thrown if {@code input == null}.
   * @throws IllegalArgumentException Thrown if the {@code input} contains any characters such that
   * {@code ((int) char) > 255}.
   * @see StringToAlphabeticalValueConverter
   */
  public long toNumericalValue(String input) throws NullPointerException, IllegalArgumentException {
    long result = 0L;

    // Loop through the first 8 characters, adding the numerical value with the bit shift applied
    for (int i = 0; i < input.length() && i < 8; i++) {
      // Get the numeric value of the character, bit shift it to the correct position and then add
      // it to the result
      result += (toNumericalValue(input.charAt(i)) << (8 * (8 - i - 1)));
    }
    return result;
  }

  /**
   * Converts a character to a numerical value.
   * @param character The character to convert.
   * @return The numerical value of the character.
   * @throws IllegalArgumentException Thrown if the {@code ((int) char) > 255}.
   */
  private long toNumericalValue(char character) throws IllegalArgumentException {
    long value = character;

    // Is the value outside of the extended ASCII range? If so, throw an exception
    if (value > 255) {
      throw new IllegalArgumentException(
          "Cannot characters that are not part of the extended ASCII character set"
      );
    }
    return value;
  }
}
