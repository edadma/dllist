package xyz.hyperreal.dllist

import org.scalatest._
import prop.PropertyChecks


class DLListTests extends FreeSpec with PropertyChecks with Matchers {

	"empty" in {
		val l = new DLList

		l.isEmpty shouldBe true
		l.length shouldBe 0
		l.iterator.isEmpty shouldBe true
		l.reverseIterator.isEmpty shouldBe true
		l.toString shouldBe "DLList()"
	}

	"insertion" in {
		val l = new DLList[Int]

		l += 3
		l += 4
		l += 5
		l.length shouldBe 3
		l.toString shouldBe "DLList(3, 4, 5)"
	}

	"iteration" in {
		val l = new DLList[Int]

		l ++= Seq( 3, 4, 5 )
		l.iterator.toList.reverse shouldBe l.reverseIterator.toList
	}

	"deletion" in {
		val l = DLList[Int]( 3, 4, 5, 6, 7, 8 )

		l.toList shouldBe List( 3, 4, 5, 6, 7, 8 )
		l.remove( 2 ) shouldBe 5
		l.toList shouldBe List( 3, 4, 6, 7, 8 )
		l.remove( 3 ) shouldBe 7
		l.toList shouldBe List( 3, 4, 6, 8 )
		l.clear
		l.length shouldBe 0
		l.toList shouldBe Nil
	}

}
