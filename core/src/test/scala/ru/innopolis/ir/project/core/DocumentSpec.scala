package ru.innopolis.ir.project.core

import java.io.File

import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Timur Kasatkin 
  * @date 19.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
class DocumentSpec extends FlatSpec with Matchers {


	"Document" should "be correctly read using 'readFrom'" in {
		val testFile1 = new File(getClass.getResource("/testDocs/1").toURI)
		val result = Document.fromFile(testFile1)
		result should have (
			'id (1),
			'title ("Test doc"),
			'abstract ("I am a test doc!"),
			'text ("I am a test doc! Yep, I have an empty line.")
		)
		result.url.toString shouldBe "https://en.wikipedia.org/wiki/maybe_exists"
	}

}
