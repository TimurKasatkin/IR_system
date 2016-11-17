package gui

import org.scalatra._

class MainServlet extends ScalatraServlet {

  get("/") {
    """
       <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
    """
  }

}