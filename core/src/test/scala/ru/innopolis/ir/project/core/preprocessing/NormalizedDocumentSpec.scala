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
		with BeforeAndAfter
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

	"NormalizedDocument" should "be correctly read using 'readFrom'" in {
		val testFile1 = new File(getClass.getResource("/testNormalizedDocs/1").toURI)
		val result = NormalizedDocument.fromFile(testFile1)
		result should have(
			'id (1),
			'title ("Test doc"),
			'abstract ("I am a test doc!")
		)
		result.url.toString shouldBe "https://en.wikipedia.org/wiki/maybe_exists"
		result.termToFrequencyMap should contain only(
			"i" -> 2,
			"am" -> 1,
			"a" -> 1,
			"test" -> 1,
			"doc" -> 1,
			"yep" -> 1,
			"have" -> 1,
			"an" -> 1,
			"empty" -> 1,
			"line" -> 1
		)
	}

	it should "be saved to file in right format" in {
		testDoc.saveToFile(testDocDir)

		val resultFile = new File(testDocDir, testDoc.id.toString)

		testDocDir.listFiles should contain(resultFile)
		val lines = Source.fromFile(resultFile).getLines
		lines.next shouldBe testDoc.title
		lines.next shouldBe testDoc.url.toString
		lines.next shouldBe testDoc.`abstract`
		lines.next shouldBe (testDoc.termToFrequencyMap.map { case (term, tf) => s"$term→$tf" } mkString " ")
	}

	after {
		val resFile = new File(testDocDir, testDoc.id.toString)
		if (resFile.exists) resFile.delete()
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
