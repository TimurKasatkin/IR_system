package ru.innopolis.ir.project.core.utils

import java.io.File

import ru.innopolis.ir.project.core.preprocessing.NormalizedDocument

/**
  * @author Timur Kasatkin 
  * @date 26.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
class CollectionIterator(dir: File) extends Iterator[(String, Int, Int)] {

	require(dir.exists, "Specified folder doesn't exists")
	require(dir.isDirectory, "Specified file is not directory")

	private val filesIterator = dir.listFiles().iterator

	private var (curDocId, curDocTermToFreqIterator, hasMore) = {
		if (filesIterator.hasNext) {
			val (docId, termToFreqIterator) = readNextDoc()
			(docId, termToFreqIterator, filesIterator.hasNext)
		} else
			(null.asInstanceOf[Int], null.asInstanceOf[Iterator[(String, Int)]], false)
	}

	override def hasNext: Boolean = hasMore

	override def next(): (String, Int, Int) = {
		var result = null.asInstanceOf[(String, Int, Int)]
		if (curDocTermToFreqIterator.hasNext) {
			val (term, tf) = curDocTermToFreqIterator.next()
			result = (term, curDocId, tf)
		}
		if (!curDocTermToFreqIterator.hasNext) {
			if (filesIterator.hasNext) {
				val (curDocId, curDocTermToFreqIterator) = readNextDoc()
				this.curDocId = curDocId
				this.curDocTermToFreqIterator = curDocTermToFreqIterator
			} else {
				curDocId = null.asInstanceOf[Int]
				curDocTermToFreqIterator = null.asInstanceOf[Iterator[(String, Int)]]
				hasMore = false
			}
		}
		result
	}

	private def readNextDoc(): (Int, Iterator[(String, Int)]) = {
		val doc = NormalizedDocument.fromFile(filesIterator.next())
		(doc.id, doc.termToFrequencyMap.iterator)
	}

}

object CollectionIterator {
	def apply(dir: File): CollectionIterator = new CollectionIterator(dir)
}
