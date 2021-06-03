/*
 ============================================================================
 Name        : CWK2Q4.c
 Author      : Anonymous (DO NOT CHANGE)
 Description :
 Implement your own XOR Linked List (https://en.wikipedia.org/wiki/XOR_linked_list)
 in C capable of storing names. Your implementation should have the following
 functions:
    void insert_string(const char* newObj)
	int insert_before(const char* before, const char* newObj)
	int insert_after(const char* after, const char* newObj)
	void remove_string(char* result)
	int remove_after(const char *after, char *result)
	int remove_before(const char *before, char *result)
    void print_list()

 ============================================================================
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "CWK2Q4.h"

/**
 * Contains details of a node in the linked list.
 */
typedef struct ListNode {

  /**
   * The value at the node.
   */
  const char *value;

  /**
   * The next/previous XOR.
   */
  struct ListNode *npx;
} ListNode;

/**
 * Stores details of the position of a given node.
 */
typedef struct NodePosition {

  /**
   * The node that was searched for.
   */
  ListNode *node;

  /**
   * The node prior to the detected node. This may be <code>NULL</code>, likely if <code>node ==
   * head</code>.
   */
  ListNode *preceding_node;

  /**
   * The node after the detected node. This may be <code>NULL</code>, likely if <code>node</code> is
   * the tail.
   */
  ListNode *proceeding_node;
} NodePosition;

static ListNode *head = NULL;

/**
 * Calculates the XOR of the pointers provided in <code>value1</code> and <code>value2</code>.
 * @param value1 The first value. May be <code>NULL</code>.
 * @param value2 The second value. May be <code>NULL</code>.
 * @return The XOR of the pointers. This will be <code>NULL</code> if <code>!value1 && !value2
 * </code>.
 */
static ListNode *xor(ListNode *value1, ListNode *value2)
{
  // Cast to pointers to perform the XOR operation, then cast the result back to a ListNode pointer.
  return (ListNode*) ((uintptr_t) value1 ^ (uintptr_t) value2);
}

/**
 * Gets the next node in the sequence.
 * @param previous_node The node before the current node.
 * @param current_node The current node.
 * @return The next node. This may be <code>NULL</code> if <code>current_node</code> is the tail.
 */
static ListNode *next_node(ListNode *previous_node, ListNode *current_node)
{
  return xor(previous_node, current_node->npx);
}

/**
 * Gets the previous node in the sequence.
 * @param current_node The current node.
 * @param next_node The node after the current node.
 * @return The previous node. This may be <code>NULL</code> if <code>current_node == head</code>.
 */
static ListNode *previous_node(ListNode *current_node, ListNode *next_node)
{
  return xor(current_node->npx, next_node);
}

/**
 * Attempts to find the first node with the given value.
 * @param value The value to search for.
 * @param position Will return the position of the node with the given value. If the value cannot be
 * found, this will be unmodified. This will be allocated as part of the function, if the value is
 * found, so will need freeing after invoking.
 * @return <code>true</code> if the value was found.
 */
static bool find(const char *value, NodePosition** position)
{
  // Start from the head
  ListNode *current = head;
  ListNode *previous = NULL;
  ListNode *next;

  // Iterate through each element in the list
  while (current)
  {
    // Calculate the next node
    next = next_node(previous, current);

    // Check if the current node matches the target value
    if (strcmp(current->value, value) == 0)
    {
      // Current value matches, so create a position result and return it.
      *position = malloc(sizeof(NodePosition));
      if (!*position) {
        printf(
            "Found node, but could not create position structure. Request will be processed as "
            "though the node could not be found\n"
        );
        return false;
      }

      (*position)->node = current;
      (*position)->preceding_node = previous;
      (*position)->proceeding_node = next;
      return true;
    }

    // Update the previous
    previous = current;
    current = next;
  }
  return false;
}

/**
 * Creates a new node with the given value and inserts it between the two nodes.
 * @param before The node before the insertion point.
 * @param value The value to be inserted.
 * @param after The node after the insertion point.
 */
