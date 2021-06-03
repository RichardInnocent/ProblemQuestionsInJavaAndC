/*
 ============================================================================
 Name        : CWK2Q3.c
 Author      : Anonymous (DO NOT CHANGE)
 Description :
 Implement your own Hash Table in C for storing and searching names, i.e. char
 arrays. In the event of collisions, you should use linear probing with an
 interval of 1. The hash function should be the sum of the ASCII values of the
 string modulo the size of the underlying data structure. Your Hash Table
 implementation should have the following interface:
	int hash_function(const char *key)
	void resize_map(int new_size)
	void add_to_map(const char *name)
	int remove_from_map(const char *name)
	int search_map(const char *name)
	void print_map()

 ============================================================================
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "CWK2Q3.h"

#define MAX_LOAD_FACTOR 0.7

static char** hash_map; // this is where you should store your names
static int current_size = 0; // Should be size_t, but this would break the hash_function interface
static int number_of_items = 0;
static const char* tombstone = "tombstone";

/**
 * Calculates a hash of the given value.
 * @param key The value to hash.
 * @return The hash, i.e. the ideal position for the element to be stored in the hash map.
 */
// This should really return a size_t but I don't want to change the interface provided
int hash_function(const char *key)
{
  int sum = 0;
  int ascii_value;

  // Iteratively loop through the key until we get to the end of the array, adding the ASCII value
  // each time
  while ((ascii_value = (int) *key++))
    sum += ascii_value; // this may overflow for very long strings, but that's acceptable

  // Module with the current size, as specified
  return sum % current_size;
}

/**
 * Attempts to add a value to the map, but does not attempt to resize the map if the load factor is
 * exceeded. Note that the value will not be added if an equivalent value already stored in the map.
 * @param name The value to add to the map.
 */
static void add_to_map_without_resizing(const char *name)
{
  // Calculate the hash of the element
  int index = hash_function(name);

  // If we find a tombstone element, we should overwrite it with the new value, assuming that a
  // duplicate of name wasn't found. To begin with, assume there is no tombstone to overwrite.
  int first_tombstone_index = -1;

  // Iterate through every element until we come across a NULL element. Even if we come across a
  // tombstone, we need to keep iterating until a NULL element to make sure that we aren't adding a
  // duplicate.
  while (hash_map[index])
  {
    // Make sure we don't allow duplicates! If the name is already contained in the table, don't add
    // the new one
    if (strcmp(hash_map[index], name) == 0)
      return;

    // If the index is a tombstone index, this would be a good place to insert the new value.
    // Remember this place, but continue checking to make sure there isn't a duplicate for this name
    // before we add it
    if (first_tombstone_index == -1 && hash_map[index] == tombstone)
      first_tombstone_index = index;

    index = next_index(index);
  }

  // If we found a tombstone index, put the new name there. Otherwise, put the element where the
  // NULL element was detected
  hash_map[first_tombstone_index == -1 ? index : first_tombstone_index] = (char*) name;

  // We've successfully added a new element, so update the number of elements so we can determine
  // the new load factor
  number_of_items++;
}

/**
 * Gets the next index from the current index. Usually, this will return <code>current_index + 1
 * </code>, although if this would be beyond the end of the array, <code>0</code> is returned
 * instead.
 * @param current_index The index to increment.
 * @return The index of the proceeding index.
 */
static int next_index(int current_index)
{
  // Try to increment the index. If this takes us past the end of the array, start over from the
  // beginning
  return ++current_index >= current_size ? 0 : current_index;
}

/**
 * Changes the capacity of the map to the new given size. Elements will be copied over to the new
 * map on re-size. If <code>new_size &lt; 1</code>, or the new size would cause the load factory of
 * 0.7 to be exceeded, the map will not be resized.
 * @param new_size The new capacity of the map.
 */
void resize_map(int new_size)
{
  // Make sure we don't allow resizing if the new size is less than 1, or if the new size would
  // cause the max load factor to be exceeded. It would be good to return some code for this, but I
  // don't want to change the interface
  if (new_size < 1 || ((double) number_of_items) / ((double) new_size) > MAX_LOAD_FACTOR)
    return;

  // Reset the number of items. This will be updated iteratively as we re-add the values from the
  // old map
  number_of_items = 0;

  // Store a reference to the old map so we can copy values over after the resizing
  char** old_map = hash_map;

  // Allocate the new size
  hash_map = calloc(new_size, sizeof(char*));

  if (!hash_map) {
    printf("Failed to allocate memory for the hash_map\n");
    exit(1);
  }

  // Update the size, but keep a record of the old one so we can still iterate over the old array
  int old_size = current_size;
  current_size = new_size;

  // If the old hash map was uninitialised (i.e. has a size of 0), don't bother copying over the old
  // values
  if (old_size > 0)
  {
    // Loop through the old array. Any non-null and non-tombstone entries should be added to the new
    // map.
    for (int i = 0; i < old_size; i++)
    {
      if (old_map[i] && old_map[i] != tombstone)
        add_to_map_without_resizing(old_map[i]);
    }
  }

  // Free up the memory for the old structure that is no longer required
  if (old_map)
  {
    free(old_map);
    old_map = NULL;
  }
}

