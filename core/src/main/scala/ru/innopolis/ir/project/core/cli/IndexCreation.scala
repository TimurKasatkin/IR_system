package ru.innopolis.ir.project.core.cli

import java.io.File

import ru.innopolis.ir.project.core.index.VectorSpaceModelInMemoryIndex
import ru.innopolis.ir.project.core.preprocessing.{NormalizedDocument, QueryNormalizer}
import ru.innopolis.ir.project.core.utils.time

/**
  * @author Timur Kasatkin 
  * @date 24.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object IndexCreation {

	def main(args: Array[String]): Unit = {
		val parser = new scopt.OptionParser[Config]("indexCreator") {
			arg[File]("<docs_folder>")
				.action((f, c) => c.copy(normalizedDocsFolder = f))
				.text("Path to folder with normalized documents")
			opt[Seq[String]]('q', "queries")
				.optional()
				.valueName("<query 1>,<query 2>,...")
				.action((qs, c) => c.copy(queries = qs))
				.text("Queries to perform after index is built")
		}

		parser.parse(args, Config()) match {
			case Some(config) =>
				val docs = config.normalizedDocsFolder.listFiles.view
					.map(NormalizedDocument.fromFile)
				println("Building index...")
				val index = time(VectorSpaceModelInMemoryIndex(docs))
				val docIdToTitle = config.normalizedDocsFolder.listFiles.view.map(NormalizedDocument.fromFile).map(d => (d.id, d.title)).toMap
				println("Performing queries...")
				for (query <- config.queries) {
					println("=" * 40)
					println(s"Results for query '$query'")
					println("=" * 40)
					val results = index.search(QueryNormalizer(query))
					results.map(res => (res.docId, docIdToTitle(res.docId), res.score)).foreach(println)
				}
			case None =>
		}
	}

	private case class Config(normalizedDocsFolder: File = null, queries: Seq[String] = Seq())

}
