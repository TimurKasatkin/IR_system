package ru.innopolis.ir.project.core.preprocessing

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source



/**
  * @author Timur Kasatkin 
  * @date 19.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
private[preprocessing] object StringNormalizer {

	private val nlp = new StanfordCoreNLP("stanfordcorenlp.properties")

	private val stopWords = Source.fromFile(getClass.getResource("/stopwords/en.stopwords").toURI).getLines.map(_.trim).toSet

	//	val wordPattern = "\\D+.*".r

	def apply(str: String): List[String] = {
		val doc = new Annotation(str)
		nlp.annotate(doc)
		val result = mutable.ListBuffer.empty[String]
		for (sentence <- doc.get(classOf[SentencesAnnotation]);
		     token <- sentence.get(classOf[TokensAnnotation])) {
			val lemma = token.get(classOf[LemmaAnnotation])
			if (!(stopWords contains lemma))
				result += lemma.toLowerCase
		}

		result.toList
	}

}
