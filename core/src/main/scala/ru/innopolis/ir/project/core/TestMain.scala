package ru.innopolis.ir.project.core

import ru.innopolis.ir.project.core.preprocessing.{DocumentNormalizer, QueryNormalizer}
import ru.innopolis.ir.project.core.index._

/**
  * @author Timur Kasatkin 
  * @date 19.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object TestMain {

	def time[R](block: => R): R = {
		val t0 = System.nanoTime()
		val result = block    // call-by-name
		val t1 = System.nanoTime()
		println("Elapsed time: " + (t1 - t0) + "ns")
		result
	}

	def main(args: Array[String]) = {
		require(args.length > 0, "Please specify path to folder with doc files")
		println("Normalizing documents ...")
		val documents: Array[Document] = readDocumentsFrom(args(0), 50)
		val docIdToTitle = documents.map(d=>(d.id, d.title)).toMap
		val normedDocs = time(documents.map(DocumentNormalizer(_)))
		println("Building index ...")
		val vsmIndex = time(new VectorSpaceModelIndex(normedDocs))
		println(vsmIndex.search(QueryNormalizer("processors")).map(_.docId).map(docIdToTitle(_)).mkString("\n"))
	}

}
