package ru.innopolis.ir.project.core.rest

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

/**
  * @author Timur Kasatkin 
  * @date 27.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object SimpleScalatraRestService extends App {
	val port = 8081
	val server = new Server(port)

	val context = new WebAppContext()
	context.setContextPath("/")
	context.setResourceBase(".")
	context.setInitParameter(ScalatraListener.LifeCycleKey, "ru.innopolis.ir.project.core.rest.ScalatraBootstrap")
	context.setEventListeners(Array(new ScalatraListener))

	server.setHandler(context)
	server.start()

	server.join()
}
