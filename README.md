# Graded Assignment 2: Problem Questions in Java and C
This is a piece of coursework I wrote for CM50275 Advanced Programming at the University of Bath. This was a series of questions that should be answered in Java or C.
## Brief

### Question 1 (Java):
Implement the Shellsort algorithm (https://en.wikipedia.org/wiki/Shellsort) for an array of up to 1,000 signed doubles in Java. Your solution must use concrete gaps of `[1, 3, 7, 15,
31, 63, 127, 255, 511]`. Your solution must print the (partially) sorted array after each gap on a new line in the form:
```[a0, a1, a2, ..., an]```
Where an is the nth element in the (partially) sorted array (please note the space after the
commas), and each element should be formatted to 2 decimal places (e.g. `1.00`).

### Question 2 (Java):
Implement interpolation search for a list of `String`s in Java. The method should return the position in the array if the string is present, or `-1` if it is not present.

### Question 3 (C\):
Implement your own Hash Table in C for storing and searching names, i.e. char arrays. Each name stored in the hash table should be unique. In the event of collisions, you should use linear probing with an interval of 1. Your Hash Table implementation should have the following interface:

`int hash_function(const char *key)` - the hash function should be the sum of the
ASCII values of the key string modulo the size of the underlying data structure.

`void resize_map(int new_size)` – resizes the underlying data structure. The hash
map should also resize when the load factor is greater than 0.7.

`void add_to_map(const char *name)` – adds the string to the hash map.

`int remove_from_map(const char *name)` – removes the string from the hash map
if it is present. Returns true for success, false if the string can not be found.

`int search_map(const char *name)` – searches the hash map for the given string.
Returns true for success, false if the string can not be found.

`void print_map()` – prints the hash map (you may choose how you wish to format this).

### Question 4 (C\)
Implement your own XOR Linked List (https://en.wikipedia.org/wiki/XOR_linked_list) in C capable of storing names. Strings (including null terminator) should not be added to the linked list if they exceed 64 bytes in length. Your implementation should have the following functions:

`void insert_string(const char* newObj)` - Inserts the string at the beginning of the XOR linked list.

`int insert_before(const char* before, const char* newObj)` - If possible, inserts before the string "before" and returns true. Returns false if not possible (e.g., the before string is not in the list).

`int insert_after(const char* after, const char* newObj)` - If possible, inserts after the string "after" and returns true. Returns false if not possible (e.g., the after string is not in the list).

`int remove_string(char* result)` - If possible removes the string at the beginning of the XOR Linked list and returns its value in result. If successful return true, otherwise returns false.

`int remove_after(const char *after, char *result)` - If possible, removes the string after the string ”after” and fills in the result buffer with its value. If successful return true, otherwise returns false.

`int remove_before(const char *before, char *result)` - If possible, removes the string before the string ”before” and fills in the result buffer with its value. If successful return true, otherwise returns false.

`void print_list()` - Prints the contents of the list in order.

### Question 5 (C\):
Implement an algorithm in C which given a file containing a block of text as input, redacts all words from a given set of "redactable" words (also from a file), and outputs the result to a file called “result.txt”. For example, given the block of text:

> The quick brown fox jumps over the lazy dog

and the redactable set of words: the, jumps, lazy

the output should be
> *** quick brown fox ***** over *** **** dog

Note that the number of stars in the redacted text is the same as the number of letters in the
word that has been redacted, and that capitalization is ignored. You must not use any of the string libraries to answer this question – if you do you will lose all marks for correctness. You may develop your own versions of the functions in these libraries (e.g. `strlen`, `strcmp`). You should also test your program using the example files provided.

### Question 6 (Java):
Implement in Java a similar algorithm to that in Q5, i.e. given a block of text your algorithm should be able to redact words from a given set and outputs the result to a file called “result.txt”. However, in this implementation of the algorithm all redactable words will be proper nouns (i.e. a name used for an individual person, place, or organisation, spelled with an initial capital letter) and your algorithm should take into account that the list of redactable words might not be complete. For example, given the block of text:
> It was in July, 1805, and the speaker was the well-known
Anna Pavlovna Scherer, maid of honor and favorite of the
Empress Marya Fedorovna. With these words she greeted
Prince Vasili Kuragin, a man of high rank and importance,
who was the first to arrive at her reception. Anna Pavlovna
had had a cough for some days. She was, as she said,
suffering from la grippe; grippe being then a new word
in St. Petersburg, used only by the elite.

and the redactable set of words:
Anna Pavlovna Scherer, St. Petersburg, Marya Fedorovna

the output text should be:
> It was in ****, 1805, and the speaker was the well-known
**** ******** *******, maid of honor and favorite of the
******* ***** *********. With these words she greeted
****** ****** *******, a man of high rank and importance,
who was the first to arrive at her reception. **** ********
had had a cough for some days. She was, as she said,
suffering from la grippe; grippe being then a new word
in *** **********, used only by the elite.

You should test your program using the example files provided.

### 2.1.7 Question 7 (C\):
Implement a Columnar Transposition Cipher in C to encrypt a message of any length. A Columnar Transposition Cipher is transposition cipher that follows a simple rule for mixing up the characters in the plaintext to form the ciphertext.

As an example, to encrypt the message ATTACK AT DAWN with the keyword KEYS, we first
write out our message as shown below (note we remove the spaces):

```
KEYS
----
ATTA
CKAT
DAWN
```

Note: if the message to encode does not fit into the grid, you should pad the message with x’s
or random characters for example, `ATTACK NOW` with the keyword `KEYS` might look like below:

```
KEYS
----
ATTA
CKNO
WXXX
```

Once you have constructed your table, the columns are now reordered such that the letters in
the keyword are ordered alphabetically:
```
EKSY
----
TAAT
KCTA
ADNW
```

The ciphertext is now read off along the columns, so in our example above, the ciphertext is
`TAATKCTAADNW`. You can test your implementation by encrypting the file in the folder Q7 using
the keyword - `LOVELACE`.


### Question 8 (Java):
You are given the following information, but you may prefer to do some research for yourself.
1. January 1st 1900 was a Monday.
2. Thirty days has September, April, June and November. All the rest have thirty-one, saving February alone, which has twenty-eight, rain or shine. And on leap years, twenty-nine.
3. A leap year occurs on any year evenly divisible by 4, but not on a century unless it is divisible by 400.

How many Tuesdays fell on the first of the month during the twentieth century (1 Jan 1901 to 31 Dec 2000)? Note, this problem is inspired by Project Euler so, as stated in the rules of Project Euler, your solution should return an answer under 60 seconds.