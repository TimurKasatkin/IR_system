package ru.innopolis.ir.project.core

import java.io.File
import java.net.URL

import scala.io.Source

/**
  * @author Timur Kasatkin 
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
case class Document(id: Int, title: String, url: URL, text: String)

object Document {

	def fromFile(docPath: String): Document = fromFile(new File(docPath))

	def fromFile(docFile: File): Document = {
		val lines = Source.fromFile(docFile).getLines
		val url = new URL(lines.next())
		val title = lines.next()
		Document(
			id = docFile.getName.toInt,
			title = title,
			url = url,
			text = lines.filterNot(_.trim.isEmpty).mkString(" ")
		)
	}
}
