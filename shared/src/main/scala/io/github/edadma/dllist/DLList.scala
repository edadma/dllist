package io.github.edadma.dllist

import collection.mutable.ListBuffer
import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.postfixOps

object DLList {

  def apply[T](elems: T*): DLList[T] =
    new DLList[T] {
      appendAll(elems)
    }

}

class DLList[T] extends mutable.AbstractBuffer[T] {

  class Node(private[DLList] var prev: Node, private[DLList] var next: Node, init: T) {

    private[DLList] def this() = this(null, null, null.asInstanceOf[T])

    private[DLList] var v = init

    def element: T = v

    def element_=(value: T): Unit = v = value

    def following: Node = next

    def preceding: Node = prev

    def isBeforeStart: Boolean = eq(startSentinel)

    def notBeforeStart: Boolean = ne(startSentinel)

    def isAfterEnd: Boolean = eq(endSentinel)

    def notAfterEnd: Boolean = ne(endSentinel)

    def unlink: T = {
      checkUnlink()
      next.prev = prev
      prev.next = next
      prev = null
      next = null
      count -= 1
      v
    }

    def follow(v: T): Node = {
      val node = new Node(this, next, v)

      v match {
        case ref: NodeRef => ref ref node.asInstanceOf[DLList[Any]#Node]
        case _            =>
      }

      next.prev = node
      next = node
      count += 1
      node
    }

    def precede(v: T): Node = {
      val node = new Node(prev, this, v)

      v match {
        case ref: NodeRef => ref ref node.asInstanceOf[DLList[Any]#Node]
        case _            =>
      }

      prev.next = node
      prev = node
      count += 1
      node
    }

    private def checkUnlink(): Unit = {
      require(this ne startSentinel, "can't unlink the start sentinel")
      require(this ne endSentinel, "can't unlink the end sentinel")
    }

    def unlinkUntil(node: Node): Seq[Any] = {
      checkUnlink()
      require(isBefore(node), "node to unlink up to must come after current node")

      var cur = this
      val buf = new ListBuffer[T]

      node.prev = prev
      prev.next = node

      while (cur ne node) {
        val curnext = cur.next

        buf += cur.element
        cur.prev = null
        cur.next = null
        count -= 1
        cur = curnext
      }

      buf.toList
    }

    def isBefore(node: Node): Boolean =
      if ((this eq node) || isAfterEnd)
        false
      else {
        var cur = this

        while (cur.notAfterEnd && (cur ne node)) cur = cur.following

        node.isAfterEnd || cur.notAfterEnd
      }

    def iteratorUntil(last: Node): Iterator[Node] =
      new Iterator[Node] {
        private var node = Node.this
        private var nextNode = node.next

        def hasNext: Boolean = (node ne last) && (node ne endSentinel)

        def next(): Node = {
          if (isEmpty) throw new NoSuchElementException("no more elements")

          val res = node

          node = nextNode

          if (hasNext) nextNode = node.next

          res
        }
      }

    def iterator: Iterator[Node] = iteratorUntil(endSentinel)

    def reverseIteratorUntil(last: Node): Iterator[Node] =
      new Iterator[Node] {
        private var node = Node.this

        def hasNext: Boolean = (node ne last) && (node ne startSentinel)

        def next(): Node = {
          if (isEmpty) throw new NoSuchElementException("no more elements")

          val res = node

          node = node.prev
          res
        }
      }

    def reverseIterator: Iterator[Node] = reverseIteratorUntil(startSentinel)

    def find(p: T => Boolean): Option[Node] = iterator find (n => p(n.element))

    def reverseFind(p: T => Boolean): Option[Node] = reverseIterator find (n => p(n.element))

    def index: Int = {
      var cur: Node = startSentinel
      var idx = -1

      while (cur.notAfterEnd && ne(cur)) {
        cur = cur.next
        idx += 1
      }

      if (cur.notAfterEnd) idx else -1
    }

    def skipForward(n: Int): Node =
      if (n > 0)
        following.skipForward(n - 1)
      else
        this

    def skipReverse(n: Int): Node =
      if (n > 0)
        preceding.skipReverse(n - 1)
      else
        this

    override def toString: String = s"node[$v]"
  }

  class Sentinel(val name: String) extends Node {
    private def novalue = sys.error(s"$name has no value")

    override def element: T = novalue

    override def element_=(v: T): Unit = novalue

    private def noiterator = sys.error("can't iterate from sentinel")

    override def iterator: Iterator[Node] =
      if (this eq startSentinel)
        noiterator
      else
        super.iterator

    override def reverseIterator: Iterator[Node] =
      if (this eq endSentinel)
        noiterator
      else
        super.reverseIterator

    override def toString: String = name
  }

  val startSentinel: Sentinel =
    new Sentinel("start sentinel") {
      override def preceding: Node = sys.error(s"$name has no preceding node")
    }

  val endSentinel: Sentinel =
    new Sentinel("end sentinel") {
      override def following: Node = sys.error(s"$name has no following node")
    }

  protected var count = 0

  clear()

  //
  // DLList operations
  //

  def appendElement(elem: T): Node = endSentinel precede elem

  def headNode: Node = {
    require(nonEmpty, "list is empty")
    startSentinel.next
  }

  def headNodeOption: Option[Node] = if (isEmpty) None else Some(headNode)

  def lastNode: Node = {
    require(nonEmpty, "list is empty")
    endSentinel.prev
  }

  def lastNodeOption: Option[Node] = if (isEmpty) None else Some(lastNode)

  def node(n: Int): Node = {
    require(0 <= n && n < count, s"node index out of range: $n")

    if (n <= count / 2)
      (nodeIterator drop n).next()
    else
      (reverseNodeIterator drop (count - 1 - n)).next()
  }

  def nodeIterator: Iterator[Node] = startSentinel.next.iterator

  def reverseNodeIterator: Iterator[Node] = endSentinel.prev.reverseIterator

  def nodeFind(p: T => Boolean): Option[Node] = startSentinel.next.find(p)

  def reverseNodeFind(p: T => Boolean): Option[Node] = endSentinel.prev.reverseFind(p)

  def prependElement(elem: T): Node = startSentinel follow elem

  //
  // abstract Buffer methods
  //

  def addOne(elem: T): DLList.this.type = {
    appendElement(elem)
    this
  }

  def prepend(elem: T): DLList.this.type = {
    prependElement(elem)
    this
  }

  def apply(n: Int): T = node(n).v

  def clear(): Unit = {
    startSentinel.next = endSentinel
    endSentinel.prev = startSentinel
    count = 0
  }

  def iterator: Iterator[T] = nodeIterator map (_.v)

  def length: Int = count

  def patchInPlace(from: Int, patch: IterableOnce[T], replaced: Int): DLList.this.type = ???

  def insert(idx: Int, elem: T): Unit = node(idx).precede(elem)

  def insertAll(n: Int, elems: IterableOnce[T]): Unit = _insertAll(n, elems)

  protected def _insertAll(n: Int, elems: IterableOnce[T]): Node = {
    var prev =
      if (isEmpty)
        startSentinel
      else if (n == count)
        lastNode
      else
        node(n).prev
    val first = prev.next

    elems.iterator foreach (e => prev = prev follow e)
    first
  }

  def remove(idx: Int, count: Int): Unit = {
    @tailrec
    def remove(n: Node, rem: Int): Unit =
      if (rem > 0) {
        val next = n.following

        n.unlink
        remove(next, rem - 1)
      }

    remove(node(idx), count)
  }

  def remove(idx: Int): T = node(idx).unlink

  def update(idx: Int, newelem: T): Unit = node(idx).v = newelem

  //
  // overrides
  //

  override def head: T = headNode.v

  override def last: T = lastNode.v

  override def reverseIterator: Iterator[T] = reverseNodeIterator map (_.v)

  override def toString: String = iterator mkString ("DLList(", ", ", ")")

}
