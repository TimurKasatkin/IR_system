package ru.innopolis.ir.project.core.preprocessing

import java.io.File

import ru.innopolis.ir.project.core.{Document, readDocumentsInParallelFrom}
import ru.innopolis.ir.project.core.utils.StringIterableExtension

/**
  * @author Timur Kasatkin
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object DocumentNormalizer {

	def apply(doc: Document): NormalizedDocument = {
		val termToFrequency: Map[String, Int] = StringNormalizer(doc.text).wordCounts
		NormalizedDocument(
			id = doc.id,
			title = doc.title,
			url = doc.url,
			`abstract` = doc.`abstract`,
			termToFrequencyMap = termToFrequency
		)
	}

	def normalizeAllFromAndSaveTo(fromDir: File,
	                              saveDir: File,
	                              removeSourceDocs: Boolean = true): Unit = {
		require(fromDir.exists, "Source folder does not exists.")
		require(fromDir.isDirectory, "Source path is not a folder.")

		if (!saveDir.exists()) saveDir.mkdir()

		fromDir.listFiles.par foreach { f =>
			val normedDoc = this(Document.fromFile(f))
			normedDoc.saveToFile(saveDir)
			if (removeSourceDocs)
				f.delete()
		}
	}

}
