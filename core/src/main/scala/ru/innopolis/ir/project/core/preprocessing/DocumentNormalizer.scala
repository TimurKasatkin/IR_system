package ru.innopolis.ir.project.core.preprocessing

import ru.innopolis.ir.project.core.Document

/**
  * @author Timur Kasatkin
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object DocumentNormalizer {

	def apply(document: Document): NormalizedDocument = {
		val termToFrequency: Map[String, Int] = StringNormalizer(document.text)
			.foldLeft(Map.empty[String, Int].withDefaultValue(0)) {
				(count, word) => count + (word -> (count(word) + 1))
			}
		NormalizedDocument(document.id, termToFrequency)
	}

}
