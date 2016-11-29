package ru.innopolis.ir.project.core.index

/**
  * @author Timur Kasatkin 
  * @date 28.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
trait RankedIndex {

	protected def searchRequirements(queryTokens: Iterable[String], pageNumber: Int, pageLimit: Int ): Unit = {
		require(pageNumber >= 1, "Page number should be at least 1")
		require(pageLimit >= 1, "Page limit can't be zero or negative")
	}

	def search(queryTokens: Iterable[String], pageNumber: Int, pageLimit: Int = 100): (List[SearchResult], Int)

}
