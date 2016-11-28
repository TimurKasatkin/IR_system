package ru.innopolis.ir.project.core.index

import java.io.File

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import ru.innopolis.ir.project.core.preprocessing.QueryNormalizer
import ru.innopolis.ir.project.core.utils.CollectionIterator

/**
  * @author Timur Kasatkin 
  * @date 28.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
class VectorSpaceModelIndexSpec extends FlatSpec with Matchers with BeforeAndAfter {

	val testFilesFolder = new File(getClass.getResource("/testCollection").getPath)

	val testIndexPostingsFile = new File("index_postings")
	val testIndexObjectFile = new File("index")
	val testQuery = "instructions set"

	"VectorSpaceModelIndex" should "be correctly saved to and read from file" in {
		var index = VectorSpaceModelIndex(CollectionIterator(testFilesFolder), testIndexPostingsFile)
		val beforeSaveSearchResult = index.search(QueryNormalizer(testQuery), 1, 10)
		index.save(testIndexObjectFile)
		index = VectorSpaceModelIndex.fromFile(testIndexObjectFile)
		val afterSaveSearchResult = index.search(QueryNormalizer(testQuery), 1, 10)

		afterSaveSearchResult._1 should contain theSameElementsAs beforeSaveSearchResult._1
		afterSaveSearchResult._2 shouldEqual beforeSaveSearchResult._2
	}

	after {
		testIndexPostingsFile.delete()
		testIndexObjectFile.delete()
	}

}
