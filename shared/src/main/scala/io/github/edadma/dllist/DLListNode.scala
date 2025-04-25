package io.github.edadma.dllist

import scala.collection.mutable.ListBuffer

/** A node in the doubly-linked list. */
class DLListNode[T](
    private[dllist] owner: DLList[T],
    private[dllist] var prev: DLListNode[T],
    private[dllist] var next: DLListNode[T],
    init: T,
):
  private[dllist] var v = init

  /** Guard against operations on an unlinked node. */
  private def assertLinked(): Unit =
    require(prev != null || next != null, "operation on unlinked (orphaned) node")

  /** Constructs an empty sentinel node. */
//  private[dllist] def this() = this(owner, null, null, null.asInstanceOf[T])

  def element: T = v

  def element_=(value: T): Unit = v = value

  /** Gets the next node in the list. */
  def following: DLListNode[T] =
    assertLinked()
    next

  /** Gets the previous node in the list. */
  def preceding: DLListNode[T] =
    assertLinked()
    prev

  def isBeforeStart: Boolean = eq(owner.startSentinel)

  def notBeforeStart: Boolean = ne(owner.startSentinel)

  def isAfterEnd: Boolean = eq(owner.endSentinel)

  def notAfterEnd: Boolean = ne(owner.endSentinel)

  /** Sentinel guard (cannot unlink sentinel). */
  private def checkUnlink(): Unit =
    require(this ne owner.startSentinel, "can't unlink the start sentinel")
    require(this ne owner.endSentinel, "can't unlink the end sentinel")

  /** Removes this node from the list and returns its element. */
  def unlink: T =
    checkUnlink()
    assertLinked()
    next.prev = prev
    prev.next = next
    owner.elemCount -= 1
    // orphan
    prev = null
    next = null
    v

  /** Inserts a new node with the given element after this node. */
  infix def follow(elem: T): DLListNode[T] =
    assertLinked()
    val node = new DLListNode(owner, this, next, elem)
    elem match
      case ref: NodeRef[T] @unchecked => ref ref node
      case _                          =>
    next.prev = node
    next = node
    owner.elemCount += 1
    node

  /** Inserts a new node with the given element before this node. */
  infix def precede(elem: T): DLListNode[T] =
    assertLinked()
    val node = new DLListNode(owner, prev, this, elem)
    elem match
      case ref: NodeRef[T] @unchecked => ref ref node
      case _                          =>
    prev.next = node
    prev = node
    owner.elemCount += 1
    node

  /** Unlinks this node and all nodes until (exclusive) the specified node. */
  def unlinkUntil(node: DLListNode[T]): Seq[T] =
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
      owner.elemCount -= 1
      cur = cur.next // safe since cur.next points to node or next
    buf.toList

  /** Determines if this node comes before the specified node. */
  def isBefore(node: DLListNode[T]): Boolean =
    assertLinked()
    if (this eq node) || isAfterEnd then false
    else
      var cur = this
      while cur.notAfterEnd && (cur ne node) do cur = cur.following
      node.isAfterEnd || cur.notAfterEnd

  /** Determines if this node comes after the specified node. */
  def isAfter(node: DLListNode[T]): Boolean =
    assertLinked()
    if (this eq node) || isBeforeStart then false
    else
      var cur = this
      while cur.notBeforeStart && (cur ne node) do cur = cur.preceding
      node.isBeforeStart || cur.notBeforeStart

  /** Creates an iterator that traverses nodes until (exclusive) the specified node. */
  def iteratorUntil(last: DLListNode[T]): Iterator[DLListNode[T]] =
    assertLinked()
    new Iterator[DLListNode[T]]:
      private var node     = DLListNode.this
      private var nextNode = node.next

      def hasNext: Boolean = (node ne last) && (node ne owner.endSentinel)

      def next(): DLListNode[T] =
        if !hasNext then throw new NoSuchElementException("no more elements")
        val res = node
        node = nextNode
        if hasNext then nextNode = node.next
        res

  /** Creates an iterator that traverses from this node to the end. */
  def iterator: Iterator[DLListNode[T]] = iteratorUntil(owner.endSentinel)

  /** Creates a reverse iterator until (exclusive) the specified node. */
  def reverseIteratorUntil(last: DLListNode[T]): Iterator[DLListNode[T]] =
    assertLinked()
    new Iterator[DLListNode[T]]:
      private var node = DLListNode.this

      def hasNext: Boolean = (node ne last) && (node ne owner.startSentinel)

      def next(): DLListNode[T] =
        if !hasNext then throw new NoSuchElementException("no more elements")
        val res = node
        node = node.prev
        res

  /** Creates a reverse iterator to the start. */
  def reverseIterator: Iterator[DLListNode[T]] = reverseIteratorUntil(owner.startSentinel)

  /** Finds the first node forward satisfying the predicate. */
  def find(p: T => Boolean): Option[DLListNode[T]] =
    assertLinked()
    iterator find (n => p(n.v))

  /** Finds the first node backward satisfying the predicate. */
  def reverseFind(p: T => Boolean): Option[DLListNode[T]] =
    assertLinked()
    reverseIterator find (n => p(n.v))

  /** Finds the index of this node. */
  def index: Int =
    assertLinked()
    var cur: DLListNode[T] = owner.startSentinel
    var idx                = -1
    while cur.notAfterEnd && ne(cur) do
      cur = cur.next
      idx += 1
    if cur.notAfterEnd then idx else -1

  /** Skips forward n nodes. */
  def skipForward(n: Int): DLListNode[T] =
    assertLinked()
    if n > 0 then following.skipForward(n - 1) else this

  /** Skips backward n nodes. */
  def skipReverse(n: Int): DLListNode[T] =
    assertLinked()
    if n > 0 then preceding.skipReverse(n - 1) else this

  override def toString: String = s"node[$v]"
end DLListNode
