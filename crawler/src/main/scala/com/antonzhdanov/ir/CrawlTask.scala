package com.antonzhdanov.ir

import java.util.concurrent.Callable

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element

/**
  * Created by Anton on 17.11.2016.
  */
class CrawlTask(url: String, tagsToSearch: List[String])
  extends Callable[Document] {

  override def call(): Document = {
    val browser = JsoupBrowser()
    try {
      val doc = browser.get(url)
      val content: Element = doc >> element("#mw-content-text")
      val wikiLinks: Iterable[String] = (content >> attrs("href")("a")).filter(_.startsWith("/wiki/"))

      val allContentElements: Iterable[Element] = content.children.filter((element: Element) =>
        tagsToSearch.contains(element.tagName))

      val stringBuilder: StringBuilder = new StringBuilder()

      stringBuilder.append(url + "\n")
      stringBuilder.append((doc >> text("#firstHeading")) + "\n")

      allContentElements.foreach((el: Element) => stringBuilder.append((el >> text(el.tagName)) + "\n"))

      new Document(url, stringBuilder.toString(), wikiLinks)
    } catch {
      case e: Exception =>
        System.err.println(url + ": " + e.getMessage)
        null
    }
  }
}
