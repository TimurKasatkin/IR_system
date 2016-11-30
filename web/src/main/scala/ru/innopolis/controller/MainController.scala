package ru.innopolis.controller

import org.scalatra._
import org.scalatra.scalate.ScalateSupport
import ru.innopolis.service.SearchEngine

class MainController extends ScalatraServlet with ScalateSupport {

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
    val results = SearchEngine.search(query, currentPage)
    val pagination = SearchEngine.getPagination(currentPage, results._3)
    layoutTemplate("/WEB-INF/view/search.ssp", "query" -> query, "results" -> results._1,
      "currentPage" -> math.min(currentPage, results._3), "pages" -> results._3, "totalResults" -> results._2,
      "firstPage" -> pagination._1, "lastPage" -> pagination._2)
  }
}