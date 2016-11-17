package controller

import org.scalatra._

class MainController extends ScalatraServlet {

  get("/") {
    "Helloaaa"
  }
}