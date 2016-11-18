package ru.innopolis.controller

import org.scalatra._
import org.scalatra.scalate.ScalateSupport
import ru.innopolis._

class MainController extends ScalatraServlet with ScalateSupport {
  private val engine: SearchEngine = null

  get("/") {
    contentType="text/html"
    val query = params.getOrElse("query", "")
    query match {
      case "" => layoutTemplate("/WEB-INF/view/index.ssp")
      case default => {
        val results = engine search query
        layoutTemplate("/WEB-INF/view/index.ssp", "query" -> query, "results" -> results)
      }
    }
  }
}