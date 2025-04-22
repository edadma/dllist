package io.github.edadma.dllist

object Main extends App {

  val l = DLList(C(3), C(4))

  println(l)

}

case class C(asdf: Int) extends NodeRef {

  private var n: DLList[C]#Node = _

  def ref(node: DLList[Any]#Node) = n = node.asInstanceOf[DLList[C]#Node]

  override def toString: String = s"<$asdf, ${n.following}>"
}
