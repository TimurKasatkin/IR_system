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
case class Document(id: Int, title: String, url: URL, `abstract`:String, text: String)

object Document {

	def fromFile(docPath: String): Document = fromFile(new File(docPath))

	def fromFile(docFile: File): Document = {
		val linesIterator = Source.fromFile(docFile).getLines
		val url = new URL(linesIterator.next())
		val title = linesIterator.next()
		val `abstract` = linesIterator.takeWhile(_.trim.nonEmpty).mkString(" ")
		Document(
			id = docFile.getName.toInt,
			title = title,
			url = url,
			`abstract` = `abstract`,
			text = `abstract` + " " + linesIterator.filterNot(_.trim.isEmpty).mkString(" ")
		)
	}
}