static void insert_between_nodes(ListNode *before, const char *value, ListNode *after)
{
  // Creates the new node
  ListNode *new_node = (ListNode *) malloc(sizeof(ListNode));
  if (!new_node) {
    printf("Could not create node\n");
    return;
  }
  new_node->value = value;
  new_node->npx = xor(before, after);

  // If we're not at the head, remove reference to after node from the before node, and reference
  // new node instead
  if (before)
    before->npx = xor(xor(before->npx, after), new_node);

  // If we're not at the tail, remove reference to before node from the after node, and reference
  // new node instead
  if (after)
    after->npx = xor(xor(after->npx, before), new_node);

  // If the after node was the head then we are inserting at the start of the list, so the head will
  // need to be updated to point at the new node.
  if (after == head)
    head = new_node;
}

/**
 * Inserts the value before another in the list. If there are multiple instances of <code>
 * before</code> in the list, this only inserts the new value before the first matching element.
 * @param before The value that <code>new_value</code> should be inserted before.
 * @param new_value The value to insert.
 * @return <code>1</code> if the insertion was successful, or <code>0</code> if not, for example if
 * <code>before</code> was not found in the list.
 */
int insert_before(const char* before, const char* new_value)
{
  // Find the first instance of the before value in the list
  NodePosition *position = NULL;
  if (!find(before, &position))
    return 0; // before value was not found in the list, so don't insert the new value

  // Insert the value
  insert_between_nodes(position->preceding_node, new_value, position->node);

  // Free up the result - we no longer need this
  free(position);

  // We successfully added a value, so return true
  return 1;
}

/**
 * Inserts the value after another in the list. If there are multiple instances of <code>
 * after</code> in the list, this only inserts the new value after the first matching element.
 * @param before The value that <code>new_value</code> should be inserted after.
 * @param new_value The value to insert.
 * @return <code>1</code> if the insertion was successful, or <code>0</code> if not, for example if
 * <code>before</code> was not found in the list.
 */
int insert_after(const char *after, const char *new_value)
{
  // Find the first instance of the before value in the list
  NodePosition *position = NULL;
  if (!find(after, &position))
    return 0; // The after value was not found in the list, so don't insert the new value

  // Insert the value
  insert_between_nodes(position->node, new_value, position->proceeding_node);

  // Free up the result - we no longer need this
  free(position);

  // We successfully added a value, so return true
  return 1;
}

/**
 * Inserts a value at the beginning of the list.
 * @param new_value The value to insert.
 */
void insert_string(const char *new_value)
{
  // Always insert at the start, i.e. before the current head
  insert_between_nodes(NULL, new_value, head);
}

/**
 * Removes a node from the list.
 * @param before The node before the node that is being removed. This will be updated such that its
 * <code>npx</code> value points to <code>after</code> instead of <code>node</code>.
 * @param node The node to be removed. Note that this will be freed from the heap as part of the
 * deletion process.
 * @param after The node after the node that is being removed. This will be updated such that its
 * <code>npx</code> value points to <code>before</code> instead of <code>node</code>.
 */
static void remove_node(ListNode *before, ListNode *node, ListNode *after)
{
  // If before exists, remove reference to this node, then add the reference to the node after this
  // one
  if (before)
    before->npx = xor(xor(before->npx, node), after);

  // If after exists, remove reference to this node, then add the reference to the node before this
  // one
  if (after)
    after->npx = xor(before, xor(node, after->npx));

  // If the node being removed was the head, then the new head will be the node after
  if (node == head)
    head = after;

  // Free the node from the heap as this is no longer needed
  free(node);
  node = NULL;
}

/**
 * Removes the first instance of the value from the list, if found.
 * @param value The value to be removed.
 * @return <code>1</code> if the value was removed, or <code>0</code> if <code>value</code> was not
 * found in the list so could not be removed.
 */
