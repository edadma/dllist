package io.github.edadma.dllist

import collection.mutable.ListBuffer
import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.postfixOps

/** Companion object for [[DLList]]. Provides convenient factory methods for creating new double-linked lists.
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
    new DLList[T] {
      appendAll(elems)
    }

/** A mutable doubly-linked list implementation with both forward and backward traversal capabilities. This
  * implementation maintains sentinel nodes at both ends for simplified boundary handling.
  *
  * @tparam T
  *   the type of elements in the list
  */
class DLList[T] extends mutable.AbstractBuffer[T]:
  /** A node in the doubly-linked list. Maintains references to previous and next nodes as well as the contained
    * element.
    *
    * @param prev
    *   reference to the previous node
    * @param next
    *   reference to the next node
    * @param init
    *   initial element value
    */
  class Node(private[DLList] var prev: Node, private[DLList] var next: Node, init: T) {

    /** Constructs an empty sentinel node.
      */
    private[DLList] def this() = this(null, null, null.asInstanceOf[T])

    private[DLList] var v = init

    /** Gets the element stored in this node.
      * @return
      *   the element
      */
    def element: T = v

    /** Updates the element stored in this node.
      * @param value
      *   the new element value
      */
    def element_=(value: T): Unit = v = value

    /** Gets the next node in the list.
      * @return
      *   the next node
      */
    def following: Node = next

    /** Gets the previous node in the list.
      * @return
      *   the previous node
      */
    def preceding: Node = prev

    /** Checks if this node is the start sentinel.
      * @return
      *   true if this is the start sentinel, false otherwise
      */
    def isBeforeStart: Boolean = eq(startSentinel)

    /** Checks if this node is not the start sentinel.
      * @return
      *   true if this is not the start sentinel, false otherwise
      */
    def notBeforeStart: Boolean = ne(startSentinel)

    /** Checks if this node is the end sentinel.
      * @return
      *   true if this is the end sentinel, false otherwise
      */
    def isAfterEnd: Boolean = eq(endSentinel)

    /** Checks if this node is not the end sentinel.
      * @return
      *   true if this is not the end sentinel, false otherwise
      */
    def notAfterEnd: Boolean = ne(endSentinel)

    /** Removes this node from the list and returns its element. Cannot be called on sentinel nodes.
      *
      * @return
      *   the element from the removed node
      * @throws IllegalArgumentException
      *   if called on a sentinel node
      */
    def unlink: T = {
      checkUnlink()
      next.prev = prev
      prev.next = next
      prev = null
      next = null
      count -= 1
      v
    }

    /** Inserts a new node with the given element after this node.
      *
      * @param v
      *   the element to insert
      * @return
      *   the newly created node
      */
    infix def follow(v: T): Node =
      val node = new Node(this, next, v)

      v match {
        case ref: NodeRef => ref ref node.asInstanceOf[DLList[Any]#Node]
        case _            =>
      }

      next.prev = node
      next = node
      count += 1
      node

    /** Inserts a new node with the given element before this node.
      *
      * @param v
      *   the element to insert
      * @return
      *   the newly created node
      */
    infix def precede(v: T): Node =
      val node = new Node(prev, this, v)

      v match {
        case ref: NodeRef => ref ref node.asInstanceOf[DLList[Any]#Node]
        case _            =>
      }

      prev.next = node
      prev = node
      count += 1
      node

    /** Validates that this node can be unlinked from the list.
      * @throws IllegalArgumentException
      *   if this is a sentinel node
      */
    private def checkUnlink(): Unit = {
      require(this ne startSentinel, "can't unlink the start sentinel")
      require(this ne endSentinel, "can't unlink the end sentinel")
    }

    /** Unlinks this node and all nodes until but not including the specified node. The node specified must come after
      * this node in the list.
      *
      * @param node
      *   the node to unlink until (exclusive)
      * @return
      *   sequence of elements that were unlinked
      * @throws IllegalArgumentException
      *   if this is a sentinel node or if target node doesn't come after this node
      */
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

    /** Determines if this node comes before the specified node in the list.
      *
      * @param node
      *   the node to check against
      * @return
      *   true if this node comes before the specified node, false otherwise
      */
    def isBefore(node: Node): Boolean =
      if ((this eq node) || isAfterEnd)
        false
      else {
        var cur = this

        while (cur.notAfterEnd && (cur ne node)) cur = cur.following

        node.isAfterEnd || cur.notAfterEnd
      }

    /** Determines if this node comes after the specified node in the list.
      *
      * @param node
      *   the node to check against
      * @return
      *   true if this node comes after the specified node, false otherwise
      */
    def isAfter(node: Node): Boolean =
      if ((this eq node) || isBeforeStart)
        false
      else {
        var cur = this

        while (cur.notBeforeStart && (cur ne node)) cur = cur.preceding

        node.isAfterEnd || cur.notBeforeStart
      }

    /** Creates an iterator that traverses nodes from this node until (but not including) the specified node.
      *
      * @param last
      *   the node to stop before
      * @return
      *   an iterator of nodes
      */
    def iteratorUntil(last: Node): Iterator[Node] =
      new Iterator[Node] {
        private var node     = Node.this
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

    /** Creates an iterator that traverses from this node to the end of the list.
      *
      * @return
      *   an iterator of nodes
      */
    def iterator: Iterator[Node] = iteratorUntil(endSentinel)

    /** Creates a reverse iterator that traverses nodes from this node backward until (but not including) the specified
      * node.
      *
      * @param last
      *   the node to stop before when traversing backward
      * @return
      *   a reverse iterator of nodes
      */
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

    /** Creates a reverse iterator that traverses from this node to the start of the list.
      *
      * @return
      *   a reverse iterator of nodes
      */
    def reverseIterator: Iterator[Node] = reverseIteratorUntil(startSentinel)

    /** Finds the first node from this node forward whose element satisfies the given predicate.
      *
      * @param p
      *   the predicate to test elements against
      * @return
      *   Some(node) if found, None otherwise
      */
    def find(p: T => Boolean): Option[Node] = iterator find (n => p(n.element))

    /** Finds the first node from this node backward whose element satisfies the given predicate.
      *
      * @param p
      *   the predicate to test elements against
      * @return
      *   Some(node) if found, None otherwise
      */
    def reverseFind(p: T => Boolean): Option[Node] = reverseIterator find (n => p(n.element))

    /** Finds the index of this node in the list.
      *
      * @return
      *   the index of this node, or -1 if not found
      */
    def index: Int = {
      var cur: Node = startSentinel
      var idx       = -1

      while (cur.notAfterEnd && ne(cur)) {
        cur = cur.next
        idx += 1
      }

      if (cur.notAfterEnd) idx else -1
    }

    /** Moves forward n nodes from this node.
      *
      * @param n
      *   number of nodes to skip
      * @return
      *   the node n positions forward
      */
    def skipForward(n: Int): Node =
      if (n > 0)
        following.skipForward(n - 1)
      else
        this

    /** Moves backward n nodes from this node.
      *
      * @param n
      *   number of nodes to skip
      * @return
      *   the node n positions backward
      */
    def skipReverse(n: Int): Node =
      if (n > 0)
        preceding.skipReverse(n - 1)
      else
        this

    override def toString: String = s"node[$v]"
  }

  /** A sentinel node used for the start and end of the list to simplify boundary handling.
    */
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

  /** The sentinel node at the start of the list. */
  val startSentinel: Sentinel =
    new Sentinel("start sentinel") {
      override def preceding: Node = sys.error(s"$name has no preceding node")
    }

  /** The sentinel node at the end of the list. */
  val endSentinel: Sentinel =
    new Sentinel("end sentinel") {
      override def following: Node = sys.error(s"$name has no following node")
    }

  /** The number of elements in the list. */
  protected var count = 0

  clear()

  //
  // DLList operations
  //

  /** Appends an element to the end of the list and returns the new node.
    *
    * @param elem
    *   the element to append
    * @return
    *   the newly created node
    */
  def appendElement(elem: T): Node = endSentinel precede elem

  /** Gets the first node in the list.
    *
    * @return
    *   the first node
    * @throws IllegalArgumentException
    *   if the list is empty
    */
  def headNode: Node = {
    require(nonEmpty, "list is empty")
    startSentinel.next
  }

  /** Gets the first node in the list as an Option.
    *
    * @return
    *   Some(node) if the list is non-empty, None otherwise
    */
  def headNodeOption: Option[Node] = if (isEmpty) None else Some(headNode)

  /** Gets the last node in the list.
    *
    * @return
    *   the last node
    * @throws IllegalArgumentException
    *   if the list is empty
    */
  def lastNode: Node = {
    require(nonEmpty, "list is empty")
    endSentinel.prev
  }

  /** Gets the last node in the list as an Option.
    *
    * @return
    *   Some(node) if the list is non-empty, None otherwise
    */
  def lastNodeOption: Option[Node] = if (isEmpty) None else Some(lastNode)

  /** Gets the node at the specified index.
    *
    * @param n
    *   the index of the node to retrieve
    * @return
    *   the node at the specified index
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  def node(n: Int): Node = {
    require(0 <= n && n < count, s"node index out of range: $n")

    if (n <= count / 2)
      (nodeIterator drop n).next()
    else
      (reverseNodeIterator drop (count - 1 - n)).next()
  }

  /** Creates an iterator that traverses all nodes in the list.
    *
    * @return
    *   an iterator of nodes
    */
  def nodeIterator: Iterator[Node] = startSentinel.next.iterator

  /** Creates a reverse iterator that traverses all nodes in the list backward.
    *
    * @return
    *   a reverse iterator of nodes
    */
  def reverseNodeIterator: Iterator[Node] = endSentinel.prev.reverseIterator

  /** Finds the first node in the list whose element satisfies the given predicate.
    *
    * @param p
    *   the predicate to test elements against
    * @return
    *   Some(node) if found, None otherwise
    */
  def nodeFind(p: T => Boolean): Option[Node] = startSentinel.next.find(p)

  /** Finds the first node from the end of the list whose element satisfies the given predicate.
    *
    * @param p
    *   the predicate to test elements against
    * @return
    *   Some(node) if found, None otherwise
    */
  def reverseNodeFind(p: T => Boolean): Option[Node] = endSentinel.prev.reverseFind(p)

  /** Prepends an element to the start of the list and returns the new node.
    *
    * @param elem
    *   the element to prepend
    * @return
    *   the newly created node
    */
  def prependElement(elem: T): Node = startSentinel follow elem

  //
  // abstract Buffer methods
  //

  /** Adds an element to the end of the list.
    *
    * @param elem
    *   the element to add
    * @return
    *   this list for chaining
    */
  def addOne(elem: T): DLList.this.type = {
    appendElement(elem)
    this
  }

  /** Adds an element to the beginning of the list.
    *
    * @param elem
    *   the element to prepend
    * @return
    *   this list for chaining
    */
  def prepend(elem: T): DLList.this.type = {
    prependElement(elem)
    this
  }

  /** Gets the element at the specified index.
    *
    * @param n
    *   the index
    * @return
    *   the element at the specified index
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  def apply(n: Int): T = node(n).v

  /** Clears all elements from the list.
    */
  def clear(): Unit = {
    startSentinel.next = endSentinel
    endSentinel.prev = startSentinel
    count = 0
  }

  /** Creates an iterator that traverses all elements in the list.
    *
    * @return
    *   an iterator of elements
    */
  def iterator: Iterator[T] = nodeIterator map (_.v)

  /** Gets the number of elements in the list.
    *
    * @return
    *   the size of the list
    */
  def length: Int = count

  /** Replaces a slice of this list with the given elements.
    *
    * @param from
    *   the index of the first element to replace
    * @param patch
    *   the collection containing replacement elements
    * @param replaced
    *   the number of elements to replace
    * @return
    *   this list for chaining
    */
  def patchInPlace(from: Int, patch: IterableOnce[T], replaced: Int): DLList.this.type = ???

  /** Inserts an element at the specified index.
    *
    * @param idx
    *   the index at which to insert
    * @param elem
    *   the element to insert
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  def insert(idx: Int, elem: T): Unit = node(idx).precede(elem)

  /** Inserts multiple elements at the specified index.
    *
    * @param n
    *   the index at which to insert
    * @param elems
    *   the elements to insert
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  def insertAll(n: Int, elems: IterableOnce[T]): Unit = _insertAll(n, elems)

  /** Internal helper method for insertAll that returns the first node inserted.
    *
    * @param n
    *   the index at which to insert
    * @param elems
    *   the elements to insert
    * @return
    *   the first node inserted, or the node at index n if no elements inserted
    */
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

  /** Removes a range of elements from the list.
    *
    * @param idx
    *   the starting index
    * @param count
    *   the number of elements to remove
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
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

  /** Removes and returns the element at the specified index.
    *
    * @param idx
    *   the index of the element to remove
    * @return
    *   the removed element
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  def remove(idx: Int): T = node(idx).unlink

  /** Updates the element at the specified index.
    *
    * @param idx
    *   the index of the element to update
    * @param newelem
    *   the new element value
    * @throws IllegalArgumentException
    *   if the index is out of bounds
    */
  def update(idx: Int, newelem: T): Unit = node(idx).v = newelem

  //
  // overrides
  //

  /** Gets the first element in the list.
    *
    * @return
    *   the first element
    * @throws NoSuchElementException
    *   if the list is empty
    */
  override def head: T = headNode.v

  /** Gets the last element in the list.
    *
    * @return
    *   the last element
    * @throws NoSuchElementException
    *   if the list is empty
    */
  override def last: T = lastNode.v

  /** Creates a reverse iterator that traverses all elements in the list backward.
    *
    * @return
    *   a reverse iterator of elements
    */
  override def reverseIterator: Iterator[T] = reverseNodeIterator map (_.v)

  /** Returns a string representation of this list.
    *
    * @return
    *   a string representation
    */
  override def toString: String = iterator mkString ("DLList(", ", ", ")")
