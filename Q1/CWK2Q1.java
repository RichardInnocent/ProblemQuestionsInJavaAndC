import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *  @author Anonymous (do not change)
 *
 *  Question 1:
 *
 *  Implement the Shellsort algorithm (https://en.wikipedia.org/wiki/Shellsort) 
 *  for an array of up to 1000 signed doubles in Java. Your solution must use 
 *  concrete gaps of [1, 3, 7, 15, 31, 63, 127, 255, 511]. Your solution must 
 *  print the (partially) sorted array after each gap on a new line in the form:
 *      [a0, a1, a2, a3, ..., an]
 *  Where an is the nth element in the (partially) sorted array (please note 
 *  the space after the commas), and each element should be formatted to 2 
 *  decimal places (e.g. 1.00).
 *
*/

public class CWK2Q1 {

	private static final Collection<Integer> GAPS = Arrays.asList(511, 255, 127, 63, 31, 15, 7, 3, 1);

	public static void shell_sort(ArrayList<Double> array) {
		// Iterate through each gap, in decreasing size until we reach the final gap size (1)
		for (int gap : GAPS) {
			// Sort the array with this gap
			sortListWithGap(array, gap);

			// Print the results of this sort iteration, as required
			print(array);
		}
	}

	/**
	 * Performs an iteration of the shell sort of the specified array with the given gap size.
	 * @param array The array to sort.
	 * @param gap The gap size.
	 */
	private static void sortListWithGap(ArrayList<Double> array, int gap) {

		// Boundary index refers to the index that we're sorting up to.
		// The boundary index is initially set to the nth index, where n is the gap size. This allows
		// us to compare with the index in the first position. Continue to loop each index of the array
		// until we reach the end, at which point we can stop.
		for (int boundaryIndex = gap; boundaryIndex < array.size(); boundaryIndex++) {

			// Get the value that we'd like to place in the correct position for this gap size
			double valueToSort = array.get(boundaryIndex);

			// New index is the position that the value should be placed in. To start with, assume it's in
			// the correct place.
			int newIndex = boundaryIndex;

			// For each iteration, compare the value to sort against the value in the array gap indexes
			// before. If the other value is less than the value to sort, put the compared value where the
			// value to sort should be and update the new index to point to the previous index of the
			// compared value. Repeat this process, starting from the new position of the value to sort
			// until we get to the start of the array. If the compared value is greater than the value
			// being sorted, take no action.
			for (; newIndex >= gap && array.get(newIndex - gap) > valueToSort; newIndex -= gap) {

				// Put the compared value in the position that the value to sort should currently be in.
				// Note that this is not a swap, so we'll need to add the sorted value in later.
				array.set(newIndex, array.get(newIndex - gap));
			}

			// Finally, place the value that we've been sorting into its correct position.
			array.set(newIndex, valueToSort);
		}
	}

	/**
	 * Prints the array to a line in the console. The array is preceded with an opening square
	 * bracket. The elements are then printed, formatted to 2 decimal places, separated by a comma
	 * and space. The array is proceeded by a closing square brace and a line break.
	 * @param array The array to print.
	 */
	private static void print(ArrayList<Double> array) {
		System.out.print('[');
		if (!array.isEmpty()) {
			printNonEmpty(array);
		}
		System.out.println(']');
	}

	/**
	 * Prints a non-empty array to the console. Each element is formatted to 2 decimal places and is
	 * separated by a comma and space.
	 * @param array The array to print.
	 */
	private static void printNonEmpty(ArrayList<Double> array) {
		// Could use Collectors.joining from a stream but I don't need to store the entire thing in
		// memory before printing

		// Loop through all but the last element of the array
		for (int i = 0; i < array.size()-1; i++) {
			System.out.print(to2DecimalPlaceString(array.get(i)));

			// Not at the end of an array so we need a trailing comma
			System.out.print(", ");
		}

		// Print the last entry. Don't proceed this value with a comma.
		System.out.print(to2DecimalPlaceString(array.get(array.size()-1)));
	}

	/**
	 * Formats a double value to 2 decimal places.
	 * @param value The value to format.
	 * @return The result of the formatting.
	 */
	private static String to2DecimalPlaceString(double value) {
		return String.format("%.2f", value);
	}

	public static void main(String[] args) {
		ArrayList<Double> testList = new ArrayList<>();
		testList.add(3.4);
		testList.add(6.55);
		testList.add(-12.2);
		testList.add(1.73);
		testList.add(140.98);
		testList.add(-4.18);
		testList.add(52.87);
		testList.add(99.14);
		testList.add(73.202);
		testList.add(-23.6);
		
		shell_sort(testList);
	}
}
