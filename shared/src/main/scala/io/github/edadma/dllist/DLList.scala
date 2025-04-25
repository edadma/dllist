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
  /** A node in the doubly-linked list. */
  class Node(private[DLList] var prev: Node, private[DLList] var next: Node, init: T):
    private[DLList] var v = init

    /** Guard against operations on an unlinked node. */
    private def assertLinked(): Unit =
      require(prev != null || next != null, "operation on unlinked (orphaned) node")

    /** Constructs an empty sentinel node. */
    private[DLList] def this() = this(null, null, null.asInstanceOf[T])

    def element: T                = v
    def element_=(value: T): Unit = v = value

    /** Gets the next node in the list. */
    def following: Node =
      assertLinked()
      next

    /** Gets the previous node in the list. */
    def preceding: Node =
      assertLinked()
      prev

    def isBeforeStart: Boolean  = eq(startSentinel)
    def notBeforeStart: Boolean = ne(startSentinel)
    def isAfterEnd: Boolean     = eq(endSentinel)
    def notAfterEnd: Boolean    = ne(endSentinel)

    /** Sentinel guard (cannot unlink sentinel). */
    private def checkUnlink(): Unit =
      require(this ne startSentinel, "can't unlink the start sentinel")
      require(this ne endSentinel, "can't unlink the end sentinel")

    /** Removes this node from the list and returns its element. */
    def unlink: T =
      checkUnlink()
      assertLinked()
      next.prev = prev
      prev.next = next
      count -= 1
      // orphan
      prev = null
      next = null
      v

    /** Inserts a new node with the given element after this node. */
    infix def follow(elem: T): Node =
      assertLinked()
      val node = new Node(this, next, elem)
      elem match
        case ref: NodeRef => ref ref node.asInstanceOf[DLList[Any]#Node]
        case _            =>
      next.prev = node
      next = node
      count += 1
      node

    /** Inserts a new node with the given element before this node. */
    infix def precede(elem: T): Node =
      assertLinked()
      val node = new Node(prev, this, elem)
      elem match
        case ref: NodeRef => ref ref node.asInstanceOf[DLList[Any]#Node]
        case _            =>
      prev.next = node
      prev = node
      count += 1
      node

    /** Unlinks this node and all nodes until (exclusive) the specified node. */
    def unlinkUntil(node: Node): Seq[T] =
      checkUnlink()
      assertLinked()
      require(isBefore(node), "node to unlink up to must come after current node")

      val buf = new ListBuffer[T]
      var cur = this
      // reconnect around block
      node.prev = prev
      prev.next = node

      while cur ne node do
        buf += cur.v
        // orphan current
        cur.prev = null
        cur.next = null
        count -= 1
        cur = cur.next // safe since cur.next points to node or next
      buf.toList

    /** Determines if this node comes before the specified node. */
    def isBefore(node: Node): Boolean =
      assertLinked()
      if (this eq node) || isAfterEnd then false
      else
        var cur = this
        while cur.notAfterEnd && (cur ne node) do cur = cur.following
        node.isAfterEnd || cur.notAfterEnd

    /** Determines if this node comes after the specified node. */
    def isAfter(node: Node): Boolean =
      assertLinked()
      if (this eq node) || isBeforeStart then false
      else
        var cur = this
        while cur.notBeforeStart && (cur ne node) do cur = cur.preceding
        node.isBeforeStart || cur.notBeforeStart

    /** Creates an iterator that traverses nodes until (exclusive) the specified node. */
    def iteratorUntil(last: Node): Iterator[Node] =
      assertLinked()
      new Iterator[Node]:
        private var node     = Node.this
        private var nextNode = node.next
        def hasNext: Boolean = (node ne last) && (node ne endSentinel)
        def next(): Node =
          if !hasNext then throw new NoSuchElementException("no more elements")
          val res = node
          node = nextNode
          if hasNext then nextNode = node.next
          res

    /** Creates an iterator that traverses from this node to the end. */
    def iterator: Iterator[Node] = iteratorUntil(endSentinel)

    /** Creates a reverse iterator until (exclusive) the specified node. */
    def reverseIteratorUntil(last: Node): Iterator[Node] =
      assertLinked()
      new Iterator[Node]:
        private var node     = Node.this
        def hasNext: Boolean = (node ne last) && (node ne startSentinel)
        def next(): Node =
          if !hasNext then throw new NoSuchElementException("no more elements")
          val res = node
          node = node.prev
          res

    /** Creates a reverse iterator to the start. */
    def reverseIterator: Iterator[Node] = reverseIteratorUntil(startSentinel)

    /** Finds the first node forward satisfying the predicate. */
    def find(p: T => Boolean): Option[Node] =
      assertLinked()
      iterator find (n => p(n.v))

    /** Finds the first node backward satisfying the predicate. */
    def reverseFind(p: T => Boolean): Option[Node] =
      assertLinked()
      reverseIterator find (n => p(n.v))

    /** Finds the index of this node. */
    def index: Int =
      assertLinked()
      var cur: Node = startSentinel
      var idx       = -1
      while cur.notAfterEnd && ne(cur) do
        cur = cur.next
        idx += 1
      if cur.notAfterEnd then idx else -1

    /** Skips forward n nodes. */
    def skipForward(n: Int): Node =
      assertLinked()
      if n > 0 then following.skipForward(n - 1) else this

    /** Skips backward n nodes. */
    def skipReverse(n: Int): Node =
      assertLinked()
      if n > 0 then preceding.skipReverse(n - 1) else this

    override def toString: String = s"node[$v]"
  end Node

  /** A sentinel node for start/end. */
  class Sentinel(name: String) extends Node:
    override def preceding: Node = sys.error(s"$name has no preceding node")
    override def following: Node = sys.error(s"$name has no following node")
    override def iterator: Iterator[Node] =
      if this eq startSentinel then sys.error("can't iterate from start sentinel") else super.iterator
    override def reverseIterator: Iterator[Node] =
      if this eq endSentinel then sys.error("can't iterate from end sentinel") else super.reverseIterator
    override def toString: String = name

  val startSentinel: Sentinel = Sentinel("start sentinel")
  val endSentinel: Sentinel   = Sentinel("end sentinel")
  protected var count: Int    = 0
  clear()

  /** Append element at end. */
  def appendElement(elem: T): Node = endSentinel precede elem
  def headNode: Node =
    require(nonEmpty, "list is empty")
    startSentinel.next
  def headNodeOption: Option[Node] = if isEmpty then None else Some(headNode)
  def lastNode: Node =
    require(nonEmpty, "list is empty")
    endSentinel.prev
  def lastNodeOption: Option[Node] = if isEmpty then None else Some(lastNode)

  /** Gets node by index. */
  def node(n: Int): Node =
    require(0 <= n && n < count, s"node index out of range: $n")
    if n <= count / 2 then (nodeIterator drop n).next()
    else (reverseNodeIterator drop (count - 1 - n)).next()

  def nodeIterator: Iterator[Node]                   = startSentinel.next.iterator
  def reverseNodeIterator: Iterator[Node]            = endSentinel.prev.reverseIterator
  def nodeFind(p: T => Boolean): Option[Node]        = startSentinel.next.find(p)
  def reverseNodeFind(p: T => Boolean): Option[Node] = endSentinel.prev.reverseFind(p)
  def prependElement(elem: T): Node                  = startSentinel follow elem

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
    count = 0

  def iterator: Iterator[T] = nodeIterator map (_.v)
  def length: Int           = count

  def patchInPlace(from: Int, patch: IterableOnce[T], replaced: Int): this.type =
    ???

  def insert(idx: Int, elem: T): Unit                 = node(idx).precede(elem)
  def insertAll(n: Int, elems: IterableOnce[T]): Unit = _insertAll(n, elems)
  protected def _insertAll(n: Int, elems: IterableOnce[T]): Node =
    var prevNode = if isEmpty then startSentinel else if n == count then lastNode else node(n).prev
    val first    = prevNode.next
    elems.iterator.foreach(e => prevNode = prevNode follow e)
    first

  def remove(idx: Int, cnt: Int): Unit =
    @tailrec def rm(n: Node, rem: Int): Unit =
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
