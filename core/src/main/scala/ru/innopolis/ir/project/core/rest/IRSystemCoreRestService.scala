package ru.innopolis.ir.project.core.rest

import java.io.File
import java.text.{DateFormat, SimpleDateFormat}
import java.util.Date
import java.util.concurrent.{Executors, TimeUnit}

import com.typesafe.scalalogging.Logger
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import ru.innopolis.ir.project.core.index.VectorSpaceModelIndex
import ru.innopolis.ir.project.core.preprocessing.{DocumentNormalizer, QueryNormalizer}
import ru.innopolis.ir.project.core.utils.{CollectionIterator, FileExtension}

import scala.concurrent.duration.Duration

/**
  * @author Timur Kasatkin
  * @date 27.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object IRSystemCoreRestService extends App {

	private final val DocsDirName: String = "documents"
	private final val NewNormalizedDocsDirName: String = "new_normalized_docs"
	private final val AlreadyIndexedDocsDirName: String = "indexed_docs"
	private final val IndexObjectFileName: String = "last_index.obj"
	private final val IndexPostingsFileNamePrefix: String = "index_postings_file_"
	private final val DateFormatter: DateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss")

	private final val argsParser = new scopt.OptionParser[Config]("coreRestService") {
		arg[File]("<working_dir>")
			.action((d, c) => c.copy(workingDir = d))
			.validate {
				case f if !f.exists() =>
					failure("Specified <working_dir> does not exists.")
				case f if !f.isDirectory =>
					failure("Specified <working_dir> is not a folder.")
				case _ => success
			}
			.text("Path to the working folder of Search System. ")
		opt[Duration]('n', "norm_delay")
			.optional()
			.action((n, c) => c.copy(docNormalizationDelay = n))
			.validate {
				case Duration(length, _) if length <= 0 =>
					failure("<length> can't be less or equal zero.")
				case Duration(_, u) if u.compareTo(TimeUnit.MILLISECONDS) == -1 =>
					failure("<unit> should be at least 'ms'.")
				case _ => success
			}
			.text("New documents normalization delay in format `<length><unit>` with <unit> one of: " +
				"'d' - days, 'h' - hours, 'min' - minutes, 's' - seconds, 'ms' - milliseconds. " +
				"Default - '3s'")
		opt[Duration]('i', "index_delay")
			.optional()
			.action((i, c) => c.copy(reindexingDelay = i))
			.validate {
				case Duration(length, _) if length <= 0 =>
					failure("<length> can't be less or equal zero.")
				case Duration(_, u) if u.compareTo(TimeUnit.SECONDS) == -1 =>
					failure("<unit> should be at least 's'.")
				case _ => success
			}
			.text("Delay of checking for new normalized docs to rebuild index. Format is same as for 'norm_delay', " +
				"but the minimal unit is 's' - seconds. " +
				"Default - '15s'")
		opt[Int]('m', "min_to_rebuild")
			.optional()
			.action((m, c) => c.copy(minNumberOfNewDocs = m))
			.validate {
				case m if m <= 0 =>
					failure("<min_to_rebuild> must not be less or equal to 0.")
				case _ => success
			}
			.text("Minimal number of new normalized documents for rebuilding index. Default - 1000.")
		opt[Int]('p', "port")
			.optional()
			.action((p, c) => c.copy(port = p))
			.text("Port on which start listening search requests. Default - 8081. ")
	}

	private val logger = Logger("IR System Core's REST service APP")

	private def createNewIndex(indexedDocsDir: File, workingDir: File): SearchIndex.IndexType = {
		VectorSpaceModelIndex(
			CollectionIterator(indexedDocsDir),
			generatePostingsFile(workingDir)
		)
	}

	private def generatePostingsFile(workingDir: File): File = {
		new File(workingDir, s"$IndexPostingsFileNamePrefix${DateFormatter.format(new Date)}")
	}

	argsParser.parse(args, Config()) match {
		case Some(config) =>
			// CONFIGURING REPEATED TASKS
			val scheduledExecutorService = Executors.newScheduledThreadPool(2)

			val workingDir = config.workingDir

			val docsDir = new File(workingDir, DocsDirName)
			docsDir.mkdir()
			val newNormalizedDocsDir = new File(workingDir, NewNormalizedDocsDirName)
			newNormalizedDocsDir.mkdir()
			val indexedDocsDir = new File(workingDir, AlreadyIndexedDocsDirName)
			indexedDocsDir.mkdir()

			val indexObjectFile = new File(workingDir, IndexObjectFileName)

			SearchIndex.current = if (indexObjectFile.exists()) {
				logger.info("Previous index' object file detected. Reading it...")
				VectorSpaceModelIndex.fromFile(indexObjectFile)
			} else {
				logger.info("There is no index's object file. ")
				if (indexedDocsDir.listFiles.nonEmpty) {
					logger.info("Creating from already indexed docs folder...")
					createNewIndex(indexedDocsDir, workingDir)
				} else if (newNormalizedDocsDir.listFiles.nonEmpty) {
					logger.info("There are some new normalized but not indexed docs. ")
					logger.info("Moving these docs to folder for already indexed docs...")
					newNormalizedDocsDir.moveAllFilesTo(indexedDocsDir)
					logger.info("Creating index from them...")
					createNewIndex(indexedDocsDir, workingDir)
				} else if (docsDir.listFiles.nonEmpty) {
					logger.info("There are some not yet normalized docs. " +
						"They will be normalized and index will be built from them.")
					logger.info(s"Normalizing ${docsDir.listFiles.length} docs...")
					DocumentNormalizer.normalizeAllFromAndSaveTo(docsDir, indexedDocsDir)
					logger.info("Building index ...")
					createNewIndex(indexedDocsDir, workingDir)
				} else {
					logger.info("There is no docs at all. Index will be created later by periodic tasks.")
					null
				}
			}

			if (SearchIndex.exists) SearchIndex.current.save(indexObjectFile)

			val scheduledDocsNormalizationTask = scheduledExecutorService.scheduleWithFixedDelay(
				new DocsNormalizationTask(docsDir, newNormalizedDocsDir),
				config.docNormalizationDelay._1,
				config.docNormalizationDelay._1,
				config.docNormalizationDelay._2
			)

			val scheduledReindexingTask = scheduledExecutorService.scheduleWithFixedDelay(
				new ReindexingTask(
					workingDir = workingDir,
					newNormalizedDocsDir = newNormalizedDocsDir,
					indexedDocsDir = indexedDocsDir,
					indexObjectFileName = IndexObjectFileName,
					minNumberOfNewDocs = config.minNumberOfNewDocs
				),
				config.reindexingDelay._1,
				config.reindexingDelay._1,
				config.reindexingDelay._2
			)

			//CONFIGURING REST SERVER
			val server = new Server(config.port)
			val context = new WebAppContext()

			context.setContextPath("/")
			context.setResourceBase(".")
			context.setInitParameter(ScalatraListener.LifeCycleKey, "ru.innopolis.ir.project.core.rest.ScalatraBootstrap")
			context.setEventListeners(Array(new ScalatraListener))

			server.setHandler(context)
			server.start()

			server.join()

		case None =>
	}

	private class ReindexingTask(workingDir: File,
	                             newNormalizedDocsDir: File,
	                             indexedDocsDir: File,
	                             indexObjectFileName: String,
	                             minNumberOfNewDocs: Int) extends Runnable {

		private val reindexingTaskLogger = Logger("Index Rebuilding Task")

		private val indexObjectFile = new File(workingDir, indexObjectFileName)

		override def run(): Unit = {
			reindexingTaskLogger.info("Checking for new normalized documents ...")
			val newNormedDocs = newNormalizedDocsDir.listFiles
			if (newNormedDocs.nonEmpty) {
				if (newNormedDocs.size >= minNumberOfNewDocs) {
					reindexingTaskLogger.info("There are enough new document to rebuild index. ")

					reindexingTaskLogger.info("Moving new normalized documents to index's folder...")
					newNormalizedDocsDir moveAllFilesTo indexedDocsDir

					reindexingTaskLogger.info("Rebuilding index...")
					val newIndex = createNewIndex(indexedDocsDir, workingDir)

					reindexingTaskLogger.info("Saving new index...")
					newIndex.save(indexObjectFile)
					SearchIndex.current = newIndex

					reindexingTaskLogger.info("Finished. New index applied.")
				} else
					reindexingTaskLogger.info("There are not enough new documents for rebuilding index.")
			} else
				reindexingTaskLogger.info("There are no new documents for rebuilding index.")
		}
	}

	private class DocsNormalizationTask(docsDir: File, newNormalizedDocsDir: File) extends Runnable {

		private val normalizationTaskLogger = Logger("Docs Normalization Task")

		override def run(): Unit = {
			normalizationTaskLogger.info("Checking for new documents ...")
			val docsFiles = docsDir.listFiles
			if (docsFiles.nonEmpty) {
				normalizationTaskLogger.info(s"Normalizing ${docsFiles.length} documents...")
				DocumentNormalizer.normalizeAllFromAndSaveTo(
					docsDir,
					newNormalizedDocsDir
				)
				normalizationTaskLogger.info("Finished.")
			} else
				normalizationTaskLogger.info("No new documents")
		}
	}


	private case class Config(
		                         workingDir: File = null,
		                         docNormalizationDelay: Duration = Duration(3, TimeUnit.SECONDS),
		                         reindexingDelay: Duration = Duration(15, TimeUnit.SECONDS),
		                         minNumberOfNewDocs: Int = 1000,
		                         port: Int = 8081
	                         )

}
