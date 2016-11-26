package ru.innopolis.ir.project.core.index

import java.io._

import ru.innopolis.ir.project.core.utils.using

import scala.collection.immutable.SortedMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * @author Timur Kasatkin
  * @date 25.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
/**
  * @param termDocIdTfTriples tuple in format (term, docId, term's frequency, )
  */
@SerialVersionUID(123L)
class VectorSpaceModelIndex(termDocIdTfTriples: Iterator[(String, Int, Int)],
                            postingsFile: File,
                            numOfTriplesPerBlock: Int = 1000000,
                            removeBlocksFiles: Boolean = true) extends Serializable {

	val VSM = VectorSpaceModelIndex

	private val (dictionary, docLengths, docsCount) = {
		var i = 0
		val blocksFiles = ListBuffer.empty[File]

		val docLengths = mutable.Map.empty[Int, Int].withDefaultValue(0)
		val docIds = mutable.HashSet.empty[Int]

		while (termDocIdTfTriples.hasNext) {
			// 1. Parse next block
			val nextBlock = termDocIdTfTriples.take(numOfTriplesPerBlock)

			// 2. Invert step
			val dictionary = mutable.HashMap
				.empty[String, MutableInterimTermInfo]
				.withDefault(_ => MutableInterimTermInfo())
			for ((term, docId, tf) <- nextBlock) {
				dictionary(term) = dictionary(term)
				dictionary(term).docFrequency += 1
				dictionary(term).postings += Tuple2(docId, tf)

				docLengths(docId) += tf ^ 2
				docIds += docId
			}
			val sortedDict = SortedMap[String, MutableInterimTermInfo](dictionary.toArray: _*)

			blocksFiles += new File(postingsFile.getPath + i.toString)
			using(new BufferedWriter(new FileWriter(blocksFiles.last)))(_.close) { writer =>
				for (s <- sortedDict.view map {
					case (term, interimTermInfo) =>
						s"$term ${interimTermInfo.docFrequency} ${
							interimTermInfo.postings.map {
								postingTuple2string
							} mkString " "
						}"
				}) {
					writer.write(s)
					writer.newLine()
				}
			}

			i += 1
		}

		val docsCount = docIds.size
		docIds.clear()
		System.gc()

		// 3. Merge blocks
		val dictionary = mutable.HashMap.empty[String, TermInfo]
		using(new BufferedOutputStream(new FileOutputStream(postingsFile, false)))(_.close()) { out =>

			val linesIterators = blocksFiles.map(Source.fromFile(_, VSM.BufferSize).getLines).toList
			val termInfosPriorQueue = mutable.PriorityQueue[((String, InterimTermInfo), Int)]()(
				Ordering.by[((String, InterimTermInfo), Int), String](_._1._1).reverse
			)

			termInfosPriorQueue ++= linesIterators.view
				.map(_.next())
				.map(_.toInterimTermInfoTuple)
				.zipWithIndex

			var ((termToSave, termInfoToSave), linesIteratorIndex) = termInfosPriorQueue.dequeue()

			linesIterators(linesIteratorIndex) doIfHasNext { s =>
				termInfosPriorQueue.enqueue((s.toInterimTermInfoTuple, linesIteratorIndex))
			}

			var curBytesOffset = 0

			while (termInfosPriorQueue.nonEmpty) {
				var ((curTerm, curTermInfo), linesIteratorIndex) = termInfosPriorQueue.dequeue()
				if (termToSave == curTerm)
					termInfoToSave += curTermInfo
				else {
					val postingsStrBytes = termInfoToSave.postings
						.map(postingTuple2string)
						.mkString(" ")
						.getBytes("UTF-8")
					out.write(postingsStrBytes)

					dictionary(termToSave) = TermInfo(
						termInfoToSave.docFrequency,
						curBytesOffset,
						postingsStrBytes.length
					)

					curBytesOffset += postingsStrBytes.length
					termToSave = curTerm
					termInfoToSave = curTermInfo
				}
				linesIterators(linesIteratorIndex) doIfHasNext { s =>
					termInfosPriorQueue.enqueue((s.toInterimTermInfoTuple, linesIteratorIndex))
				}
			}
		}

		if (removeBlocksFiles) {
			blocksFiles.foreach { _.delete() }
		}

		(dictionary, docLengths, docsCount)
	}

	private implicit class StringExtension(str: String) {
		def toInterimTermInfoTuple: (String, InterimTermInfo) = {
			val split = str.split(' ')
			val term = split(0)
			val docFrequency = split(1).toInt
			val postings = split.drop(2).view
				.map(_.split(VSM.DocIdTermFrequencySeparator))
				.map(spl => (spl(0), spl(1)))
				.map { case (docIdStr, tfStr) => (docIdStr.toInt, tfStr.toInt) }
				.toList

			(term, InterimTermInfo(docFrequency, postings))
		}
	}

	private implicit def postingTuple2string(t: (Int, Int)): String =
		s"${t._1}${VSM.DocIdTermFrequencySeparator}${t._2}"

	private implicit class IteratorExtensions[A](iter: Iterator[A]) {
		def doIfHasNext(f: A => Unit): Unit = if (iter.hasNext) f(iter.next())
	}

	private case class TermInfo(docFrequency: Int, postingsByteOffset: Int, postingsByteLength: Int)

	private case class InterimTermInfo(docFrequency: Int = 0, postings: List[(Int, Int)] = List()) {
		def +(other: InterimTermInfo): InterimTermInfo = InterimTermInfo(
			this.docFrequency + other.docFrequency,
			this.postings ++ other.postings
		)
	}

	private case class MutableInterimTermInfo(var docFrequency: Int = 0,
	                                          postings: ListBuffer[(Int, Int)] = ListBuffer()) {
		def +(other: MutableInterimTermInfo): MutableInterimTermInfo = MutableInterimTermInfo(
			this.docFrequency + other.docFrequency,
			this.postings ++ other.postings
		)
	}


}

object VectorSpaceModelIndex {
	private final val BufferSize: Int = 8 * Source.DefaultBufSize

	private final val DocIdTermFrequencySeparator: Char = '→'

	def main(args: Array[String]): Unit = {
		val index = VectorSpaceModelIndex(
			List(("lol1", 2, 5), ("lol2", 5, 4), ("lol4", 2, 8), ("lol4", 5, 3), ("lol5", 5, 1)).iterator,
			new File("/hdd/dev/jvm/scala/workspace/IR_data/index_file"),
			removeBlocksFiles = false)
	}

	def apply(termDocIdTfTriples: Iterator[(String, Int, Int)],
	          postingsFile: File,
	          numOfTriplesPerBlock: Int = 1000000,
	          removeBlocksFiles: Boolean = true): VectorSpaceModelIndex =
		new VectorSpaceModelIndex(termDocIdTfTriples, postingsFile, numOfTriplesPerBlock, removeBlocksFiles)
}
