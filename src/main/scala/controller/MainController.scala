package controller

import org.scalatra._
import org.scalatra.scalate.ScalateSupport

class MainController extends ScalatraServlet with ScalateSupport {

  get("/") {
    contentType="text/html"
    layoutTemplate("/WEB-INF/view/index.ssp")
  }
}