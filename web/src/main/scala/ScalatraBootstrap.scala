import javax.servlet.ServletContext

import controller.MainController
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new MainController, "/*")
  }
}
