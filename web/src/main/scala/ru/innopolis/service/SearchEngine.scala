package ru.innopolis.service
import java.net.URLEncoder

import com.typesafe.scalalogging.Logger
import org.json4s.jackson.JsonMethods.parse
import org.json4s.DefaultFormats

object SearchEngine {

  private implicit val formats = DefaultFormats
  private val logger = Logger("Search Engine")

  def search(searchQuery: String, page: Int = 0, limit: Int = 10): (Iterable[HumanSearchResult], Int, Int) = {
    val request = "http://localhost:8081/documents?query=" + URLEncoder.encode(searchQuery, "UTF-8") + s"&pageNumber=$page&pageLimit=$limit"
    val response = parseJSON(scala.io.Source.fromURL(request).mkString)
    val resultsNumber = response._2
    val pages = resultsNumber / limit + (if (resultsNumber % limit > 0) 1 else 0)
    (response._1, resultsNumber, pages)
  }

  private def parseJSON(json: String) : (List[HumanSearchResult], Int) = {
    logger.info(json)
    val _json = parse(json)
    val results = (_json \ "docs" ).extract[List[HumanSearchResult]]
    val resNum = (_json \ "totalNumberOfResults").extract[Integer]
    return (results, resNum)
  }
}
