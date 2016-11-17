import javax.servlet.ServletContext

import controller.MainController
import org.scalatra.LifeCycle

/**
  * Created by marat on 17.11.2016.
  */
class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    // Mount servlets.
    context.mount(new MainController, "/*")
  }
}
