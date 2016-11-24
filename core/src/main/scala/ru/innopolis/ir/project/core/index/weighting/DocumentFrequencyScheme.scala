package ru.innopolis.ir.project.core.index.weighting

/**
  * @author Timur Kasatkin 
  * @date 24.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
/**
  * Function that accepts doc frequency and number of docs, and returns doc weight
  */
trait DocumentFrequencyScheme extends ((Int, Int) => Double) {
	override def apply(df: Int, numOfDocs: Int): Double
}

object NoDFScheme extends DocumentFrequencyScheme {
	override def apply(df: Int, numOfDocs: Int): Double = 1
}

object InvertedDFScheme extends DocumentFrequencyScheme {
	override def apply(df: Int, numOfDocs: Int): Double = math.log(numOfDocs.toDouble / df)
}
