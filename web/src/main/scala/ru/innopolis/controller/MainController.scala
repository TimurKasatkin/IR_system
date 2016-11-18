package ru.innopolis.controller

import org.scalatra._
import org.scalatra.scalate.ScalateSupport
import ru.innopolis._

class MainController extends ScalatraServlet with ScalateSupport {
  private val engine: SearchEngine = null

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
    if (query equals "") redirect("/index")
    val results = Nil //engine search query
    layoutTemplate("/WEB-INF/view/search.ssp", "query" -> query, "results" -> results)
  }
}