package xyz.hyperreal.dllist

import scala.collection.mutable.ArrayBuffer


object DLIndexedList {

  def apply[T]( elems: T* ) =
    new DLIndexedList[T] {
      appendAll( elems )
    }

}

class DLIndexedList[T] extends DLList[T] {

  protected lazy val array = new ArrayBuffer[Node]

  override def clear: Unit = {
    super.clear
    array.clear
  }

  override def appendElement( elem: T ) = {
    val n = super.appendElement( elem )

    array += n
    n
  }

  override def node( n: Int ) = {
    require( 0 <= n && n < count, s"node index out of range: $n" )

    array(n)
  }

  override def prependElement( elem: T ) = {
    val n = super.prependElement( elem )

    n +=: array
    n
  }

  override def insertAll( n: Int, elems: Traversable[T] ) =
    array.insertAll( n, super._insertAll( n, elems ).iterator take n toList )

  override def remove( n: Int ) = {
    super.remove( n )
    array.remove( n ).element
  }

  override def toString = iterator mkString ("DLIndexedList(", ", ", ")")

}