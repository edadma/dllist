# Guide to the `dllist` Library

## Table of Contents

1. [Introduction](#introduction)
2. [Core Classes](#core-classes)
3. [Basic Operations](#basic-operations)
4. [Advanced Node Operations](#advanced-node-operations)
5. [Node References](#node-references)
6. [Performance Characteristics](#performance-characteristics)
7. [Implementation Details](#implementation-details)
8. [Common Patterns and Idioms](#common-patterns-and-idioms)
9. [Gotchas and Edge Cases](#gotchas-and-edge-cases)
10. [Cross-Platform Considerations](#cross-platform-considerations)

## Introduction

The `dllist` library provides a high-performance doubly-linked list implementation for Scala. It offers two primary data structures:

- `DLList`: A doubly-linked list with standard operations
- `DLIndexedList`: An extension that adds array-backed indexing for fast random access

These structures are designed for scenarios where you need:
- Efficient insertions and deletions at any position
- Both forward and backward traversal
- Direct manipulation of list nodes
- Reference tracking of contained elements

## Core Classes

### DLList

`DLList[T]` is the foundation of the library:

```scala
// Import
import io.github.edadma.dllist._

// Create an empty list
val emptyList = new DLList[Int]

// Create a list with initial elements
val list = DLList(1, 2, 3)
```

**Key Components:**

- `class Node`: Represents a node in the list with links to previous and next nodes
- `class Sentinel`: Special nodes that mark the start and end of the list
- `startSentinel` and `endSentinel`: Boundary nodes that simplify edge case handling

### DLIndexedList

`DLIndexedList[T]` extends `DLList[T]` with array-backed indexing:

```scala
// Create an empty indexed list
val emptyIndexedList = new DLIndexedList[String]

// Create an indexed list with initial elements
val indexedList = DLIndexedList("a", "b", "c")
```

It maintains an internal array buffer (`ArrayBuffer[Node]`) that enables O(1) access to any node by index.

## Basic Operations

### Adding Elements

```scala
val list = DLList[Int]()

// Append element (returns this)
list += 1          // [1]

// Prepend element (returns this)
list.prepend(0)    // [0, 1]

// Append multiple elements
list ++= List(2, 3, 4)  // [0, 1, 2, 3, 4]

// Insert at specific position
list.insert(2, 5)  // [0, 1, 5, 2, 3, 4]

// Insert multiple elements at position
list.insertAll(3, List(6, 7))  // [0, 1, 5, 6, 7, 2, 3, 4]
```

### Accessing Elements

```scala
val list = DLList(0, 1, 2, 3, 4)

// Get element by index
val second = list(1)  // 1

// Get first element
val first = list.head  // 0

// Get last element
val last = list.last   // 4

// Iterate forward
for (elem <- list) println(elem)

// Iterate backward
for (elem <- list.reverseIterator) println(elem)

// Get length
val size = list.length  // 5
```

### Removing Elements

```scala
val list = DLList(0, 1, 2, 3, 4)

// Remove element at index
val removed = list.remove(2)  // Removes and returns 2
// list is now [0, 1, 3, 4]

// Remove range of elements
list.remove(1, 2)  // Removes elements at indices 1 and 2
// list is now [0, 4]

// Clear the list
list.clear()  // []
```

### Updating Elements

```scala
val list = DLList(0, 1, 2, 3, 4)

// Update element at index
list(2) = 5  // [0, 1, 5, 3, 4]
```

## Advanced Node Operations

### Working with Nodes Directly

```scala
val list = DLList(0, 1, 2, 3, 4)

// Get node by index
val node = list.node(2)  // Node containing 2

// Get head node
val firstNode = list.headNode

// Get last node
val lastNode = list.lastNode

// Get element from node
val value = node.element  // 2

// Set element in node
node.element = 5  // [0, 1, 5, 3, 4]

// Get next node
val nextNode = node.following

// Get previous node
val prevNode = node.preceding
```

### Node Traversal

```scala
val list = DLList(0, 1, 2, 3, 4)
val node = list.node(1)  // Node containing 1

// Skip forward by n nodes
val skippedNode = node.skipForward(2)  // Node containing 3

// Skip backward by n nodes
val backNode = node.skipReverse(1)  // Node containing 0

// Check if node is at start/end
val isStart = node.isBeforeStart  // false
val isEnd = node.isAfterEnd       // false

// Check if one node comes before another
val before = node.isBefore(skippedNode)  // true
```

### Modifying the List through Nodes

```scala
val list = DLList(0, 1, 2, 3, 4)
val node = list.node(2)  // Node containing 2

// Insert element after node
val afterNode = node.follow(2.5)  // [0, 1, 2, 2.5, 3, 4]

// Insert element before node
val beforeNode = node.precede(1.5)  // [0, 1, 1.5, 2, 2.5, 3, 4]

// Remove node from list
node.unlink  // [0, 1, 1.5, 2.5, 3, 4]
```

### Node Finding

```scala
val list = DLList(0, 1, 2, 3, 4)

// Find first node matching predicate
val evenNode = list.nodeFind(_ % 2 == 0)  // Some(Node containing 0)

// Find last node matching predicate
val lastEvenNode = list.reverseNodeFind(_ % 2 == 0)  // Some(Node containing 4)

// Find from specific node
val node = list.node(2)
val foundNode = node.find(_ > 3)  // Some(Node containing 4)
```

### Node Removal with Range

```scala
val list = DLList(0, 1, 2, 3, 4, 5)
val startNode = list.node(1)  // Node containing 1
val endNode = list.node(4)    // Node containing 4

// Remove range of nodes between startNode and endNode (exclusive)
val removed = startNode.unlinkUntil(endNode)  // [1, 2, 3]
// list is now [0, 4, 5]
```

### Node Iteration

```scala
val list = DLList(0, 1, 2, 3, 4)
val startNode = list.node(1)  // Node containing 1

// Iterate from node to end
val nodeIterator = startNode.iterator
while (nodeIterator.hasNext) {
  val node = nodeIterator.next()
  println(node.element)  // Prints 1, 2, 3, 4
}

// Iterate from node backward
val reverseIterator = startNode.reverseIterator
while (reverseIterator.hasNext) {
  val node = reverseIterator.next()
  println(node.element)  // Prints 1, 0
}

// Iterate from node to specific node (exclusive)
val endNode = list.node(3)  // Node containing 3
val rangeIterator = startNode.iteratorUntil(endNode)
while (rangeIterator.hasNext) {
  val node = rangeIterator.next()
  println(node.element)  // Prints 1, 2
}
```

## Node References

The `NodeRef` trait allows objects to track their own position in a list:

```scala
class TrackableItem(var value: String) extends NodeRef {
  private var myNode: DLList[Any]#Node = _
  
  // Called automatically when added to list
  def ref(node: DLList[Any]#Node): Unit = {
    myNode = node
  }
  
  // Self-removal method
  def removeFromList(): Unit = {
    if (myNode != null) {
      myNode.unlink
      myNode = null
    }
  }
  
  // Update value and notify list
  def updateValue(newValue: String): Unit = {
    value = newValue
    if (myNode != null) {
      myNode.element = this
    }
  }
}

val list = DLList[TrackableItem]()
val item = new TrackableItem("hello")
list += item

// Later, the item can remove itself
item.removeFromList()
```

This is particularly useful for game entities, UI elements, or any object that needs to know its position in a collection.

## Performance Characteristics

### Time Complexity

| Operation | DLList | DLIndexedList |
|-----------|--------|--------------|
| Append/Prepend | O(1) | O(1) |
| Insert at position with node reference | O(1) | O(1) |
| Insert at index | O(n) | O(n) |
| Access by index | O(n) | O(1) |
| Remove by node reference | O(1) | O(1) |
| Remove by index | O(n) | O(n) |
| Find element | O(n) | O(n) |
| Clear | O(1) | O(1) |
| Size | O(1) | O(1) |

### Memory Overhead

- `DLList`: 2 references per element (prev, next) + element
- `DLIndexedList`: Same as `DLList` + 1 reference per element in the array buffer

### When to Use Each Implementation

- Use `DLList` when:
    - You primarily access elements sequentially
    - You frequently insert/remove elements at both ends
    - You hold references to nodes for direct manipulation
    - Memory efficiency is important

- Use `DLIndexedList` when:
    - You need frequent random access by index
    - The additional memory overhead is acceptable
    - You perform many index-based operations

## Implementation Details

### Sentinel Nodes

The implementation uses sentinel nodes at both ends of the list to simplify boundary condition handling:

- `startSentinel`: Marks the beginning of the list, has no previous node
- `endSentinel`: Marks the end of the list, has no next node

This approach eliminates many null checks and special cases in the code.

### Node Linking Logic

When nodes are inserted or removed, the linking logic is:

1. For insertion between nodes A and B:
   ```
   New.prev = A
   New.next = B
   A.next = New
   B.prev = New
   ```

2. For removal of node N:
   ```
   N.prev.next = N.next
   N.next.prev = N.prev
   N.prev = null  // Clean up references
   N.next = null
   ```

### Array Synchronization in DLIndexedList

`DLIndexedList` maintains the array buffer in sync with the linked list by:
- Adding nodes to the array when they're added to the list
- Removing nodes from the array when they're removed from the list
- Keeping the array order matching the list order

### NodeRef Implementation

When an element implementing `NodeRef` is added to the list:
1. The list creates a new node containing the element
2. Before linking the node, it checks if the element is a `NodeRef`
3. If so, it calls the element's `ref` method with the newly created node
4. This gives the element a reference to its containing node

## Common Patterns and Idioms

### Using Option for Safe Node Access

```scala
val list = DLList(1, 2, 3)

// Safe head/last access
val firstOpt = list.headNodeOption
val lastOpt = list.lastNodeOption

// Using Option for node finding
list.nodeFind(_ > 5) match {
  case Some(node) => println(s"Found: ${node.element}")
  case None => println("Not found")
}
```

### List as a Queue or Stack

```scala
// Queue operations
val queue = DLList[String]()
queue += "first"   // Enqueue
queue += "second"
val item = queue.head  // Peek
queue.remove(0)        // Dequeue

// Stack operations
val stack = DLList[Int]()
stack.prepend(1)  // Push
stack.prepend(2)
val top = stack.head  // Peek
stack.remove(0)       // Pop
```

### In-place List Manipulation

```scala
val list = DLList(1, 2, 3, 4, 5)

// Remove elements based on predicate
var current = list.headNode
while (current != null && current.notAfterEnd) {
  val next = current.following
  if (current.element % 2 == 0) {
    current.unlink
  }
  current = next
}
// list is now [1, 3, 5]
```

### Combining with Scala Collections

```scala
val list = DLList(1, 2, 3, 4, 5)

// Convert to standard collections
val stdList = list.toList
val array = list.toArray
val seq = list.toSeq

// Use with standard collection operations
val sum = list.sum
val doubledList = list.map(_ * 2)
val evenCount = list.count(_ % 2 == 0)
```

## Gotchas and Edge Cases

### Empty List Handling

```scala
val list = DLList[Int]()

// These throw exceptions on empty lists
try {
  val head = list.head        // NoSuchElementException
  val firstNode = list.headNode  // IllegalArgumentException
} catch {
  case e: Exception => println(e.getMessage)
}

// Safe alternatives
val headOpt = if (list.isEmpty) None else Some(list.head)
val headNodeOpt = list.headNodeOption
```

### Sentinel Node Confusion

```scala
val list = DLList(1, 2, 3)
val firstNode = list.headNode

// Checking if we're at a sentinel
if (firstNode.isBeforeStart) println("At start sentinel")
if (firstNode.isAfterEnd) println("At end sentinel")

// Don't try to access element of a sentinel
// This will throw an error
try {
  val startElem = list.startSentinel.element
} catch {
  case e: Exception => println(e.getMessage)
}
```

### Node Invalidation

```scala
val list = DLList(1, 2, 3)
val node = list.node(1)  // Node with element 2

// If you unlink a node, it becomes orphaned
node.unlink
// After this, using node.following or node.preceding is undefined behavior
```

### Concurrent Modification

The library doesn't protect against concurrent modification:

```scala
val list = DLList(1, 2, 3, 4, 5)

// This may cause problems
for (elem <- list) {
  if (elem % 2 == 0) {
    list.remove(list.indexOf(elem))  // Modifying while iterating!
  }
}

// Instead, collect indices first
val indicesToRemove = list.zipWithIndex.collect {
  case (elem, idx) if elem % 2 == 0 => idx
}.reverse  // Remove from end to avoid shifting indices

for (idx <- indicesToRemove) {
  list.remove(idx)
}
```

## Cross-Platform Considerations

The library is cross-compiled for:
- Scala JVM
- Scala.js
- Scala Native

### Platform-Specific Notes

**Scala JVM:**
- Standard behavior, no special considerations

**Scala.js:**
- Performance characteristics may differ from JVM
- Reference equality using `eq`/`ne` works as expected

**Scala Native:**
- May have different performance characteristics
- Manual memory management implications

### Build Configuration

For SBT:

```scala
// For JVM only
libraryDependencies += "io.github.edadma" %% "dllist" % "0.0.1"

// For Scala.js
libraryDependencies += "io.github.edadma" %%% "dllist" % "0.0.1"

// For Scala Native
libraryDependencies += "io.github.edadma" %%% "dllist" % "0.0.1"
```

## Troubleshooting Common Issues

### List Corruption

If list traversal doesn't work as expected:

1. Check that you're not using unlinked nodes
2. Verify sentinel nodes are properly connected
3. Ensure `count` is correctly incremented/decremented
4. For `DLIndexedList`, check array synchronization

### Memory Leaks

Potential causes:

1. Orphaned nodes still holding references to objects
2. `NodeRef` implementations maintaining circular references

### Performance Problems

If experiencing poor performance:

1. Use `DLIndexedList` for frequent random access
2. Cache node references when doing repeated operations
3. Use direct node manipulation instead of index-based operations when possible
4. For large lists, consider batching operations

### IndexOutOfBoundsException

Common causes:

1. Accessing empty list elements
2. Using invalid indices
3. Not checking bounds before operations

### Fixed Patches for Current Issues in codebase:

1. **Fix for insertAll in DLIndexedList**:
   ```scala
   override def insertAll(n: Int, elems: IterableOnce[T]): Unit = {
     val iter = elems.iterator
     if (iter.nonEmpty) {
       val firstNode = super._insertAll(n, elems)
       // Build a list of all inserted nodes
       val insertedNodes = new mutable.ListBuffer[Node]
       var current = firstNode
       while (current ne firstNode.prev.next) {
         insertedNodes += current
         current = current.following
       }
       array.insertAll(n, insertedNodes)
     }
   }
   ```

2. **Fix for patchInPlace implementation**:
   ```scala
   def patchInPlace(from: Int, patch: IterableOnce[T], replaced: Int): DLList.this.type = {
     require(from >= 0, s"Invalid 'from' index: $from < 0")
     require(replaced >= 0, s"Invalid 'replaced' count: $replaced < 0")
     
     if (isEmpty && from == 0 && replaced == 0) {
       appendAll(patch)
       return this
     }
     
     val actualReplaceCount = math.min(replaced, math.max(0, length - from))
     if (actualReplaceCount > 0) {
       remove(from, actualReplaceCount)
     }
     
     val patchIterator = patch.iterator
     if (patchIterator.nonEmpty) {
       if (isEmpty) {
         appendAll(patch)
       } else if (from >= length) {
         appendAll(patch)
       } else {
         insertAll(from, patch)
       }
     }
     
     this
   }
   ```

3. **Improved node method with tail recursion**:
   ```scala
   def node(n: Int): Node = {
     require(0 <= n && n < count, s"node index out of range: $n")

     @annotation.tailrec
     def findFromStart(current: Node, remaining: Int): Node =
       if (remaining == 0) current
       else findFromStart(current.next, remaining - 1)
       
     @annotation.tailrec
     def findFromEnd(current: Node, remaining: Int): Node =
       if (remaining == 0) current
       else findFromEnd(current.prev, remaining - 1)
       
     if (n <= count / 2)
       findFromStart(startSentinel.next, n)
     else
       findFromEnd(endSentinel.prev, count - 1 - n)
   }
   ```
