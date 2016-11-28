package ru.innopolis.ir.project

import java.io.File

import ru.innopolis.ir.project.core.preprocessing.NormalizedDocument

import scala.collection.parallel.mutable.ParArray

/**
  * @author Timur Kasatkin
  * @date 19.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
package object core {

	def readDocumentsFrom(dir: String, maxCount: Int = 0): Array[Document] =
		readDocumentsFrom(new File(dir), maxCount)

	def readDocumentsFrom(dir: File): Array[Document] = {
		dir.listFiles.sorted map Document.fromFile
	}

	def readDocumentsInParallelFrom(dir: File): ParArray[Document] = {
		dir.listFiles.par map Document.fromFile
	}

	def readDocumentsFrom(dir: File, maxCount: Int): Array[Document] = {
		var docFiles: Array[File] = dir.listFiles.sorted
		docFiles = if (maxCount > 0) docFiles take maxCount else docFiles
		docFiles map Document.fromFile
	}

	def readNormalizedDocumentsFrom(dir: String): Iterable[NormalizedDocument] =
		readNormalizedDocumentsFrom(new File(dir))

	def readNormalizedDocumentsFrom(dir: File): Iterable[NormalizedDocument] = {
		dir.listFiles().view.map(NormalizedDocument.fromFile)
	}


}
