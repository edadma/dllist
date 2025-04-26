package io.github.edadma.dllist

import scala.collection.mutable.ListBuffer

/** A node in a doubly-linked list, holding a value of type `T` and links to its neighboring nodes.
  *
  * @param owner
  *   The DLList that owns this node.
  * @param prev
  *   The previous node in the list (or the start sentinel).
  * @param next
  *   The next node in the list (or the end sentinel).
  * @param init
  *   The initial value stored in this node.
  * @tparam T
  *   The element type.
  */
class DLListNode[T](
    private[dllist] owner: DLList[T],
    private[dllist] var prev: DLListNode[T],
    private[dllist] var next: DLListNode[T],
    init: T,
):
  /** The current value in this node. */
  private[dllist] var v: T = init

  /** Ensures this node is still linked into a list. */
  private def assertLinked(): Unit =
    require(prev != null || next != null, "operation on unlinked (orphaned) node")

  /** Gets the element stored in this node. */
  def element: T = v

  /** Updates the element in this node.
    *
    * @param value
    *   The new value to store.
    */
  def element_=(value: T): Unit = v = value

  /** Returns the next node in the list.
    *
    * @throws IllegalArgumentException
    *   if this node is unlinked.
    */
  def following: DLListNode[T] =
    assertLinked()
    next

  /** Returns the previous node in the list.
    *
    * @throws IllegalArgumentException
    *   if this node is unlinked.
    */
  def preceding: DLListNode[T] =
    assertLinked()
    prev

  /** `true` if this node is the start sentinel. */
  def isBeforeStart: Boolean = eq(owner.startSentinel)

  /** `true` if this node is not the start sentinel. */
  def notBeforeStart: Boolean = ne(owner.startSentinel)

  /** `true` if this node is the end sentinel. */
  def isAfterEnd: Boolean = eq(owner.endSentinel)

  /** `true` if this node is not the end sentinel. */
  def notAfterEnd: Boolean = ne(owner.endSentinel)

  /** Prevents unlinking a sentinel node. */
  private def checkUnlink(): Unit =
    require(this ne owner.startSentinel, "can't unlink the start sentinel")
    require(this ne owner.endSentinel, "can't unlink the end sentinel")

  /** Removes this node from the list and returns its element.
    *
    * @return
    *   The element that was stored in this node.
    * @throws IllegalArgumentException
    *   if called on a sentinel or an already-unlinked node.
    */
  def unlink: T =
    checkUnlink()
    assertLinked()
    // reconnect neighbors
    next.prev = prev
    prev.next = next
    owner.elemCount -= 1
    // detach
    prev = null
    next = null
    v

  /** Inserts a new node holding `elem` immediately after this node.
    *
    * @param elem
    *   The element to insert.
    * @return
    *   The newly created node.
    */
  infix def follow(elem: T): DLListNode[T] =
    assertLinked()
    val node = new DLListNode(owner, this, next, elem)
    // if the element is a DLListNodeRef, register the callback
    elem match
      case ref: DLListNodeRef[T] @unchecked => ref ref node
      case _                                =>
    next.prev = node
    next = node
    owner.elemCount += 1
    node

  /** Inserts a new node holding `elem` immediately before this node.
    *
    * @param elem
    *   The element to insert.
    * @return
    *   The newly created node.
    */
  infix def precede(elem: T): DLListNode[T] =
    assertLinked()
    val node = new DLListNode(owner, prev, this, elem)
    elem match
      case ref: DLListNodeRef[T] @unchecked => ref ref node
      case _                                =>
    prev.next = node
    prev = node
    owner.elemCount += 1
    node

  /** Unlinks this node and all nodes up to (but excluding) `node`, returning the removed elements in traversal order.
    *
    * @param node
    *   The node at which to stop (exclusive).
    * @return
    *   The list of elements removed.
    * @throws IllegalArgumentException
    *   if `node` does not follow this node.
    */
  def unlinkUntil(node: DLListNode[T]): Seq[T] =
    checkUnlink()
    assertLinked()
    require(isBefore(node), "node to unlink up to must come after current node")

    val buf = new ListBuffer[T]

    // splice out the whole block in one go:
    val before = this.prev
    before.next = node
    node.prev = before

    // now walk the block, detaching each node
    var cur = this

    while cur ne node do
      val nextCur = cur.next // grab the successor before we null it

      buf += cur.v
      owner.elemCount -= 1

      // detach the node completely
      cur.prev = null
      cur.next = null

      cur = nextCur

    buf.toList

  /** Tests whether this node comes before `node` in list order.
    *
    * @param node
    *   The node to compare against.
    * @return
    *   `true` if this node precedes `node` in the list.
    */
  def isBefore(node: DLListNode[T]): Boolean =
    assertLinked()
    if (this eq node) || isAfterEnd then false
    else
      var cur = this
      while cur.notAfterEnd && (cur ne node) do
        cur = cur.following
      node.isAfterEnd || cur.notAfterEnd

  /** Tests whether this node comes after `node` in list order.
    *
    * @param node
    *   The node to compare against.
    * @return
    *   `true` if this node follows `node` in the list.
    */
  def isAfter(node: DLListNode[T]): Boolean =
    assertLinked()
    if (this eq node) || isBeforeStart then false
    else
      var cur = this
      while cur.notBeforeStart && (cur ne node) do
        cur = cur.preceding
      node.isBeforeStart || cur.notBeforeStart

  /** Returns an iterator traversing from this node up to (but excluding) `last`.
    *
    * @param last
    *   The end sentinel or other node at which to stop.
    */
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

  /** A forward iterator from this node to the end sentinel. */
  def iterator: Iterator[DLListNode[T]] = iteratorUntil(owner.endSentinel)

  /** A reverse iterator from this node back to (but excluding) `last`.
    *
    * @param last
    *   The start sentinel or other node at which to stop.
    */
  def reverseIteratorUntil(last: DLListNode[T]): Iterator[DLListNode[T]] =
    assertLinked()
    new Iterator[DLListNode[T]]:
      private var node     = DLListNode.this
      def hasNext: Boolean = (node ne last) && (node ne owner.startSentinel)
      def next(): DLListNode[T] =
        if !hasNext then throw new NoSuchElementException("no more elements")
        val res = node
        node = node.prev
        res

  /** A reverse iterator from this node back to the start sentinel. */
  def reverseIterator: Iterator[DLListNode[T]] = reverseIteratorUntil(owner.startSentinel)

  /** Finds the first node forward whose element satisfies `p`.
    *
    * @param p
    *   Predicate on elements.
    * @return
    *   Some(node) if found, or None.
    */
  def find(p: T => Boolean): Option[DLListNode[T]] =
    assertLinked()
    iterator find (_.v match { case v if p(v) => true; case _ => false })

  /** Finds the first node backward whose element satisfies `p`.
    *
    * @param p
    *   Predicate on elements.
    * @return
    *   Some(node) if found, or None.
    */
  def reverseFind(p: T => Boolean): Option[DLListNode[T]] =
    assertLinked()
    reverseIterator find (_.v match { case v if p(v) => true; case _ => false })

  /** Computes this node’s zero-based index within the list.
    *
    * @return
    *   The index, or –1 if not linked.
    */
  def index: Int =
    assertLinked()
    var cur: DLListNode[T] = owner.startSentinel
    var idx                = -1
    while cur.notAfterEnd && ne(cur) do
      cur = cur.next
      idx += 1
    if cur.notAfterEnd then idx else -1

  /** Skips forward `n` nodes from here.
    *
    * @param n
    *   Number to skip.
    * @return
    *   The node reached.
    */
  def skipForward(n: Int): DLListNode[T] =
    assertLinked()
    if n > 0 then following.skipForward(n - 1) else this

  /** Skips backward `n` nodes from here.
    *
    * @param n
    *   Number to skip.
    * @return
    *   The node reached.
    */
  def skipReverse(n: Int): DLListNode[T] =
    assertLinked()
    if n > 0 then preceding.skipReverse(n - 1) else this

  /** String form showing the node’s value. */
  override def toString: String = s"node[$v]"
end DLListNode
