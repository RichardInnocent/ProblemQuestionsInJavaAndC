#ifndef CWK2Q3_H
#define CWK2Q3_H

int hash_function(const char*);
void resize_map(int new_size);
void add_to_map(const char *name);
static void add_to_map_without_resizing(const char *name);
static int next_index(int);
int remove_from_map(const char *name);
int search_map(const char *name);
static int index_of(const char*);
static void print_value_at_index(int);

#endif // CWK2Q3_H
