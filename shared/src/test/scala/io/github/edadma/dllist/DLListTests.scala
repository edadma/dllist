//package io.github.edadma.dllist
//
//import org.scalatest.freespec.AnyFreeSpec
//import org.scalatest.matchers.should.Matchers
//
//class DLListTests extends AnyFreeSpec with Matchers {
//
//  "empty" in {
//    val l = new DLList
//
//    l.isEmpty shouldBe true
//    l.length shouldBe 0
//    l.iterator.isEmpty shouldBe true
//    l.reverseIterator.isEmpty shouldBe true
//    l.toString shouldBe "DLList()"
//  }
//
//  "insertion" in {
//    val l = new DLList[Int]
//
//    l += 3
//    l += 4
//    l += 5
//    l.length shouldBe 3
//    l.toString shouldBe "DLList(3, 4, 5)"
//  }
//
//  "insertion all" in {
//    val l = DLList[Int](3, 4, 5)
//
//    l.length shouldBe 3
//    l.toString shouldBe "DLList(3, 4, 5)"
//    l.insertAll(1, List(6, 7, 8))
//    l.length shouldBe 6
//    l.toString shouldBe "DLList(3, 6, 7, 8, 4, 5)"
//
//    val l1 = DLList[Int](3, 4, 5)
//
//    l1.insertAll(0, List(6, 7, 8))
//    l1.length shouldBe 6
//    l1.toString shouldBe "DLList(6, 7, 8, 3, 4, 5)"
//
//    val l2 = DLList[Int]()
//
//    l2.insertAll(0, List(6, 7, 8))
//    l2.length shouldBe 3
//    l2.toString shouldBe "DLList(6, 7, 8)"
//
//    val l3 = DLList[Int](3, 4, 5)
//
//    l3.insertAll(3, List(6, 7, 8))
//    l3.length shouldBe 6
//    l3.toString shouldBe "DLList(3, 4, 5, 6, 7, 8)"
//  }
//
//  "iteration" in {
//    val l = new DLList[Int]
//
//    l ++= Seq(3, 4, 5)
//    l.iterator.toList.reverse shouldBe l.reverseIterator.toList
//  }
//
//  "deletion" in {
//    val l = DLList[Int](3, 4, 5, 6, 7, 8)
//
//    l.toList shouldBe List(3, 4, 5, 6, 7, 8)
//    l.remove(2) shouldBe 5
//    l.toList shouldBe List(3, 4, 6, 7, 8)
//    l.remove(3) shouldBe 7
//    l.toList shouldBe List(3, 4, 6, 8)
//    l.clear()
//    l.length shouldBe 0
//    l.toList shouldBe Nil
//  }
//
//}
