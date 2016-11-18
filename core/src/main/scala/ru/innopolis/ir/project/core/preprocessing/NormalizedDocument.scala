package ru.innopolis.ir.project.core.preprocessing

/**
  * @author Timur Kasatkin 
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
/**
  * @param termToFrequencyMap map in format (term, term's frequency)
  */
case class NormalizedDocument(id: Int, termToFrequencyMap: Map[String, Int])
