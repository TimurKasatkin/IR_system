package com.antonzhdanov.ir

import java.io.File
import java.util
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

/**
  * Created by Anton on 17.11.2016.
  */
object Crawler {
  private val BASE_URL = "https://en.wikipedia.org/wiki/"
  private val START_PAGE = "Information_retrieval"
  private var OUTPUT_DIR = ""
  private var SYSTEM_DIR = ""
  private val alreadyCrawledCount: AtomicInteger = new AtomicInteger(0)
  private val alreadyInExecutor: util.Set[String] = new util.TreeSet[String]()

  private var CRAWL_COUNT: Int = _

  private val TAGS_TO_SEARCH = List("p", "ul", "h3 mw-headline", "h2 mw-headline", "table")
  private val SPECIAL_PREFIXES = List("File:", "User:", "Wikipedia:", "MediaWiki:", "Template:", "Help:", "Book:",
    "Draft:", "Education Program:", "TimedText:", "Module:", "Gadget:", "Topic:", "Image:")

  private var NUMBER_OF_THREADS: Int = _

  private var completionService: CompletionService[Document] = _

  def submit(page: String): Unit = {
    completionService.submit(new CrawlTask(BASE_URL + page, TAGS_TO_SEARCH))
    alreadyInExecutor.add(page)
  }

  def start(workingDir: String, numberOfThreads: Int, crawlCount: Int): Unit = {
    SYSTEM_DIR = workingDir
    OUTPUT_DIR = SYSTEM_DIR + File.separator + "documents" + File.separator
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
    submit(START_PAGE)

    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          val result: Document = completionService.take().get()
          if (result != null) {
            result.save(OUTPUT_DIR + alreadyCrawledCount.getAndIncrement())

            if (alreadyInExecutor.size() + alreadyCrawledCount.intValue() < CRAWL_COUNT)
              result.getLinks.foreach((relUrl: String) => {
                var page: String = relUrl.substring(6)
                val sharpIndex = page.indexOf("#")
                if (sharpIndex != -1)
                  page = page.substring(0, sharpIndex)

                val split: Array[String] = page.split(":")
                var nameSpace: String = null
                if (split.length > 0)
                  nameSpace = split.apply(0)

                if (!SPECIAL_PREFIXES.contains(nameSpace) && !page.contains("#") && !alreadyInExecutor.contains(page)) {
                  submit(page)
                }
              })
          }
        }
      }
    }).start()
  }

  def documentsDone: Int = alreadyCrawledCount.get()

  def documentsInQueue: Int = alreadyInExecutor.size()
}
