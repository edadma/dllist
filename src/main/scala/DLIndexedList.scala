package xyz.hyperreal.dllist

import scala.collection.mutable.ArrayBuffer


class DLIndexedList[T] extends DLList[T] {

  protected val array = new ArrayBuffer[Node]

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

//  override def insertAll( n: Int, elems: Traversable[T] ) = {
//    super.insertAll( n, elems )
//    array.insertAll( n, elems )
//  }

  override def remove( n: Int ) = node( n ).unlink

}