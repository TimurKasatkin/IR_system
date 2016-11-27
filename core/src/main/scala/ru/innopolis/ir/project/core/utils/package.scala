package ru.innopolis.ir.project.core

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

/**
  * @author Timur Kasatkin 
  * @date 25.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
package object utils {

	implicit class RichRegex(underlying: Regex) {
		def matches(s: String): Boolean = underlying.pattern.matcher(s).matches
	}

	implicit class StringIterableExtension(strIterable: Iterable[String]) {
		def wordCounts: Map[String, Int] = strIterable.foldLeft(Map.empty[String, Int].withDefaultValue(0)) {
			(count, term) => count + (term -> (count(term) + 1))
		}
	}

	implicit class IteratorExtension[A](iter: Iterator[A]) {
		def doIfHasNext(f: A => Unit): Unit = if (iter.hasNext) f(iter.next())
	}

	def time[R](block: => R): R = {
		val t0 = System.nanoTime()
		val result = block
		// call-by-name
		val t1 = System.nanoTime()
		println("Elapsed time: " + (t1 - t0) + "ns")
		result
	}

	def using[A, B](resource: A)(cleanup: A => Unit)(doWork: A => B): Try[B] = {
		try {
			Success(doWork(resource))
		} catch {
			case e: Exception =>
				e.printStackTrace()
				Failure(e)
		} finally {
			try {
				if (resource != null) {
					cleanup(resource)
				}
			} catch {
				case e: Exception => e.printStackTrace() // should be logged
			}
		}
	}

}
