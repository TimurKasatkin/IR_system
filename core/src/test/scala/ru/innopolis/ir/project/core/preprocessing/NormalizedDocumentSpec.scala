package ru.innopolis.ir.project.core.preprocessing

import java.io.File
import java.net.URL

import org.scalatest._

import scala.collection.mutable
import scala.io.Source

/**
  * @author Timur Kasatkin
  * @date 24.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
class NormalizedDocumentSpec
	extends FlatSpec
		with Matchers
		with BeforeAndAfterAll
		with BeforeAndAfterEach {

	val testDocDir = new File("testNormalized")

	val testDoc = NormalizedDocument(
		id = 5,
		title = "Test Title 5",
		url = new URL("http://yandex.ru"),
		`abstract` = "I am a test abstract",
		termToFrequencyMap = mutable
			.LinkedHashMap(
				"i" -> 1,
				"am" -> 3,
				"test" -> 2,
				"text" -> 4
			).toMap
	)

	override protected def beforeAll(): Unit = {
		testDocDir.mkdir()
	}

	"NormalizedDocument" should "be correctly saved using \"saveTo\" and read using \"fromFile\"" in {
		testDoc.saveTo(testDocDir)

		val result = NormalizedDocument.fromFile(new File(testDocDir, testDoc.id.toString))

		result shouldEqual testDoc
	}

	override protected def afterEach(): Unit = {
		testDocDir.listFiles foreach {
			_.delete()
		}
	}

	override protected def afterAll(): Unit = {
		testDocDir.delete()
	}

}
