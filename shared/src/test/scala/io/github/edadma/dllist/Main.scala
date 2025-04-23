package io.github.edadma.dllist

import scala.compiletime.uninitialized

object Main extends App {

  val l = DLList(C(3), C(4))

  println(l)

}

case class C(asdf: Int) extends NodeRef {

  private var n: DLList[C]#Node = uninitialized

  def ref(node: DLList[Any]#Node): Unit = n = node.asInstanceOf[DLList[C]#Node]

  override def toString: String = s"<$asdf, ${n.following}>"
}
