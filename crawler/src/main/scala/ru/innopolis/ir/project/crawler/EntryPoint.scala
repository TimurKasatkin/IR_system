package ru.innopolis.ir.project.crawler

import java.io.File

/**
  * Created by Anton on 17.11.2016.
  */
object EntryPoint {

  private case class Config(threads: Int = Runtime.getRuntime.availableProcessors(), out: String = "./",
                            interval: Int = 10, crawlCount: Int = 1250000, time: Int = 15)

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("wikicrawler") {
      opt[String]('o', "out").required().action((x, c) => c.copy(out = x)).validate(x => if (x.isEmpty)
        failure("Output folder must be specified")
      else success).text("Working directory")

      opt[Int]('t', "threads").optional().action((x, c) => c.copy(threads = x)).validate(x => if (x < 1)
        failure("Number of threads must be > 0 - default value is depends on your system")
      else success).text("Number of working threads")

      opt[Int]('i', "interval").optional().action((x, c) => c.copy(interval = x)).validate(x => if (x < 1)
        failure("Interval must be > 0")
      else success).text("Console output interval (in seconds) - default 10s")

      opt[Int]('c', "count").optional().action((x, c) => c.copy(crawlCount = x)).validate(x => if (x < 1)
        failure("Crawl count must be > 0")
      else success).text("Sets how many documents to crawl - default 1.250M")

      opt[Int]("time").optional().action((x, c) => c.copy(time = x)).validate(x => if (x < 1)
        failure("Save interval must be > 0")
      else success).text("Sets interval to save crawling state in seconds - default 15s")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        println("I will crawl " + config.crawlCount + " of documents and save them to "
          + new File(config.out).getAbsolutePath)

        Crawler.start(config.out, config.threads, config.crawlCount, config.time)
        var previousCrawled = Crawler.documentsDone
        while (true) {
          Thread.sleep(config.interval * 1000)

          val alreadyCrawled = Crawler.documentsDone

          println("Processed documents: " + alreadyCrawled + " (" + (alreadyCrawled - previousCrawled) + ")")

          previousCrawled = alreadyCrawled
        }

      case None =>
    }
  }
}
