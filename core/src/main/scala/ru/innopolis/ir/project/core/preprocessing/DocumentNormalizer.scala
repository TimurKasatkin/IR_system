package ru.innopolis.ir.project.core.preprocessing

import java.io.File

import ru.innopolis.ir.project.core.Document

/**
  * @author Timur Kasatkin
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object DocumentNormalizer {

	def apply(doc: Document): NormalizedDocument = {
		val termToFrequency: Map[String, Int] = StringNormalizer(doc.text)
			.foldLeft(Map.empty[String, Int].withDefaultValue(0)) { (count, word) =>
				count + (word -> (count(word) + 1))
			}
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
	                              removeSourceDocs: Boolean = true,
	                              verbose: Boolean = true,
	                              verboseFilesCountDelay: Int = 1000): Unit = {
		require(fromDir.exists, "Source folder does not exists.")
		require(fromDir.isDirectory, "Source path is not a folder.")

		if (!saveDir.exists()) saveDir.mkdir()
		var i = 0
		for (normedDoc <- fromDir.listFiles.view
			.map(Document.fromFile)
			.map(this (_))) {
			if (removeSourceDocs)
				new File(fromDir, normedDoc.id.toString).delete()
			normedDoc.saveToFile(saveDir)
			i += 1
			if (verbose && i > 0 && i % verboseFilesCountDelay == 0)
				println(s"$i docs processed...")
		}
		if (verbose) println("All docs are processed. ")
	}

}
