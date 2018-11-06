package xyz.hyperreal.dllist

import collection.mutable.{AbstractBuffer, ListBuffer}


object DLList {

  def apply[T]( elems: T* ) =
    new DLList[T] {
      appendAll( elems )
    }

}

class DLList[T] extends AbstractBuffer[T] {

  class Node( private [DLList] var prev: Node, private [DLList] var next: Node, init: T ) {

    private [DLList] def this() = this( null, null, null.asInstanceOf[T] )

    private [DLList] var v = init

    def element = v

    def element_=( value: T ) = v = value

    def following = next

    def preceding = prev

    def isBeforeStart = eq( startSentinel )

    def notBeforeStart = ne( startSentinel )

    def isAfterEnd = eq( endSentinel )

    def notAfterEnd = ne( endSentinel )

    def unlink = {
      checkUnlink
      next.prev = prev
      prev.next = next
      prev = null
      next = null
      count -= 1
      v
    }

    def follow( v: T ) = {
      val node = new Node( this, next, v )

      next.prev = node
      next = node
      count += 1
      node
    }

    def precede( v: T ) = {
      val node = new Node( prev, this, v )

      prev.next = node
      prev = node
      count += 1
      node
    }

    private def checkUnlink: Unit = {
      require( this ne startSentinel, "can't unlink the start sentinel" )
      require( this ne endSentinel, "can't unlink the end sentinel" )
    }

    def unlinkUntil( node: Node ) = {
      checkUnlink
      require( isBefore(node), "node to unlink up to must come after current node" )

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

    def isBefore( node: Node ) =
      if ((this eq node) || isAfterEnd)
        false
      else {
        var cur = this

        while (cur.notAfterEnd && (cur ne node))
          cur = cur.following

        node.isAfterEnd || cur.notAfterEnd
      }

    def iteratorUntil( last: Node ) =
      new Iterator[Node] {
        private var node = Node.this

        def hasNext = (node ne last) && (node ne endSentinel)

        def next = {
          if (isEmpty) throw new NoSuchElementException( "no more elements" )

          val res = node

          node = node.next
          res
        }
      }

    def iterator = iteratorUntil( endSentinel )

    def reverseIteratorUntil( last: Node ) =
      new Iterator[Node] {
        private var node = Node.this

        def hasNext = (node ne last) && (node ne startSentinel)

        def next = {
          if (isEmpty) throw new NoSuchElementException( "no more elements" )

          val res = node

          node = node.prev
          res
        }
      }

    def reverseIterator = reverseIteratorUntil( startSentinel )

    def find( p: T => Boolean ) = iterator find (n => p( n.element ))

    def reverseFind( p: T => Boolean ) = reverseIterator find (n => p( n.element ))

    def index = {
      var cur: Node = startSentinel
      var idx = -1

      while (cur.notAfterEnd && ne( cur )) {
        cur = cur.next
        idx += 1
      }

      if (cur.notAfterEnd) idx else -1
    }

    def skipForward( n: Int ): Node =
      if (n > 0)
        following.skipForward( n - 1 )
      else
        this

    def skipReverse( n: Int ): Node =
      if (n > 0)
        preceding.skipReverse( n - 1 )
      else
        this

    override def toString: String = s"node[$v]"
  }

  class Sentinel( val name: String ) extends Node {
    private def novalue = sys.error( s"$name has no value" )

    override def element = novalue

    override def element_=( v: T ) = novalue

    private def noiterator = sys.error( "can't iterate from sentinel" )

    override def iterator =
      if (this eq startSentinel)
        noiterator
      else
        super.iterator

    override def reverseIterator =
      if (this eq endSentinel)
        noiterator
      else
        super.reverseIterator

    override def toString: String = name
  }

  val startSentinel =
    new Sentinel( "start sentinel" ) {
      override def preceding = sys.error( s"$name has no preceding node" )
    }

  val endSentinel =
    new Sentinel( "end sentinel" ) {
      override def following = sys.error( s"$name has no following node" )
    }

  private var count = 0

  clear

  //
  // DLList operations
  //

  def appendElement( elem: T ) = endSentinel precede elem

  def headNode = {
    require( nonEmpty, "list is empty" )
    startSentinel.next
  }

  def headNodeOption = if (isEmpty) None else Some( headNode )

  def lastNode = {
    require( nonEmpty, "list is empty" )
    endSentinel.prev
  }

  def lastNodeOption = if (isEmpty) None else Some( lastNode )

  def node( n: Int ) = {
    require( 0 <= n && n < count, s"node index out of range: $n" )

    if (n == 0)
      headNode
    else if (n == count - 1)
      lastNode
    else if (n <= count/2)
      nodeIterator drop n next
    else
      reverseNodeIterator drop (count - 1 - n) next
  }

  def nodeIterator = startSentinel.next.iterator

  def reverseNodeIterator = endSentinel.prev.reverseIterator

  def nodeFind( p: T => Boolean ) = startSentinel.next.find( p )

  def reverseNodeFind( p: T => Boolean ) = endSentinel.prev.reverseFind( p )

  def prependElement( elem: T ) = startSentinel follow elem

  //
  // abstract Buffer methods
  //

  def +=( elem: T ) = {
    appendElement( elem )
    this
  }

  def +=:( elem: T ) = {
    prependElement( elem )
    this
  }

  def apply( n: Int ) = node( n ).v

  def clear: Unit = {
    startSentinel.next = endSentinel
    endSentinel.prev = startSentinel
    count = 0
  }

  def iterator = nodeIterator map (_.v)

  def length = count

  def insertAll( n: Int, elems: Traversable[T] ) = {
    var prev = node( n )

    elems foreach (e => prev = prev follow e)
  }

  def remove( n: Int ) = node( n ).unlink

  def update( n: Int, newelem: T ): Unit = node( n ).v = newelem

  //
  // overrides
  //

  override def head = headNode.v

  override def last = lastNode.v

  override def reverseIterator = reverseNodeIterator map (_.v)

  override def toString = iterator mkString ("DLList(", ", ", ")")

}