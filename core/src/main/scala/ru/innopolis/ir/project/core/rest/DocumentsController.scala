package ru.innopolis.ir.project.core.rest

import com.typesafe.scalalogging.Logger
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{NotFound, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport
import ru.innopolis.ir.project.core.preprocessing.QueryNormalizer

/**
  * @author Timur Kasatkin 
  * @date 27.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
case class SearchResult(docs: List[Document] = List(), totalNumberOfResults: Int = 0)

case class Document(title: String, `abstract`: String, url: String)

class DocumentsController extends ScalatraServlet with JacksonJsonSupport {

	protected implicit val jsonFormats: Formats = DefaultFormats

	private val logger = Logger("Documents search queries handler")

	private val testDocs = (1 to 300).map(i => Document(
		title = s"title #$i",
		`abstract` = s"I am the abstract number $i.",
		url = s"http://lol.com/page$i"
	))

	get("/*") {
		NotFound("Resource not found")
	}


	before() {
		contentType = formats("json")
	}

	get("/documents") {
		val query = params("query")
		val (pageNumber, pageLimit) = (params("pageNumber").toInt, params("pageLimit").toInt)
		logger.info(s"query '$query' for page $pageNumber with limit $pageLimit")

		if (SearchIndex.exists) {
			val (result, totalCount) = SearchIndex.current.search(QueryNormalizer(query), pageNumber, pageLimit)
//			SearchResult(testDocs.toList, testDocs.size)
			SearchResult(
				SearchIndex.documentsByIds(result.view.map(_.docId))
					.map(doc => Document(doc.title, doc.`abstract`, doc.url.toString)).toList,
				totalCount
			)
		} else {
			logger.warn("No index exists at the moment. Returning empty result...")
			SearchResult()
		}
	}

}
