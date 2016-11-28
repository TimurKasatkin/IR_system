package ru.innopolis.ir.project.core.utils

import java.io.File

import org.scalatest.{FlatSpec, Matchers}

import ru.innopolis.ir.project.core.readNormalizedDocumentsFrom

/**
  * @author Timur Kasatkin 
  * @date 26.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
class CollectionIteratorSpec extends FlatSpec with Matchers {

	val testFilesFolder = new File(getClass.getResource("/testCollection").getPath)

	"CollectionIterator" should "read all document triples" in {
		val result = CollectionIterator(testFilesFolder).toList

		val requiredResult = readNormalizedDocumentsFrom(testFilesFolder.getPath)
			.flatMap(doc => doc.termToFrequencyMap.map { case (term, tf) => (term, doc.id, tf) })
			.toList

		result should contain theSameElementsAs requiredResult
	}


}
