package xyz.hyperreal.dllist


trait NodeRef {

  def ref[T]( node: DLList[T]#Node )

}