package ru.innopolis.ir.project.core.cli

import ru.innopolis.ir.project.core.index._
import ru.innopolis.ir.project.core.preprocessing.{DocumentNormalizer, QueryNormalizer}
import ru.innopolis.ir.project.core.{Document, readDocumentsFrom}
import ru.innopolis.ir.project.core.utils.time

/**
  * @author Timur Kasatkin
  * @date 19.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object TestMain {

	def main(args: Array[String]): Unit = {
		require(args.length > 0, "Please specify path to folder with doc files")
		println("Normalizing documents ...")
		val documents: Array[Document] = readDocumentsFrom(args(0), 50)
		val docIdToTitle = documents.map(d => (d.id, d.title)).toMap
		val normedDocs = time(documents.map(DocumentNormalizer(_)))
		println("Building index ...")
		val vsmIndex = time(new VectorSpaceModelInMemoryIndex(normedDocs))
		val query = "intel"
		println(s"Result of query '$query':")
		println(vsmIndex.search(QueryNormalizer(query))
			.map { res => (docIdToTitle(res.docId), res.score) }
			.mkString("\n"))
	}

}
