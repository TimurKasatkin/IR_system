package ru.innopolis.ir.project.core.rest

import java.io.File

import ru.innopolis.ir.project.core.index.VectorSpaceModelIndex
import ru.innopolis.ir.project.core.preprocessing.NormalizedDocument

/**
  * @author Timur Kasatkin 
  * @date 28.11.16.
  * @email aronwest001@gmail.com
  * @email t.kasatkin@innopolis.ru
  */
private[rest] object SearchIndex {

	type IndexType = VectorSpaceModelIndex

	@volatile private var _currentIndex: IndexType = _

	private var _indexedDocsDir: File = _

	def current: IndexType = _currentIndex

	def current_=(newIndex: IndexType): Unit = {
		_currentIndex = newIndex
	}

	def exists: Boolean = _currentIndex != null

	def indexedDocsDir: File = _indexedDocsDir

	def indexedDocsDir_=(indexedDocsDir: File): Unit = {
		_indexedDocsDir = indexedDocsDir
	}

	def documentsByIds(ids: Iterable[Int]): Iterable[NormalizedDocument] =
		ids.map(id => NormalizedDocument.fromFile(new File(_indexedDocsDir, id.toString)))
}
