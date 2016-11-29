package ru.innopolis.ir.project.core.preprocessing

import java.io._
import java.net.URL

import ru.innopolis.ir.project.core.utils.using

/**
  * @author Timur Kasatkin 
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
/**
  * @param termToFrequencyMap map in format (term, term's frequency)
  */
@SerialVersionUID(4849138440517517685L)
case class NormalizedDocument(id: Int, title: String, url: URL, `abstract`: String, termToFrequencyMap: Map[String, Int])
	extends Serializable {

	def saveTo(directory: File, docName: String = id.toString): Unit = {
		using(new ObjectOutputStream(new FileOutputStream(new File(directory, docName))))(_.close()) {
			_.writeObject(this)
		}
	}

}

object NormalizedDocument {

	def fromFile(f: File): NormalizedDocument =
		using(new ObjectInputStream(new FileInputStream(f)))(_.close()) {
			_.readObject().asInstanceOf[NormalizedDocument]
		}.getOrElse(null)
}