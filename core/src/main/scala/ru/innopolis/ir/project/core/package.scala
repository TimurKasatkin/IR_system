package ru.innopolis.ir.project

import java.io.File

import scala.util.{Failure, Success, Try}

/**
  * @author Timur Kasatkin
  * @date 19.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
package object core {

	def readDocumentsFrom(dir: String, maxCount: Int = 0): Array[Document] = {
		var docFiles: Array[File] = new File(dir).listFiles.sorted
		docFiles = if (maxCount > 0) docFiles take maxCount else docFiles
		docFiles map Document.fromFile
	}

	def time[R](block: => R): R = {
		val t0 = System.nanoTime()
		val result = block    // call-by-name
		val t1 = System.nanoTime()
		println("Elapsed time: " + (t1 - t0) + "ns")
		result
	}

	def cleanly[A, B](resource: A)(cleanup: A => Unit)(doWork: A => B): Try[B] = {
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
