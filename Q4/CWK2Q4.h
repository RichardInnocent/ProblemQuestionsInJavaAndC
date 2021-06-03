//
// Created by richa on 08/05/2021.
//

#ifndef CWK2Q4_H
#define CWK2Q4_H

typedef struct ListNode ListNode;
typedef struct NodePosition NodePosition;

static ListNode *xor(ListNode*, ListNode*);
static ListNode *next_node(ListNode*, ListNode*);
static ListNode *previous_node(ListNode*, ListNode*);
static bool find(const char*, NodePosition**);
static void insert_between_nodes(ListNode*, const char*, ListNode*);
int insert_before(const char*, const char*);
int insert_after(const char*, const char*);
void insert_string(const char*);
static void remove_node(ListNode*, ListNode*, ListNode*);
int remove_string(char*);
int remove_after(const char*, char*);
int remove_before(const char*, char*);
void print_list();

#endif //CWK2Q4_H
