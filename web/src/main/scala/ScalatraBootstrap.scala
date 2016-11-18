import javax.servlet.ServletContext

import org.scalatra.LifeCycle
import ru.innopolis.controller.MainController

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new MainController, "/*")
  }
}
