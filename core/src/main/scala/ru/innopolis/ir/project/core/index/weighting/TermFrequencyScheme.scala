package ru.innopolis.ir.project.core.index.weighting

/**
  * @author Timur Kasatkin
  * @date 24.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
/**
  * Function that accepts term frequencies and returns their changed versions
  */
trait TermFrequencyScheme extends (Iterable[Int] => Iterable[Double]) {
	override def apply(tfs: Iterable[Int]): Iterable[Double]
}

object NaturalTFScheme extends TermFrequencyScheme {
	override def apply(tfs: Iterable[Int]): Iterable[Double] =
		tfs.map(_.toDouble)
}

object LogarithmicTFScheme extends TermFrequencyScheme {
	override def apply(tfs: Iterable[Int]): Iterable[Double] = tfs.map(tf => 1 + math.log(tf))
}

class NormalizedTFScheme(alpha: Float = 0.5f) extends TermFrequencyScheme {
	override def apply(tfs: Iterable[Int]): Iterable[Double] = {
		val max = tfs.max
		tfs.map(alpha + (1 - alpha) * _.toDouble / max)
	}
}

object NormalizedTFScheme {
	def apply(alpha: Float = 0.5f): NormalizedTFScheme = new NormalizedTFScheme(alpha)
}

object BooleanTFScheme extends TermFrequencyScheme {
	override def apply(tfs: Iterable[Int]): Iterable[Double] =
		tfs.map(v => if (v > 0) v.toDouble else 0)
}

object LogarithmicAverageTFScheme extends TermFrequencyScheme {
	override def apply(tfs: Iterable[Int]): Iterable[Double] = {
		val aveLog = math.log(tfs.sum / tfs.size)
		tfs.map(x => (1 + math.log(x)) / (1 + aveLog))
	}
}
