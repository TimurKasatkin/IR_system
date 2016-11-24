package ru.innopolis.ir.project.core.preprocessing

import java.io._
import java.net.URL

import ru.innopolis.ir.project.core.cleanly

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
	override def toString: String = {
		s"""$title
		   |$url
		   |${`abstract`}
		   |${termToFrequencyMap.map { case (term, tf) => s"$term→$tf" } mkString " "}""".stripMargin
	}

	def saveToFile(directory: File): Unit = {
		cleanly(new BufferedWriter(new FileWriter(new File(directory, id.toString))))(_.close) { writer =>
			writer.write(this.toString)
		}
	}

}

object NormalizedDocument {
	def fromFile(f: File): NormalizedDocument = {
		val lines = Source.fromFile(f).getLines
		val title = lines.next
		val url = new URL(lines.next)
		val `abstract` = lines.next
		val termToFrequencyMap = lines.next.split(" ").view
			.map(_.split("→"))
			.map(s => (s(0), s(1).toInt))
			.toMap
		NormalizedDocument(
			id = f.getName.toInt,
			title = title,
			url = url,
			`abstract` = `abstract`,
			termToFrequencyMap = termToFrequencyMap
		)
	}
}