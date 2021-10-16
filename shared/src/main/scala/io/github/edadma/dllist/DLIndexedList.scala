package io.github.edadma.dllist

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

object DLIndexedList {

  def apply[T](elems: T*): DLIndexedList[T] =
    new DLIndexedList[T] {
      appendAll(elems)
    }

}

class DLIndexedList[T] extends DLList[T] {

  protected lazy val array = new ArrayBuffer[Node]

  override def clear(): Unit = {
    super.clear()
    array.clear()
  }

  override def appendElement(elem: T): Node = {
    val n = super.appendElement(elem)

    array += n
    n
  }

  override def node(n: Int): Node = {
    require(0 <= n && n < count, s"node index out of range: $n")

    array(n)
  }

  override def prependElement(elem: T): Node = {
    val n = super.prependElement(elem)

    n +=: array
    n
  }

  override def insertAll(n: Int, elems: IterableOnce[T]): Unit =
    array.insertAll(n, super._insertAll(n, elems).iterator take n toList)

  override def remove(n: Int): T = {
    super.remove(n)
    array.remove(n).element
  }

  override def toString: String = iterator mkString ("DLIndexedList(", ", ", ")")

}
