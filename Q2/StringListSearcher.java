/**
 * Responsible for searching a list for a given value.
 */
public interface StringListSearcher {

  /**
   * Searches the list for a given value.
   * @param input The item to search for.
   * @return The index of the item in the underlying collection, or {@code -1} if the item could not
   * be found.
   * @throws NullPointerException Thrown if {@code input == null}.
   */
  int search(String input) throws NullPointerException;

}
