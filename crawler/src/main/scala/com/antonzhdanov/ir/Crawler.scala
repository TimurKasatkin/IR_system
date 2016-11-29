package com.antonzhdanov.ir

import java.io.{File, FileWriter, IOException}
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable
import scala.io.Source

/**
  * Created by Anton on 17.11.2016.
  */
object Crawler {
  private val BASE_URL = "https://en.wikipedia.org/wiki/"
  private val START_PAGE = "Information_retrieval"
  private var OUTPUT_DIR = ""
  private var SYSTEM_DIR = ""
  private val QUEUE_NAME = "queue"
  private val COMPLETED_NAME = "completed"

  private val completedCount: AtomicInteger = new AtomicInteger(0)
  private val completedDocuments: mutable.TreeSet[String] = new mutable.TreeSet[String]()
  private val queuedDocuments: mutable.TreeSet[String] = new mutable.TreeSet[String]()

  private var CRAWL_COUNT: Int = _

  private val TAGS_TO_SEARCH = List("p", "ul", "h3 mw-headline", "h2 mw-headline", "table")
  private val SPECIAL_PREFIXES = List("File:", "User:", "Wikipedia:", "MediaWiki:", "Template:", "Help:", "Book:",
    "Draft:", "Education Program:", "TimedText:", "Module:", "Gadget:", "Topic:", "Image:", "Special:",
    "Template_talk:", "Category:")

  private var NUMBER_OF_THREADS: Int = _

  private var completionService: CompletionService[Document] = _

  def submit(page: String): Unit = {
    queuedDocuments.add(page)
    completionService.submit(new CrawlTask(BASE_URL + page, TAGS_TO_SEARCH))
  }

  def start(workingDir: String, numberOfThreads: Int, crawlCount: Int): Unit = {
    SYSTEM_DIR = workingDir + File.separator
    OUTPUT_DIR = SYSTEM_DIR + "documents" + File.separator
    NUMBER_OF_THREADS = numberOfThreads
    CRAWL_COUNT = crawlCount

    val sysDir: File = new File(OUTPUT_DIR)
    if (!sysDir.exists())
      sysDir.mkdirs()


    if (NUMBER_OF_THREADS == 1)
      completionService = new ExecutorCompletionService(Executors.newSingleThreadExecutor())
    else
      completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(NUMBER_OF_THREADS))

    start()
  }

  private def start(): Unit = {
    loadState()

    if (completedCount.get() == 0)
      submit(START_PAGE)

    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          val result: Document = completionService.take().get()
          if (result != null) {
            result.save(OUTPUT_DIR + completedCount.getAndIncrement())
            queuedDocuments.remove(result.getUrl.substring(BASE_URL.length))
            completedDocuments.add(result.getUrl.substring(BASE_URL.length))

            if (completedDocuments.size + queuedDocuments.size + completedCount.intValue() < CRAWL_COUNT)
              result.getLinks.foreach((relUrl: String) => {
                var page: String = relUrl.substring(6)
                val sharpIndex = page.indexOf("#")
                if (sharpIndex != -1)
                  page = page.substring(0, sharpIndex)

                var specialPage: Boolean = false
                SPECIAL_PREFIXES.foreach((prefix: String) => if (page.startsWith(prefix)) specialPage = true)

                if (!specialPage && !completedDocuments.contains(page) && !queuedDocuments.contains(page)) {
                  submit(page)
                }
              })

            if (completedCount.get() % 5000 == 0)
              saveState()
          }
        }
      }
    }).start()
  }

  def documentsDone: Int = completedCount.get()

  def documentsInQueue: Int = completedDocuments.size

  private def saveState(): Unit = {
    var file: File = null
    var fileWriter: FileWriter = null
    val stringBuilder: StringBuilder = new mutable.StringBuilder()
    try {
      file = new File(SYSTEM_DIR + QUEUE_NAME)
      if (file.exists())
        file.delete()

      fileWriter = new FileWriter(file)
      queuedDocuments.foreach((page: String) => stringBuilder.append(page + "\n"))
      fileWriter.write(stringBuilder.toString())

      fileWriter.close()
      stringBuilder.clear()

      file = new File(SYSTEM_DIR + COMPLETED_NAME)
      if (file.exists())
        file.delete()

      fileWriter = new FileWriter(file)
      completedDocuments.foreach((page: String) => stringBuilder.append(page + "\n"))
      fileWriter.write(stringBuilder.toString())
    } catch {
      case e: IOException =>
    } finally {
      fileWriter.close()
    }
  }

  private def loadState(): Unit = {
    var file: File = new File(SYSTEM_DIR + QUEUE_NAME)
    if (file.exists())
      Source.fromFile(file).getLines().foreach((page: String) => submit(page))

    file = new File(SYSTEM_DIR + COMPLETED_NAME)
    if (file.exists())
      Source.fromFile(file).getLines().foreach((page: String) => completedDocuments.add(page))

    completedCount.set(completedDocuments.size)
  }
}
