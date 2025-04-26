# dllist Programmer's Guide

This guide provides an overview and reference for the `dllist` library, designed to be concise yet complete enough for AI-assisted code generation and understanding.

---

## Overview

`dllist` is a high-performance mutable doubly-linked list implementation for Scala 3, cross-compiled for JVM, Scala.js, and Native platforms. It offers two primary data structures:

- `DLList`: A mutable doubly-linked list with sentinel nodes and O(1) operations for adding/removing at both ends as well as arbitrary positions when you have a node reference.
- `DLIndexedList`: Extends `DLList` by maintaining an array buffer of node references for O(1) indexed access.

Additional features include forward/backward traversal, direct node manipulation, and optional node referencing via `DLListNodeRef`.

---

## Installation

Add the dependency to your `build.sbt`:

```scala
libraryDependencies += "io.github.edadma" %%% "dllist" % "0.0.2"
```

Ensure you have the appropriate Scala.js and native plugins if targeting those platforms.

---

## Core Concepts

- **Sentinel Nodes:** `DLList` uses `startSentinel` and `endSentinel` to simplify boundary handling, avoiding null checks.
- **Node (`DLListNode`):** Wraps each element, providing O(1) insert/unlink operations when you hold a reference to the node.
- **Indexing Layer:** `DLIndexedList` maintains an `ArrayBuffer` of nodes to enable constant-time random access by index.

---

## DLList

### Class Signature

```scala
class DLList[T] extends scala.collection.mutable.AbstractBuffer[T]
```

### Key Methods

| Method                                            | Description                                                     |
|---------------------------------------------------|-----------------------------------------------------------------|
| `appendElement(elem: T): DLListNode[T]`           | Append `elem` to the end; returns the new node.                |
| `prependElement(elem: T): DLListNode[T]`          | Prepend `elem` to the start; returns the new node.             |
| `headNode`, `lastNode`: DLListNode[T]             | Retrieve first/last node (throws if empty).                    |
| `headNodeOption`, `lastNodeOption`: Option[...]   | Safe variants returning `None` if empty.                        |
| `node(n: Int): DLListNode[T]`                     | Get node at index (bidirectional scan).                        |
| `apply(n: Int): T`                                | Element at index `n`.                                          |
| `insert(idx: Int, elem: T): Unit`                 | Insert `elem` at position `idx`.                                |
| `insertAll(idx: Int, elems: IterableOnce[T]): Unit` | Bulk insert before `idx`.                                       |
| `remove(idx: Int): T`                             | Remove element at `idx`, returning it.                          |
| `remove(idx: Int, cnt: Int): Unit`                | Remove `cnt` elements from `idx`.                               |
| `clear(): Unit`                                   | Remove all elements.                                            |
| `iterator: Iterator[T]`                           | Forward traversal.                                              |
| `reverseIterator: Iterator[T]`                    | Backward traversal.                                             |
| `nodeIterator: Iterator[DLListNode[T]]`           | Forward node-level traversal.                                   |
| `reverseNodeIterator: Iterator[DLListNode[T]]`    | Backward node-level traversal.                                  |
| `nodeFind(p: T => Boolean): Option[...]`          | Find first node matching predicate, forward.                    |
| `reverseNodeFind(p: T => Boolean): Option[...]`   | Find first node matching predicate, backward.                   |

### Direct Node Manipulation

Operations on `DLListNode` (`class DLListNode[T]`) include:

- `node.follow(elem: T): DLListNode[T]` — insert after this node.
- `node.precede(elem: T): DLListNode[T]` — insert before this node.
- `node.unlink: T` — unlink (remove) this node; returns its element.
- `node.unlinkUntil(other: DLListNode[T]): Seq[T]` — unlink a contiguous block up to `other`.
- Position checks: `isBefore`, `isAfter`, `isBeforeStart`, `isAfterEnd`.
- Iterators: `iterator`, `reverseIterator`, plus `iteratorUntil` and `reverseIteratorUntil`.

---

## DLListNodeRef

```scala
trait DLListNodeRef[T] {
  /** Called when this object is added as a node; provides node reference. */
  infix def ref(node: DLListNode[T]): Unit
}
```

Use this trait to track the node associated with an object when it is inserted.

---

## DLIndexedList

### Class Signature

```scala
class DLIndexedList[T] extends DLList[T]
```

Maintains an internal:

```scala
protected lazy val array: scala.collection.mutable.ArrayBuffer[DLListNode[T]]
```

#### Overridden Methods

- `clear()`: clears both the list and the index array.
- `appendElement(elem: T)`: append plus `array += n`.
- `prependElement(elem: T)`: prepend plus `n +=: array`.
- `node(n: Int)`: O(1) index access via `array(n)`.
- `insertAll(idx, elems)`: updates `array` with inserted nodes.
- `remove(idx)`: updates `array` and returns removed element.
- `toString`: prints in `DLIndexedList(e1, e2, …)` format.

---

## Usage Examples

```scala
import io.github.edadma.dllist._

// DLList example
type L = DLList[Double]
val list: L = DLList(1.0, 2.0, 3.0)
list += 4.0                    // [1.0, 2.0, 3.0, 4.0]
list.prepend(0.0)              // [0.0, 1.0, 2.0, 3.0, 4.0]

val thirdElem: Double = list(2) // 2.0

// Direct node manipulation
val node = list.headNode       // node[0.0]
node.follow(0.5)               // insert after head
node.precede(-0.5)             // insert before head

// Remove by index
list.remove(3)

// DLIndexedList example
val idxList = DLIndexedList("a", "b", "c")
val second: String = idxList(1) // "b"
idxList.insertAll(1, List("x","y"))
println(idxList)               // DLIndexedList(a, x, y, b, c)
```
