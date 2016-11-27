package ru.innopolis.ir.project.core.preprocessing

import java.io._
import java.net.URL

import ru.innopolis.ir.project.core.utils.using

import scala.io.Source

/**
  * @author Timur Kasatkin 
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
/**
  * @param termToFrequencyMap map in format (term, term's frequency)
  */
case class NormalizedDocument(id: Int, title: String, url: URL, `abstract`: String, termToFrequencyMap: Map[String, Int]) {

	def saveToFile(directory: File): Unit = {
		using(new BufferedWriter(new FileWriter(new File(directory, id.toString))))(_.close) {
			_.write(this.toString)
		}
	}

	override def toString: String = {
		import NormalizedDocument.TermTfSeparator
		s"""$title
		   |$url
		   |${`abstract`}
		   |${termToFrequencyMap.map { case (term, tf) => s"$term$TermTfSeparator$tf" } mkString " "}""".stripMargin
	}

}

object NormalizedDocument {

	private[preprocessing] val TermTfSeparator = '→'

	def fromFile(f: File): NormalizedDocument = {
		val linesIterator = Source.fromFile(f).getLines
		val id = f.getName.toInt
		val title = linesIterator.next
		val url = new URL(linesIterator.next)
		val `abstract` = if (linesIterator.hasNext) linesIterator.next else ""
		val termToFrequencyMap = if (linesIterator.hasNext) {
			linesIterator.next.split(" ").view
				.map(_.split(TermTfSeparator))
				.map(s => {
					(s(0), s(1).toInt)
				})
				.toMap
		} else Map.empty[String, Int]
		NormalizedDocument(
			id = id,
			title = title,
			url = url,
			`abstract` = `abstract`,
			termToFrequencyMap = termToFrequencyMap
		)
	}
}