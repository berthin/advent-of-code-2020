#include <iostream>
#include <vector>
#include <algorithm>
#include <unordered_map>
#include <cassert>
#include <cstdint>

using namespace std;

const int N_ROUNDS = (int)1e7;
const int MAX_CUPS = (int)1e6;

struct Cup
{
    int id;
};

struct Node
{
    Cup cup;
    Node* next;
    Node* prev;
};

ostream& operator<<(ostream& os, const Node& node)
{
    os << node.cup.id;
    return os;
}

template <class T=Node>
class CircularBuffer
{
public:
    CircularBuffer(): head(nullptr)
    {}

    void append(T* element)
    {
        if (head == nullptr)
        {
            head = element;
            head->next = head->prev = head;
            return;
        }
        T* tail = head->prev;

        element->prev = tail;
        element->next = head;
        tail->next = element;
        head->prev = element;
    }

    void insert(T* element, T* other)
    {
        other->next = element->next;
        other->prev = element;

        element->next->prev = other;
        element->next = other;
    }

    T* getHead()
    {
        return head;
    }

    T* next(T* element, int k)
    {
        T* answer = element;
        for (int i = 0; i < k; ++i) answer = answer->next;
        return answer;
    }

    void moveHeadTo(T* element)
    {
        head = element;
    }

    void print()
    {
        T* cur = head;

        int k = 0;
        do
        {
            cout << (*cur) << " " ;
            cur = cur->next;
            ++k;
            if (k > MAX_CUPS) assert(false);
        } while (cur != head);
        cout << endl;
    }

    T* pop()
    {
        T* curr = head;
        head = head->next;

        curr->prev->next = head;
        head->prev = curr->prev;

        return curr;
    }

private:
    T* head;
};


int nCups = 0;

Cup* cups[MAX_CUPS+1];
Node nodes[MAX_CUPS+1];

CircularBuffer<Node> buffer;

void addCup(int id)
{
    nodes[nCups].cup.id = id;
    cups[id] = &nodes[nCups].cup;

    ++nCups;
}

int findCupId(int first, vector<int>& threeId, int N)
{
    int id = first;
    do
    {
        --id;
    }
    while (id > 0 && (id == threeId[0] || id == threeId[1] || id == threeId[2]));

    if (id != 0) return id;

    id = N;
    while (id == first || id == threeId[0] || id == threeId[1] || id == threeId[2]) --id;
    return id;
}

Node* findDestination(Node* first, vector<Node*> three, int N)
{
    vector<int> threeId = {three[0]->cup.id, three[1]->cup.id, three[2]->cup.id};
    auto cupId = findCupId(first->cup.id, threeId, N);
    auto* cup = cups[cupId];
    return reinterpret_cast<Node*>(cup);
}

void f(int n_rounds)
{
    for (int round = 0; round < n_rounds; ++round)
    {
        Node* first = buffer.pop();
        vector<Node*> three(3);
        for (int i = 0; i < 3; ++i)
            three[i] = buffer.pop();

        Node* newHead = three[2]->next;
        Node* dest = findDestination(first, three, nCups);

        buffer.insert(buffer.getHead()->prev, first);
        buffer.insert(dest, three[0]);
        buffer.insert(three[0], three[1]);
        buffer.insert(three[1], three[2]);

        buffer.moveHeadTo(newHead);
    }
}

void buildCups(string& label)
{
    for (char ch: label) addCup(ch - '0');
    for (int id = label.size() + 1; id <= MAX_CUPS; ++id) addCup(id);

    for (int i = 0; i < MAX_CUPS; ++i) buffer.append(&nodes[i]);
}

int main()
{
    string label = "";
    label = "389125467";
    label = "586439172";
    buildCups(label);
    f(N_ROUNDS);

    Node* cup = reinterpret_cast<Node*>(cups[1]);
    Node* next1 = cup->next;
    Node* next2 = next1->next;

    cout << " [" << *cup << "] " << *next1 << " " << *next2 << endl;

    cout << "answer " << (((uint64_t)next1->cup.id) * ((uint64_t)next2->cup.id)) << endl;
    return 0;
}