int remove_string(char *value)
{
  NodePosition *position = NULL;
  if (!find(value, &position))
    return 0; // The value wasn't found, so do nothing and return 0

	// Remove the node
  remove_node(position->preceding_node, position->node, position->proceeding_node);

  // Free up the position as this is no longer required
  free(position);

  // We successfully removed a node, so return 1
  return 1;
}

/**
 * Removes the value after the first instance of <code>after</code> in the list.
 * @param after The value of the node preceding the node that should be removed.
 * @param result The value that was removed.
 * @return <code>1</code> if a value was removed, or <code>0</code> if a value was not removed. A
 * value will not be removed if either of the following conditions are met:
 * <ul>
 *   <li>The value <code>after</code> is not in the list.</li>
 *   <li>The first instance of <code>after</code> is at the end of the list.</li>
 * </ul>
 */
int remove_after(const char *after, char *result)
{
  // Find the first instance of the value
  NodePosition *position = NULL;

  // If an instance of the value could not be found, or the node after this one is null then we
  // can't delete anything
  if (!find(after, &position) || !position->proceeding_node)
  {
    if (position)
      free(position);
    return 0;
  }

  // Get the nodes required to delete the node
  ListNode *preceding_node = position->node;
  ListNode *node_to_remove = position->proceeding_node;
  free(position); // No longer required

  // To get the node after the node being removed, we need to continue the sequence to find the next
  // node
  ListNode *proceeding_node = next_node(preceding_node, node_to_remove);

  // Copy the value of the node being removed before it is freed
  strcpy(result, node_to_remove->value);

  // Remove the node
  remove_node(preceding_node, node_to_remove, proceeding_node);

  // We successfully removed the node, so return 1
  return 1;
}

int remove_before(const char *before, char *result)
{
  // If the value could not be found, or the node before this one is null then we can't delete
  // anything
  NodePosition *position = NULL;
  if (!find(before, &position) || !position->preceding_node)
  {
    if (position)
      free(position);
    return 0;
  }

  // Get the nodes required to delete the node
  ListNode *proceeding_node = position->node;
  ListNode *node_to_remove = position->preceding_node;
  free(position); // No longer required

  // To get the node after the node being removed, we need to continue the sequence in reverse to
  // find the previous node
  ListNode *preceding_node = previous_node(node_to_remove, proceeding_node);

  // Copy the value of the node being removed before it is freed
  strcpy(result, node_to_remove->value);

  // Remove the node
  remove_node(preceding_node, node_to_remove, proceeding_node);

  // We successfully removed the node, so return 1
  return 1;
}

/**
 * Prints the list to the console in order, from the head to the tail. Elements are separated by
 * "<code> -> </code>", e.g. <code>head value -> middle value -> tail value</code>
 */
void print_list()
{
  bool first_element = true;

  // Start from the head
  ListNode *preceding_node = NULL;
	ListNode *current_node = head;
	ListNode *proceeding_node;

	// Loop through the list, and continue printing as long as the node we're looking it isn't null.
	// If it is null, we've reached the end of the list and should stop printing.
	while (current_node)
	{
	  // All elements apart from the first element should be preceded by " -> "
	  if (!first_element)
      printf(" -> ");
	  else
	    first_element = false;

	  // Print the value of the node
	  printf("%s", current_node->value);

	  // Update the node references
    proceeding_node = next_node(preceding_node, current_node);
    preceding_node = current_node;
    current_node = proceeding_node;
	}
	printf("\n");
}

int main(int argc, char *argv[])
{
  insert_string("Alpha");
  insert_string("Bravo");
  insert_string("Charlie");
  insert_after("Bravo", "Delta");
  insert_before("Alpha", "Echo");
  print_list(); // Charlie -> Bravo -> Delta -> Echo -> Alpha

  char result[10];
  int ret;

  ret = remove_after("Delta", result);
  if (ret)
    printf("Removed: %s\n", result);

  ret = remove_before("Bravo", result);
  if (ret)
    printf("Removed: %s\n", result);

  ret = remove_string(result);
  if (ret)
    printf("Removed: %s\n", result);

  print_list();
}
