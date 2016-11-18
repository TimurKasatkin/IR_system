package ru.innopolis.ir.project.core.index

import ru.innopolis.ir.project.core.preprocessing.NormalizedDocument

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * @author Timur Kasatkin 
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
class VectorSpaceModelIndex(docs: Iterable[NormalizedDocument]) {


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

		for (termInfo <- queryTerms.view.map(dictionary(_))) {
			for (posting <- termInfo.postings) {
				scores(posting.docId) += docWeight(posting.termFrequency, termInfo.docFrequency)
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

	private def docWeight(tf: Int, df: Int) = logarithmicTermFrequency(tf) * invertedDocFrequency(df)

	private def logarithmicTermFrequency(tf: Int): Double = 1 + math.log(tf | 1)

	private def invertedDocFrequency(df: Int): Double = math.log(docsCount.toDouble / df)

	private case class TermInfo(docFrequency: Int, postings: List[Posting])

	private case class Posting(docId: Int, termFrequency: Int)


}
