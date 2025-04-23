package io.github.edadma.dllist

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/** Companion object for [[DLIndexedList]]. Provides convenient factory methods for creating new indexed double-linked
  * lists.
  */
object DLIndexedList:
  /** Creates a new [[DLIndexedList]] containing the provided elements.
    *
    * @param elems
    *   the elements to include in the new list
    * @tparam T
    *   the type of elements in the list
    * @return
    *   a new DLIndexedList containing the provided elements
    */
  def apply[T](elems: T*): DLIndexedList[T] =
    new DLIndexedList[T] {
      appendAll(elems)
    }

/** A mutable doubly-linked list with O(1) indexed access. This implementation extends [[DLList]] with an array buffer
  * that provides fast random access to elements by index.
  *
  * @tparam T
  *   the type of elements in the list
  */
class DLIndexedList[T] extends DLList[T]:
  /** Array buffer that stores references to all nodes for O(1) index access.
    */
  protected lazy val array = new ArrayBuffer[Node]

  /** Clears all elements from the list. Overrides the base implementation to also clear the index array.
    */
  override def clear(): Unit = {
    super.clear()
    array.clear()
  }

  /** Appends an element to the end of the list and returns the new node. Overrides the base implementation to also
    * update the index array.
    *
    * @param elem
    *   the element to append
    * @return
    *   the newly created node
    */
  override def appendElement(elem: T): Node = {
    val n = super.appendElement(elem)

    array += n
    n
  }

  /** Gets the node at the specified index with O(1) complexity. Overrides the base implementation for better
    * performance.
    *
    * @param n
    *   the index of the node to retrieve
    * @return
    *   the node at the specified index
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  override def node(n: Int): Node = {
    require(0 <= n && n < count, s"node index out of range: $n")

    array(n)
  }

  /** Prepends an element to the start of the list and returns the new node. Overrides the base implementation to also
    * update the index array.
    *
    * @param elem
    *   the element to prepend
    * @return
    *   the newly created node
    */
  override def prependElement(elem: T): Node = {
    val n = super.prependElement(elem)

    n +=: array
    n
  }

  /** Inserts multiple elements at the specified index. Overrides the base implementation to also update the index
    * array.
    *
    * @param n
    *   the index at which to insert
    * @param elems
    *   the elements to insert
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  override def insertAll(n: Int, elems: IterableOnce[T]): Unit =
    array.insertAll(n, super._insertAll(n, elems).iterator.toList)

  /** Removes and returns the element at the specified index. Overrides the base implementation to also update the index
    * array.
    *
    * @param idx
    *   the index of the element to remove
    * @return
    *   the removed element
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  override def remove(n: Int): T = {
    super.remove(n)
    array.remove(n).element
  }

  /** Returns a string representation of this list.
    *
    * @return
    *   a string representation
    */
  override def toString: String = iterator mkString ("DLIndexedList(", ", ", ")")
