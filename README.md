# dllist

[![License: ISC](https://img.shields.io/badge/License-ISC-blue.svg)](https://opensource.org/licenses/ISC)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.edadma/dllist_3.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.edadma/dllist_3)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.18.2.svg)](https://www.scala-js.org)
[![Scala Native](https://scala-native.org/img/scala-native-logo-white.png)](https://scala-native.org)

A high-performance doubly-linked list implementation for Scala with both standard list operations and direct node manipulation.

## Overview

`dllist` provides two key data structures:

- `DLList`: A mutable doubly-linked list with O(1) operations for adding/removing at both ends and arbitrary positions when you have a reference to a node
- `DLIndexedList`: Extends `DLList` with O(1) indexed access by adding an array-backed indexing layer

The library supports advanced operations like:
- Forward and backward traversal
- Element and node-based searches
- Direct node manipulation (unlinking, inserting between nodes)
- O(1) access to first and last elements
- Efficient slicing and bulk operations

Built for the Scala 3 ecosystem and cross-compiled for JVM, JavaScript, and Native platforms.

## Installation

Add the following to your `build.sbt`:

```scala
libraryDependencies += "io.github.edadma" %%% "dllist" % "0.0.1"
```

The library is cross-compiled for:
- Scala JVM
- Scala.js
- Scala Native

## Basic Usage

```scala
import io.github.edadma.dllist._

// Create a new list
val list = DLList(1, 2, 3)

// Add elements
list += 4       // [1, 2, 3, 4]
list.prepend(0) // [0, 1, 2, 3, 4]

// Access elements
val first = list.head         // 0
val last = list.last          // 4
val third = list(2)           // 2

// Remove elements
list.remove(1)  // Removes second element
// list is now [0, 2, 3, 4]

// Iterate forward
for (elem <- list) println(elem)

// Iterate backward
for (elem <- list.reverseIterator) println(elem)

// Direct node manipulation
val node = list.headNode
val newNode = node.follow(1)  // Insert 1 after the first element
// list is now [0, 1, 2, 3, 4]
```

## Indexed List

For faster random access by index, use `DLIndexedList`:

```scala
import io.github.edadma.dllist._

val list = DLIndexedList(1, 2, 3, 4, 5)

// O(1) index access
val third = list(2)  // 3

// Other operations work the same as DLList
list.prepend(0)
list += 6
```

## Advanced Features

### Direct Node Manipulation

```scala
val list = DLList(1, 2, 3, 4, 5)
val node = list.node(2)  // Get node containing 3

// Insert elements around a node
node.precede(2.5)        // Insert before: [1, 2, 2.5, 3, 4, 5]
node.follow(3.5)         // Insert after: [1, 2, 2.5, 3, 3.5, 4, 5]

// Remove a node
node.unlink              // Removes node: [1, 2, 2.5, 3.5, 4, 5]

// Check node positions
val nodeA = list.headNode
val nodeB = list.lastNode
nodeA.isBefore(nodeB)    // true
```

### Node References

Implement the `NodeRef` trait to keep track of where objects are stored in the list:

```scala
class TrackedItem(val value: Int) extends NodeRef {
  private var myNode: DLList[Any]#Node = _
  
  def ref(node: DLList[Any]#Node): Unit = {
    myNode = node
  }
  
  def removeFromList(): Unit = {
    if (myNode != null) {
      myNode.unlink
      myNode = null
    }
  }
}

val list = DLList[TrackedItem]()
val item = new TrackedItem(42)
list += item

// Later, the item can remove itself from the list
item.removeFromList()
```

## Performance Considerations

- `DLList` provides O(1) operations for adding/removing at both ends
- For frequent random access by index, prefer `DLIndexedList` which has O(1) indexed access
- Direct node manipulation is always O(1) when you have a reference to the node
- Finding a node by predicate is O(n)

## Contributing

This project welcomes contributions from the community. To contribute:

1. Fork the repository
2. Create a feature branch
3. Add your changes
4. Add tests for your changes
5. Make sure all tests pass
6. Submit a pull request

## License

This project is licensed under the ISC License - see the LICENSE file for details.