package ru.innopolis.ir.project.core

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

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

	def time[R](block: => R): R = {
		val t0 = System.nanoTime()
		val result = block    // call-by-name
		val t1 = System.nanoTime()
		println("Elapsed time: " + (t1 - t0) + "ns")
		result
	}

	def using[A, B](resource: A)(cleanup: A => Unit)(doWork: A => B): Try[B] = {
		try {
			Success(doWork(resource))
		} catch {
			case e: Exception => Failure(e)
		} finally {
			try {
				if (resource != null) {
					cleanup(resource)
				}
			} catch {
				case e: Exception => println(e) // should be logged
			}
		}
	}

}
