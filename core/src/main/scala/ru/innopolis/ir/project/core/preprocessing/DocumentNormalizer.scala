package ru.innopolis.ir.project.core.preprocessing

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

}
