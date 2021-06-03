import java.util.ArrayList;
import java.util.List;

/**
 *  @author Anonymous (do not change)
 *
 *  Question 2:
 *
 *  Implement interpolation search for a list of Strings in Java
 *  using the skeleton class provided. The method should return
 *  the position in the array if the string is present, or -1 if
 *  it is not present.
*/

public class CWK2Q2 {

	public static int interpolation_search(List<String> array, String item) {
		// Sort the array according to the searcher implementation sort. This is probably the same as
		// Collections.sort, using the natural ordering of Strings, but we need this to be guaranteed.
		// This is being sorted in alphabetical order as this is my understanding of the coursework.
		// If this was not a requirement, it may have been possible to produce more distributive numeric
		// values (such as by using String.hashCode).
		StringListInterpolationSearcher.sort(array);

		// Create the searcher. I use a separate instance here so that it can be reused quickly and
		// easily to search over the same array. This doesn't really lend itself well to the interface
		// provided by the coursework starter code, but you can see how this would vastly improve speed
		// for repeat searches over the same underlying collection.
		StringListSearcher searcher = new StringListInterpolationSearcher(array);
		return searcher.search(item);
	}

	public static void main(String[] args) {
		ArrayList<String> testList = new ArrayList<>();
		testList.add("Hello");
		testList.add("World");
		testList.add("How");
		testList.add("Are");
		testList.add("You");

		int result = interpolation_search(testList, "How");
		System.out.println("Result = " + result);
	}
}
