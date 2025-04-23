package io.github.edadma.dllist

/** A trait for objects that need to reference a node in a doubly-linked list. Implementing classes can receive a
  * callback when they are added to a list.
  */
trait NodeRef:
  /** Method called when this object is added to a list as a node element.
    *
    * @param node
    *   the node containing this object
    */
  infix def ref(node: DLList[Any]#Node): Unit
