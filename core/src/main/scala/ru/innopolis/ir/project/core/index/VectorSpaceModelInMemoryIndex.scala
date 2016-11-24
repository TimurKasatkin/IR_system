package ru.innopolis.ir.project.core.index

import ru.innopolis.ir.project.core.index.weighting._
import ru.innopolis.ir.project.core.preprocessing.NormalizedDocument

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * @author Timur Kasatkin
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
class VectorSpaceModelInMemoryIndex(docs: Iterable[NormalizedDocument],
                                    docTFScheme: TermFrequencyScheme = LogarithmicTFScheme,
                                    queryTFScheme: TermFrequencyScheme = BooleanTFScheme,
                                    queryDFScheme: DocumentFrequencyScheme = InvertedDFScheme) {

	private val (dictionary, docLengths, docsCount) = {

		val termToDocFrequency: mutable.Map[String, Int] = mutable.LinkedHashMap.empty.withDefaultValue(0)
		val termToPostingsList: mutable.Map[String, ListBuffer[Posting]] = mutable.LinkedHashMap.empty.withDefault(_ => ListBuffer.empty[Posting])
		val docLengths: mutable.Map[Int, Int] = mutable.Map.empty.withDefaultValue(0)

		var docsCount: Int = 0

		for (doc <- docs) {
			for ((term, termFrequency) <- doc.termToFrequencyMap) {
				termToDocFrequency(term) += 1
				termToPostingsList(term) = termToPostingsList(term) += Posting(doc.id, termFrequency)
				docLengths(doc.id) += termFrequency ^ 2
			}
			docsCount += 1
		}


		((termToDocFrequency zip termToPostingsList.values)
			.map { case ((term, docFrequency), postingsList) => (term, TermInfo(docFrequency, postingsList.toList)) }
			.toMap,
			docLengths.mapValues(math.sqrt(_)),
			docsCount
		)
	}

	def search(queryTerms: Iterable[String], numOfTopResults: Int = 100): List[SearchResult] = {
		val scores: mutable.Map[Int, Double] = mutable.Map.empty.withDefaultValue(0)

		val terms = queryTerms.filter(dictionary contains)

		val queryTermTFs = terms.foldLeft(Map.empty[String, Int].withDefaultValue(0)) {
			(count, term) => count + (term -> (count(term) + 1))
		}

		val queryTermsWeights = (queryTermTFs.keys.view zip queryTFScheme(queryTermTFs.values.view)).toMap

		for ((term, termInfo) <- terms.view.map(t => (t, dictionary(t)))) {
			val queryWeight = queryTermsWeights(term) * queryDFScheme(termInfo.docFrequency, docsCount)
			val postings = termInfo.postings
			for ((docId, docWeight) <- postings.view.map(_.docId) zip docTFScheme(postings.view.map(_.termFrequency))) {
				scores(docId) += docWeight * queryWeight
			}
		}

		val scoresHeap: mutable.PriorityQueue[(Int, Double)] = mutable.PriorityQueue()(Ordering.by(_._2))
		scoresHeap ++= scores.view.map { case (docId, score) => (docId, score / docLengths(docId)) }

		val result: ListBuffer[SearchResult] = ListBuffer.empty
		var i = 0
		while (scoresHeap.nonEmpty && i < numOfTopResults) {
			val (docId, score) = scoresHeap.dequeue()
			result += SearchResult(docId, score)
			i += 1
		}

		result.toList
	}

	private case class TermInfo(docFrequency: Int, postings: List[Posting])

	private case class Posting(docId: Int, termFrequency: Int)

}
