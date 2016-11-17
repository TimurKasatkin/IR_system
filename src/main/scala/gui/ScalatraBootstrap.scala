package gui

import org.scalatra.LifeCycle
import javax.servlet.ServletContext

/**
  * Created by marat on 17.11.2016.
  */
class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    // Mount servlets.
    context.mount(new MainServlet, "/*")
  }
}
