package ru.innopolis.ir.project.core.preprocessing

/**
  * @author Timur Kasatkin 
  * @date 19.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object QueryNormalizer {

	def apply(query: String): List[String] = StringNormalizer(query)

}