/**
 * Attempts to add the name to the map. The name will not be added if an equivalent value already
 * exists in the map. This may trigger a doubling of the size of the map if the maximum load factor
 * is exceeded after adding the element. Finally, if the map is uninitialised, calling this method
 * will initialise the map with an initial capacity of 10.
 * @param name The value to be added to the map.
 */
void add_to_map(const char *name)
{
  // If the map is uninitialised, give it a default size of 10 so this operation doesn't fail
  if (current_size == 0)
    resize_map(10);

  // Provided the load factor is enforced in other parts of the application, there'll always be room
  // to add the element first before resizing (if necessary).
  add_to_map_without_resizing(name);

  // Check if adding the value put us over the 0.7 load factor threshold. If so, double the size of
  // the underlying data structure. Note that we can't do this operation prior to adding the
  // element. Although this would be convenient to save us from having the rehash the value, we
  // wouldn't be sure if adding the value actually increased the number of elements in the
  // structure, as duplicates aren't added.
  if (((double) (number_of_items)) / ((double) current_size) > MAX_LOAD_FACTOR)
    resize_map(current_size * 2); // Assume that doubling the size of the map is sensible
}

/**
 * Attempts to remove an entry matching <code>name</code> from the map. If a value is found and
 * removed, the map will not be resized.
 * @param name The value to remove from the map.
 * @return <code>1</code> if the value was found and removed, or <code>0</code> if the value was not
 * found.
 */
int remove_from_map(const char *name)
{
  int removed_index = index_of(name);
  if (removed_index == -1)
    return 0; // Element not found so there's nothing to remove

  // Deleted elements should be replaced with a tombstone to indicate that there is no longer an
  // element in this position. This is deliberately different from NULL, as a NULL pointer would
  // break the searching mechanism.
  hash_map[removed_index] = (char*) tombstone;

  // We've successfully removed an element, to decrement the number of items so we can calculate the
  // new load factor on subsequent operations
  number_of_items--;

  // We could resize the map here if we wanted to but this would be computationally expensive. For
  // now, we'll assume that optimising for speed is more important than optimising for memory
  // utilisation.
  return 1;
}

/**
 * Searches the map for the given name.
 * @param name The value to search for.
 * @return <code>1</code> if the name was found in the map, or <code>0</code> if not.
 */
int search_map(const char *name)
{
  // If the index is -1 then the value could not be found
  return index_of(name) == -1 ? 0 : 1;
}

/**
 * Gets the index of the given value in the map.
 * @param name The value to search for.
 * @return The index of an entry matching <code>name</code>, or <code>-1</code> if the value is not
 * stored in the map.
 */
static int index_of(const char *name)
{
  // Loop through the array from the hash of the name up until the first NULL entry
  for (int index = hash_function(name); hash_map[index]; index = next_index(index))
  {
    // If the value at this index is equal to name, we've found a match so return that index
    if (strcmp(name, hash_map[index]) == 0)
      return index;
  }
  // Return the status code -1 to indicate that the value could not be found in the map
  return -1;
}

/**
 * Prints the map element by element, separated by commas. This isn't a great print function as it
 * shows the implementation detail which the caller shouldn't really need to know. However, it's
 * probably suitable for coursework as it allows the assessor (hi) to easily check how map's
 * internal state.
 */
void print_map()
{
  // Iterate through the array and print each element except for the last one. Each element is
  // proceeded by a comma
  for (int i = 0; i < current_size-1; i++)
  {
    if (hash_map[i])
      print_value_at_index(i);
    printf(", ");
  }
  // Print the last element, not proceeded by a comma
  print_value_at_index(current_size-1);
  printf("\n");
}

/**
 * Prints out the value at the given index in the map. The value will be printed as follows:
 * <ul>
 *   <li><b>Matches an entry</b>: The value of the entry</li>
 *   <li><b>Matches a tombstone</b>: <code>[TOMBSTONE]</code></li>
 *   <li><b>Matches a <code>NULL</code> element</b>: No value will be printed</li>
 * </ul>
 * @param index The index in the map to print.
 */
static void print_value_at_index(int index)
{
  if (hash_map[index])
    printf("%s", hash_map[index] == tombstone ? "[TOMBSTONE]" : hash_map[index]);
}

int main(int argc, char *argv[])
{
  char *stringOne = "#Hello world";
  char *stringTwo = "How are you?";
  char *stringThree = "Be the best you...!!";
  char *stringFour = "Be kind to yourself";
  char *stringFive = "Principles of Programming 2";

  resize_map(6);
  add_to_map(stringOne);
  add_to_map(stringTwo);
  add_to_map(stringOne);
  add_to_map(stringThree);
  add_to_map(stringFour);
  add_to_map(stringFive);
  print_map();

  int ret = search_map(stringOne);
  if(ret)
    printf("Found %s!\n", stringOne);

  remove_from_map(stringThree);

  ret = search_map(stringFive);
  if(ret)
    printf("Found %s!\n", stringFive);
  print_map();

  add_to_map(stringThree);
  print_map();

  // Clean up! No need to free the strings added to the map as (in this instance) these exist on the
  // stack so will be freed when the function completes.
  free(hash_map);
  hash_map = NULL;

  return EXIT_SUCCESS;
}
