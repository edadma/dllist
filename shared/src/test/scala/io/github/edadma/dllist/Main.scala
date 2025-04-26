package io.github.edadma.dllist

object Main extends App {

  val list   = List(1, 2, 3, 4, 5)
  val dllist = DLList(list*)

//  println(dllist)

  println(dllist.headNode.unlinkUntil(dllist.lastNode))
  println(dllist)

}
