package ru.innopolis.ir.project.core.rest

import ru.innopolis.ir.project.core.index.VectorSpaceModelIndex

/**
  * @author Timur Kasatkin 
  * @date 28.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
object SearchIndex {

	type IndexType = VectorSpaceModelIndex

	@volatile private var _currentIndex: IndexType = _

	def current: IndexType = _currentIndex

	def current_=(newIndex: IndexType): Unit = {
		_currentIndex = newIndex
	}

	def exists: Boolean = _currentIndex != null
}
