package ru.innopolis.ir.project.crawler

import java.io._
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable

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
  private var completedDocuments: mutable.TreeSet[String] = new mutable.TreeSet[String]()
  private var queuedDocuments: mutable.TreeSet[String] = new mutable.TreeSet[String]()

  private var CRAWL_COUNT: Int = _
  private var TIME_INTERVAL: Int = _

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

  def start(workingDir: String, numberOfThreads: Int, crawlCount: Int, timeInterval: Int): Unit = {
    SYSTEM_DIR = workingDir + File.separator
    OUTPUT_DIR = SYSTEM_DIR + "documents" + File.separator
    NUMBER_OF_THREADS = numberOfThreads
    CRAWL_COUNT = crawlCount
    TIME_INTERVAL = timeInterval

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

            if ((System.currentTimeMillis() / 1000l) % TIME_INTERVAL == 0)
              saveState()
          }
        }
      }
    }).start()
  }

  def documentsDone: Int = completedCount.get()

  def documentsInQueue: Int = completedDocuments.size

  private def saveState(): Unit = {
    var fileOutputStream: FileOutputStream = null
    var objectOutputStream: ObjectOutputStream = null
    var file: File = null

    try {
      file = new File(SYSTEM_DIR + QUEUE_NAME)
      if (file.exists())
        file.delete()

      fileOutputStream = new FileOutputStream(file)
      objectOutputStream = new ObjectOutputStream(fileOutputStream)

      objectOutputStream.writeObject(queuedDocuments)
      objectOutputStream.close()
      fileOutputStream.close()

      file = new File(SYSTEM_DIR + COMPLETED_NAME)
      if (file.exists())
        file.delete()

      fileOutputStream = new FileOutputStream(file)
      objectOutputStream = new ObjectOutputStream(fileOutputStream)
      objectOutputStream.writeObject(completedDocuments)

      println("State has been saved")
      Thread.sleep(1000)
    } catch {
      case e: IOException =>
    } finally {
      fileOutputStream.close()
      objectOutputStream.close()
    }
  }

  private def loadState(): Unit = {
    var file: File = null
    var fileInputStream: FileInputStream = null
    var objectInputStream: ObjectInputStream = null

    file = new File(SYSTEM_DIR + COMPLETED_NAME)
    if (file.exists()) {
      try {
        fileInputStream = new FileInputStream(file)
        objectInputStream = new ObjectInputStream(fileInputStream)
        completedDocuments = objectInputStream.readObject().asInstanceOf[mutable.TreeSet[String]]
        completedCount.set(completedDocuments.size)
      } catch {
        case e: IOException =>
      } finally {
        objectInputStream.close()
      }
    }

    file = new File(SYSTEM_DIR + QUEUE_NAME)
    if (file.exists()) {
      try {
        fileInputStream = new FileInputStream(file)
        objectInputStream = new ObjectInputStream(fileInputStream)
        queuedDocuments = objectInputStream.readObject().asInstanceOf[mutable.TreeSet[String]]
        queuedDocuments.foreach(submit)
      } catch {
        case e: IOException =>
      } finally {
        objectInputStream.close()
      }
    }
  }
}
