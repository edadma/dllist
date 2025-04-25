package io.github.edadma.dllist

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import java.util.NoSuchElementException

/** Companion object for [[DLList]]. Provides convenient factory methods for creating new double-linked lists. */
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
    new DLList[T] {
      appendAll(elems)
    }

/** A mutable doubly-linked list implementation with both forward and backward traversal capabilities. Sentinel nodes
  * simplify boundary handling.
  */
class DLList[T] extends mutable.AbstractBuffer[T]:
  /** A sentinel node for start/end. */
  class Sentinel(name: String) extends DLListNode[T](this, null, null, null.asInstanceOf[T]):
    override def preceding: DLListNode[T] = sys.error(s"$name has no preceding node")
    override def following: DLListNode[T] = sys.error(s"$name has no following node")
    override def iterator: Iterator[DLListNode[T]] =
      if this eq startSentinel then sys.error("can't iterate from start sentinel") else super.iterator
    override def reverseIterator: Iterator[DLListNode[T]] =
      if this eq endSentinel then sys.error("can't iterate from end sentinel") else super.reverseIterator
    override def toString: String = name

  val startSentinel: Sentinel          = Sentinel("start sentinel")
  val endSentinel: Sentinel            = Sentinel("end sentinel")
  protected[dllist] var elemCount: Int = 0
  clear()

  /** Append element at end. */
  def appendElement(elem: T): DLListNode[T] = endSentinel precede elem
  def headNode: DLListNode[T] =
    require(nonEmpty, "list is empty")
    startSentinel.next
  def headNodeOption: Option[DLListNode[T]] = if isEmpty then None else Some(headNode)
  def lastNode: DLListNode[T] =
    require(nonEmpty, "list is empty")
    endSentinel.prev
  def lastNodeOption: Option[DLListNode[T]] = if isEmpty then None else Some(lastNode)

  /** Gets node by index. */
  def node(n: Int): DLListNode[T] =
    require(0 <= n && n < elemCount, s"node index out of range: $n")
    if n <= elemCount / 2 then (nodeIterator drop n).next()
    else (reverseNodeIterator drop (elemCount - 1 - n)).next()

  def nodeIterator: Iterator[DLListNode[T]]                   = startSentinel.next.iterator
  def reverseNodeIterator: Iterator[DLListNode[T]]            = endSentinel.prev.reverseIterator
  def nodeFind(p: T => Boolean): Option[DLListNode[T]]        = startSentinel.next.find(p)
  def reverseNodeFind(p: T => Boolean): Option[DLListNode[T]] = endSentinel.prev.reverseFind(p)
  def prependElement(elem: T): DLListNode[T]                  = startSentinel follow elem

  // Buffer methods
  def addOne(elem: T): this.type = {
    appendElement(elem)
    this
  }
  def prepend(elem: T): this.type = {
    prependElement(elem)
    this
  }
  def apply(n: Int): T = node(n).v
  def clear(): Unit =
    startSentinel.next = endSentinel
    endSentinel.prev = startSentinel
    elemCount = 0

  def iterator: Iterator[T] = nodeIterator map (_.v)
  def length: Int           = elemCount

  def patchInPlace(from: Int, patch: IterableOnce[T], replaced: Int): this.type =
    ???

  def insert(idx: Int, elem: T): Unit                 = node(idx).precede(elem)
  def insertAll(n: Int, elems: IterableOnce[T]): Unit = _insertAll(n, elems)
  protected def _insertAll(n: Int, elems: IterableOnce[T]): DLListNode[T] =
    var prevNode = if isEmpty then startSentinel else if n == elemCount then lastNode else node(n).prev
    val first    = prevNode.next
    elems.iterator.foreach(e => prevNode = prevNode follow e)
    first

  def remove(idx: Int, cnt: Int): Unit =
    @tailrec def rm(n: DLListNode[T], rem: Int): Unit =
      if rem > 0 then
        val nxt = n.next
        n.unlink
        rm(nxt, rem - 1)
    rm(node(idx), cnt)

  def remove(idx: Int): T                = node(idx).unlink
  def update(idx: Int, newelem: T): Unit = node(idx).v = newelem

  override def head: T                      = headNode.v
  override def last: T                      = lastNode.v
  override def reverseIterator: Iterator[T] = reverseNodeIterator map (_.v)
  override def toString: String             = iterator.mkString("DLList(", ", ", ")")

end DLList
