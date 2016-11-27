package ru.innopolis.ir.project.core.cli

import java.io.File

import ru.innopolis.ir.project.core.preprocessing.DocumentNormalizer
import ru.innopolis.ir.project.core.utils.time

/**
  * @author Timur Kasatkin 
  * @date 24.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object DocumentsNormalization {

	def main(args: Array[String]): Unit = {
		val parser = new scopt.OptionParser[Config]("docsNormalizer") {
			arg[File]("<source_docs_folder>")
				.action((f, c) => c.copy(sourceDocsFolder = f))
				.text("Path to folder with source documents")
			arg[File]("<save_folder>")
				.action((f, c) => c.copy(processedSaveFolder = f))
				.text("Path to folder where normalized documents should be saved")
		}
		parser.parse(args, Config()) match {
			case Some(config) =>
				time(DocumentNormalizer.normalizeAllFromAndSaveTo(
					config.sourceDocsFolder,
					config.processedSaveFolder
				))
			case None =>
		}
	}

	private case class Config(sourceDocsFolder: File = null, processedSaveFolder: File = null)

}
