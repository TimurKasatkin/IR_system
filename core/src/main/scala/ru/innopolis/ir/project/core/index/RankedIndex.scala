package ru.innopolis.ir.project.core.index

/**
  * @author Timur Kasatkin 
  * @date 28.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
trait RankedIndex {

	def search(queryTokens: Iterable[String], pageNumber: Int, pageLimit: Int = 100): (List[SearchResult], Int)

}
