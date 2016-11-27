package ru.innopolis.controller

import org.scalatra._
import org.scalatra.scalate.ScalateSupport
import ru.innopolis.service.SearchEngine

class MainController extends ScalatraServlet with ScalateSupport {
  private val engine: SearchEngine = new SearchEngine

  get("/") {
    redirect("/index")
  }

  get("/index") {
    contentType = "text/html"
    ssp("/WEB-INF/view/index.ssp")
  }

  get("/search") {
    contentType = "text/html"
    val query = params.getOrElse("query", "")
    val currentPage = Integer.parseInt(params.getOrElse("page", "1"))
    if (query equals "") redirect("/index")
    val results = engine.search(query, currentPage)
    layoutTemplate("/WEB-INF/view/search.ssp", "query" -> query, "results" -> results._1,
      "page" -> math.min(currentPage, results._3), "pages" -> results._3, "totalResults" -> results._2)
  }
}