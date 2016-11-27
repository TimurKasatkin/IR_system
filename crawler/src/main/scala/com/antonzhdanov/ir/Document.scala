package com.antonzhdanov.ir

import java.io.{File, FileWriter, IOException}

/**
  * Created by Anton on 17.11.2016.
  */

class Document(pageUrl: String, data: String, links: Iterable[String]) {
  def getUrl: String = pageUrl

  def getData: String = data

  def getLinks: Iterable[String] = links

  def save(path: String): Unit = {
    val file: File = new File(path)
    var fileWriter: FileWriter = null

    try {
      fileWriter = new FileWriter(file)
      fileWriter.write(data)
    } catch {
      case e: IOException =>
    } finally {
      fileWriter.close()
    }
  }
}
