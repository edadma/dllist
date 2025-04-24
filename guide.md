# DLList Complete Reference Guide

## Table of Contents
1. [Introduction](#introduction)
2. [DLList Class](#dllist-class)
3. [Node Class](#node-class)
4. [Sentinel Class](#sentinel-class)
5. [DLIndexedList Class](#dlindexedlist-class)
6. [NodeRef Trait](#noderef-trait)
7. [Usage Patterns](#usage-patterns)

## Introduction

This reference documents every method and property of the `dllist` library, a high-performance doubly-linked list implementation for Scala. It includes two main classes: `DLList` and its extension `DLIndexedList`.

Key concepts to remember:
- Nodes are never `null` - sentinel nodes are used instead
- Forward traversal checks `isAfterEnd` to detect the end
- Backward traversal checks `isBeforeStart` to detect the start

## DLList Class

`DLList[T]` is a mutable doubly-linked list with direct node access.

### Constructors and Factory Methods

```scala
// Empty constructor
val list = new DLList[T]()

// Factory method with initial elements
val list = DLList[T](elements: T*)
```

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `startSentinel` | `Sentinel` | Sentinel node at the start of the list |
| `endSentinel` | `Sentinel` | Sentinel node at the end of the list |
| `count` | `Int` (protected) | Number of elements in the list |

### Collection Methods (from AbstractBuffer)

| Method | Signature | Description |
|--------|-----------|-------------|
| `addOne` | `(elem: T): DLList.this.type` | Adds element to end, returns this list |
| `prepend` | `(elem: T): DLList.this.type` | Adds element to beginning, returns this list |
| `apply` | `(n: Int): T` | Returns element at index n |
| `clear` | `(): Unit` | Removes all elements |
| `iterator` | `(): Iterator[T]` | Returns iterator over elements |
| `length` | `(): Int` | Returns number of elements |
| `patchInPlace` | `(from: Int, patch: IterableOnce[T], replaced: Int): DLList.this.type` | Replaces elements (not implemented) |
| `insert` | `(idx: Int, elem: T): Unit` | Inserts element at specified index |
| `insertAll` | `(n: Int, elems: IterableOnce[T]): Unit` | Inserts multiple elements at index |
| `remove` | `(idx: Int): T` | Removes and returns element at index |
| `remove` | `(idx: Int, count: Int): Unit` | Removes range of elements |
| `update` | `(idx: Int, newelem: T): Unit` | Updates element at index |

### Node Access Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `appendElement` | `(elem: T): Node` | Adds element to end, returns its node |
| `headNode` | `(): Node` | Returns first node (throws if empty) |
| `headNodeOption` | `(): Option[Node]` | Returns first node as Option |
| `lastNode` | `(): Node` | Returns last node (throws if empty) |
| `lastNodeOption` | `(): Option[Node]` | Returns last node as Option |
| `node` | `(n: Int): Node` | Returns node at index n |
| `nodeIterator` | `(): Iterator[Node]` | Returns iterator over nodes |
| `reverseNodeIterator` | `(): Iterator[Node]` | Returns reverse iterator over nodes |
| `nodeFind` | `(p: T => Boolean): Option[Node]` | Finds first node matching predicate |
| `reverseNodeFind` | `(p: T => Boolean): Option[Node]` | Finds last node matching predicate |
| `prependElement` | `(elem: T): Node` | Adds element to beginning, returns its node |

### Override Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `head` | `(): T` | Returns first element |
| `last` | `(): T` | Returns last element |
| `reverseIterator` | `(): Iterator[T]` | Returns reverse iterator over elements |
| `toString` | `(): String` | Returns string representation |

### Protected Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `_insertAll` | `(n: Int, elems: IterableOnce[T]): Node` | Internal helper for insertAll, returns first node |

## Node Class

`Node` represents an element in the list with links to adjacent nodes.

### Constructors

```scala
// Standard constructor (typically used internally)
new Node(prev: Node, next: Node, init: T)

// Sentinel constructor (used internally)
new Node() // Creates empty sentinel node
```

### Properties and Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `element` | `(): T` | Gets the stored element |
| `element_=` | `(value: T): Unit` | Updates the stored element |
| `following` | `(): Node` | Gets the next node |
| `preceding` | `(): Node` | Gets the previous node |
| `isBeforeStart` | `(): Boolean` | Checks if this is the start sentinel |
| `notBeforeStart` | `(): Boolean` | Checks if this is not the start sentinel |
| `isAfterEnd` | `(): Boolean` | Checks if this is the end sentinel |
| `notAfterEnd` | `(): Boolean` | Checks if this is not the end sentinel |
| `unlink` | `(): T` | Removes this node from list, returns its element |
| `follow` | `(v: T): Node` | Inserts new node after this one, returns the new node |
| `precede` | `(v: T): Node` | Inserts new node before this one, returns the new node |
| `unlinkUntil` | `(node: Node): Seq[Any]` | Removes nodes from this to (not including) target |
| `isBefore` | `(node: Node): Boolean` | Checks if this comes before the given node |
| `isAfter` | `(node: Node): Boolean` | Checks if this comes after the given node |
| `iteratorUntil` | `(last: Node): Iterator[Node]` | Creates iterator from this to (not including) last |
| `iterator` | `(): Iterator[Node]` | Creates iterator from this to the end |
| `reverseIteratorUntil` | `(last: Node): Iterator[Node]` | Creates reverse iterator to (not including) last |
| `reverseIterator` | `(): Iterator[Node]` | Creates reverse iterator from this to the start |
| `find` | `(p: T => Boolean): Option[Node]` | Finds first node from here with matching element |
| `reverseFind` | `(p: T => Boolean): Option[Node]` | Finds first node before here with matching element |
| `index` | `(): Int` | Returns index of this node or -1 if not found |
| `skipForward` | `(n: Int): Node` | Moves n positions forward |
| `skipReverse` | `(n: Int): Node` | Moves n positions backward |
| `toString` | `(): String` | Returns string representation |

### Protected/Private Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `checkUnlink` | `(): Unit` | Validates node can be unlinked |

## Sentinel Class

`Sentinel` extends `Node` and is used to mark list boundaries.

### Constructor

```scala
// Used internally by DLList
new Sentinel(name: String)
```

### Properties and Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `name` | `String` | Name of this sentinel ("start sentinel" or "end sentinel") |
| `element` | `(): T` | Throws error - sentinels have no element |
| `element_=` | `(v: T): Unit` | Throws error - sentinels have no element |
| `iterator` | `(): Iterator[Node]` | Returns iterator if end sentinel, throws for start |
| `reverseIterator` | `(): Iterator[Node]` | Returns reverse iterator if start sentinel, throws for end |
| `toString` | `(): String` | Returns sentinel name |

## DLIndexedList Class

`DLIndexedList[T]` extends `DLList[T]` with O(1) indexed access.

### Constructors and Factory Methods

```scala
// Empty constructor
val list = new DLIndexedList[T]()

// Factory method with initial elements
val list = DLIndexedList[T](elements: T*)
```

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `array` | `ArrayBuffer[Node]` | Protected buffer of node references |

### Overridden Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `clear` | `(): Unit` | Clears list and index array |
| `appendElement` | `(elem: T): Node` | Adds to end and updates index |
| `node` | `(n: Int): Node` | Gets node at index with O(1) complexity |
| `prependElement` | `(elem: T): Node` | Adds to beginning and updates index |
| `insertAll` | `(n: Int, elems: IterableOnce[T]): Unit` | Inserts elements and updates index |
| `remove` | `(n: Int): T` | Removes element and updates index |
| `toString` | `(): String` | Returns string representation |

## NodeRef Trait

`NodeRef` is a trait for objects that need to reference their node in a list.

### Methods

| Method | Signature | Description |
|--------|-----------|-------------|
| `ref` | `(node: DLList[Any]#Node): Unit` | Called when object is added to list |

## Usage Patterns

### Safe Traversal

```scala
// Forward traversal
var node = list.headNode
while (node.notAfterEnd) {
  // Process node.element
  node = node.following
}

// Backward traversal
var node = list.lastNode  
while (node.notBeforeStart) {
  // Process node.element
  node = node.preceding
}
```

### Safe Empty List Handling

```scala
// Using node options
list.headNodeOption.foreach { node =>
  // Process first node
}

// Manual check
if (list.nonEmpty) {
  val node = list.headNode
  // Safe to use node
}
```

### Direct Node Manipulation

```scala
// Get node reference
val node = list.node(3)

// Insert elements around it
val newNext = node.follow(42)
val newPrev = node.precede(41)

// Remove the node
val value = node.unlink

// Check position relative to other nodes
if (nodeA.isBefore(nodeB)) {
  // nodeA comes before nodeB
}
```

### NodeRef Implementation

```scala
class TrackedItem(val value: Int) extends NodeRef {
  private var myNode: DLList[Any]#Node = _
  
  // Called when added to list
  infix def ref(node: DLList[Any]#Node): Unit = {
    myNode = node
  }
  
  // Methods that use the node reference
  def removeFromList(): Unit = {
    if (myNode != null) {
      myNode.unlink
      myNode = null
    }
  }
  
  def getNext[T](): Option[T] = {
    if (myNode != null && myNode.following.notAfterEnd) {
      Some(myNode.following.element.asInstanceOf[T])
    } else {
      None
    }
  }
}
```
