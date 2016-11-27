package ru.innopolis.ir.project.core.rest

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport

/**
  * @author Timur Kasatkin 
  * @date 27.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
case class SearchResult(docs: List[Document], totalNumberOfResults: Int)

case class Document(title: String, `abstract`: String, url: String)

class DocumentsController extends ScalatraServlet with JacksonJsonSupport {

	protected implicit val jsonFormats: Formats = DefaultFormats

	private val testDocs = (1 to 300).map(i => Document(
		title = s"title #$i",
		`abstract` = s"I am the abstract number $i.",
		url = s"http://lol.com/page$i"
	))


	before() {
		contentType = formats("json")
	}

	get("/documents") {
		val query = params("query")
		val (pageNumber, pageLimit) = (params("pageNumber").toInt, params("pageLimit").toInt)
		println(List(query, pageNumber, pageLimit))

		SearchResult(
			testDocs.slice((pageNumber - 1) * pageLimit,(pageNumber - 1) * pageLimit + pageLimit).toList,
			testDocs.size
		)
	}

}
