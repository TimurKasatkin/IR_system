import javax.servlet.ServletContext

import com.typesafe.scalalogging.Logger
import org.scalatra.LifeCycle
import ru.innopolis.controller.MainController

class ScalatraBootstrap extends LifeCycle {
  val logger = Logger(this.getClass)
  override def init(context: ServletContext) {
    context.mount(new MainController, "/*")
  }
}
