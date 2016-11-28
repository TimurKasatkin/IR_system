package ru.innopolis.ir.project.core.rest

import javax.servlet.ServletContext

import com.typesafe.scalalogging.Logger
import org.scalatra.LifeCycle

/**
  * @author Timur Kasatkin 
  * @date 27.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
class ScalatraBootstrap extends LifeCycle {

	val logger = Logger(this.getClass)

	override def init(context: ServletContext): Unit = {
		//Load first index
		//Configure repeated task
		context mount (new DocumentsController, "/*")
	}

}
