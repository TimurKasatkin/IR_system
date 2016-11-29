package ru.innopolis.ir.project.core.preprocessing

import java.io.File
import java.util.concurrent.Executors

import ru.innopolis.ir.project.core.Document
import ru.innopolis.ir.project.core.utils.StringIterableExtension

import scala.collection.parallel.ExecutionContextTaskSupport
import scala.concurrent.ExecutionContext

/**
  * @author Timur Kasatkin
  * @date 18.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object DocumentNormalizer {

	def apply(doc: Document): NormalizedDocument = {
		val termToFrequency: Map[String, Int] = StringNormalizer(doc.text).wordCounts
		NormalizedDocument(
			id = doc.id,
			title = doc.title,
			url = doc.url,
			`abstract` = doc.`abstract`,
			termToFrequencyMap = termToFrequency
		)
	}

	def normalizeRemovingInParallelAllFromAndSaveTo(fromDir: File,
	                                                saveDir: File,
	                                                levelOfParallelism: Int = Runtime.getRuntime.availableProcessors()): Unit = {
		normalizeInParallelAllFromAndSaveTo(fromDir, saveDir, levelOfParallelism, removeSourceDocs = true): @inline
	}

	def normalizeInParallelAllFromAndSaveTo(fromDir: File,
	                                        saveDir: File,
	                                        levelOfParallelism: Int = Runtime.getRuntime.availableProcessors(),
	                                        removeSourceDocs: Boolean = false): Unit = {
		require(fromDir.exists, "Source folder does not exists.")
		require(fromDir.isDirectory, "Source path is not a folder.")

		if (!saveDir.exists()) saveDir.mkdir()

		val dirs = fromDir.listFiles.par
		//		dirs.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(levelOfParallelism))
		val executorService = Executors.newFixedThreadPool(levelOfParallelism)
		dirs.tasksupport = new ExecutionContextTaskSupport(ExecutionContext.fromExecutorService(executorService))

		dirs foreach { f =>
			val normedDoc = this (Document.fromFile(f))
			normedDoc.saveTo(saveDir)
		}

		if (removeSourceDocs)
			dirs foreach {
				_.delete()
			}

		executorService.shutdown()
	}

}
