package ru.innopolis.ir.project

import java.io.File

/**
  * @author Timur Kasatkin
  * @date 19.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
package object core {

	def readDocumentsFrom(dir: String, maxCount: Int = 0): Array[Document] = {
		var docFiles: Array[File] = new File(dir).listFiles.sorted
		docFiles = if (maxCount > 0) docFiles take maxCount else docFiles
		docFiles map Document.fromFile
	}


}
