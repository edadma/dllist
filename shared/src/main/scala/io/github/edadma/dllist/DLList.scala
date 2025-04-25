// File: DLList.scala
package io.github.edadma.dllist

import scala.annotation.tailrec
import scala.collection.mutable
import java.util.NoSuchElementException

/** Companion object for [[DLList]]. Provides factory methods for creating new doubly-linked lists.
  */
object DLList:
  /** Creates a new [[DLList]] containing the provided elements.
    *
    * @param elems
    *   the elements to include in the new list
    * @tparam T
    *   the type of elements in the list
    * @return
    *   a new DLList containing the provided elements
    */
  def apply[T](elems: T*): DLList[T] =
    val list = new DLList[T]
    list.appendAll(elems)
    list

/** A mutable doubly-linked list implementation with both forward and backward traversal capabilities. Sentinel nodes
  * simplify boundary handling. Supports standard buffer operations and direct node manipulation.
  *
  * @tparam T
  *   the type of elements stored in the list
  */
class DLList[T] extends mutable.AbstractBuffer[T]:
  /** A sentinel node marking the start or end of the list. Prevents null checks on boundaries. */
  class Sentinel(name: String) extends DLListNode[T](this, null, null, null.asInstanceOf[T]):
    override def preceding: DLListNode[T] = sys.error(s"$name has no preceding node")
    override def following: DLListNode[T] = sys.error(s"$name has no following node")
    override def iterator: Iterator[DLListNode[T]] =
      if this eq startSentinel then sys.error("can't iterate from start sentinel")
      else super.iterator
    override def reverseIterator: Iterator[DLListNode[T]] =
      if this eq endSentinel then sys.error("can't iterate from end sentinel")
      else super.reverseIterator
    override def toString: String = name

  /** The start sentinel; its .next points to the first element or endSentinel if empty. */
  val startSentinel: Sentinel = Sentinel("start sentinel")

  /** The end sentinel; its .prev points to the last element or startSentinel if empty. */
  val endSentinel: Sentinel            = Sentinel("end sentinel")
  protected[dllist] var elemCount: Int = 0

  // Initialize empty list
  clear()

  /** Appends an element at the end of the list.
    * @param elem
    *   the element to append
    * @return
    *   the newly created node containing the element
    */
  def appendElement(elem: T): DLListNode[T] = endSentinel precede elem

  /** Retrieves the first element's node; fails if the list is empty.
    * @throws IllegalArgumentException
    *   if the list is empty
    * @return
    *   the first node
    */
  def headNode: DLListNode[T] =
    require(nonEmpty, "list is empty")
    startSentinel.next

  /** Optionally retrieves the first element's node.
    * @return
    *   Some(node) if non-empty, otherwise None
    */
  def headNodeOption: Option[DLListNode[T]] = if isEmpty then None else Some(headNode)

  /** Retrieves the last element's node; fails if the list is empty.
    * @throws IllegalArgumentException
    *   if the list is empty
    * @return
    *   the last node
    */
  def lastNode: DLListNode[T] =
    require(nonEmpty, "list is empty")
    endSentinel.prev

  /** Optionally retrieves the last element's node.
    * @return
    *   Some(node) if non-empty, otherwise None
    */
  def lastNodeOption: Option[DLListNode[T]] = if isEmpty then None else Some(lastNode)

  /** Retrieves the node at the specified index with bidirectional traversal.
    * @param n
    *   index of the node (0-based)
    * @throws IllegalArgumentException
    *   if index out of range
    * @return
    *   the node at index n
    */
  def node(n: Int): DLListNode[T] =
    require(0 <= n && n < elemCount, s"node index out of range: $n")
    if n <= elemCount / 2 then (nodeIterator drop n).next()
    else (reverseNodeIterator drop (elemCount - 1 - n)).next()

  /** Iterator over nodes, from first to last.
    * @return
    *   iterator of [[DLListNode]]
    */
  def nodeIterator: Iterator[DLListNode[T]] = startSentinel.next.iterator

  /** Reverse iterator over nodes, from last to first.
    * @return
    *   reverse iterator of [[DLListNode]]
    */
  def reverseNodeIterator: Iterator[DLListNode[T]] = endSentinel.prev.reverseIterator

  /** Finds the first node satisfying predicate p, forward direction.
    * @param p
    *   predicate to test element
    * @return
    *   Some(node) if found, None otherwise
    */
  def nodeFind(p: T => Boolean): Option[DLListNode[T]] = startSentinel.next.find(p)

  /** Finds the first node satisfying predicate p, backward direction.
    * @param p
    *   predicate to test element
    * @return
    *   Some(node) if found, None otherwise
    */
  def reverseNodeFind(p: T => Boolean): Option[DLListNode[T]] = endSentinel.prev.reverseFind(p)

  /** Prepends an element at the start of the list.
    * @param elem
    *   the element to prepend
    * @return
    *   the newly created node containing the element
    */
  def prependElement(elem: T): DLListNode[T] = startSentinel follow elem

  // Buffer methods implementation
  override def addOne(elem: T): this.type =
    appendElement(elem)
    this

  /** Prepends an element and returns this buffer.
    * @param elem
    *   the element to prepend
    * @return
    *   this list
    */
  def prepend(elem: T): this.type =
    prependElement(elem)
    this

  /** Applies zero-based index to get element value.
    * @param n
    *   index of element
    * @return
    *   value at index
    */
  override def apply(n: Int): T = node(n).v

  /** Clears all elements from the list. */
  override def clear(): Unit =
    startSentinel.next = endSentinel
    endSentinel.prev = startSentinel
    elemCount = 0

  /** Returns an iterator over elements.
    * @return
    *   forward element iterator
    */
  override def iterator: Iterator[T] = nodeIterator.map(_.v)

  /** Returns the number of elements in the list.
    * @return
    *   current list size
    */
  override def length: Int = elemCount

  /** Patches the list in place by replacing `replaced` elements starting at `from` with `patch` elements.
    *
    * @param from
    *   starting index
    * @param patch
    *   elements to insert
    * @param replaced
    *   number of elements to remove
    * @return
    *   this list
    */
  def patchInPlace(from: Int, patch: IterableOnce[T], replaced: Int): this.type = ???

  /** Inserts an element at specified position.
    *
    * @param idx
    *   index to insert at
    * @param elem
    *   element to insert
    */
  override def insert(idx: Int, elem: T): Unit = node(idx).precede(elem)

  /** Inserts all elements of the given iterable at position n. Delegates to protected `_insertAll` for core logic.
    *
    * @param n
    *   index to insert at
    * @param elems
    *   elements to insert
    */
  override def insertAll(n: Int, elems: IterableOnce[T]): Unit = _insertAll(n, elems)

  /** Core bulk insert that links new nodes before index n.
    *
    * @param n
    *   index to insert at
    * @param elems
    *   elements to insert
    * @return
    *   the first inserted node
    */
  protected def _insertAll(n: Int, elems: IterableOnce[T]): DLListNode[T] =
    var prevNode =
      if isEmpty then startSentinel
      else if n == elemCount then lastNode
      else node(n).prev
    val firstInserted = prevNode.next
    elems.iterator.foreach(e => prevNode = prevNode follow e)
    firstInserted

  /** Removes `cnt` elements starting from index `idx`.
    *
    * @param idx
    *   starting index
    * @param cnt
    *   number of elements to remove
    */
  override def remove(idx: Int, cnt: Int): Unit =
    @tailrec def rm(n: DLListNode[T], rem: Int): Unit =
      if rem > 0 then
        val nxt = n.next
        n.unlink
        rm(nxt, rem - 1)
    rm(node(idx), cnt)

  /** Removes and returns the element at position `idx`.
    *
    * @param idx
    *   index of element to remove
    * @return
    *   removed element
    */
  override def remove(idx: Int): T = node(idx).unlink

  /** Updates the element at position `idx` to `newelem`.
    *
    * @param idx
    *   index to update
    * @param newelem
    *   new value to set
    */
  override def update(idx: Int, newelem: T): Unit = node(idx).v = newelem

  override def head: T                      = headNode.v
  override def last: T                      = lastNode.v
  override def reverseIterator: Iterator[T] = reverseNodeIterator.map(_.v)

  /** Returns a string representation of the list in the form DLList(e1, e2, ...). */
  override def toString: String = iterator.mkString("DLList(", ", ", ")")

end DLList
