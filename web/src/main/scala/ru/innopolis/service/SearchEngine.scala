package ru.innopolis.service

class SearchEngine {
  /**
    * Принимает поисковый запрос, номер страницы и кол-во записей на страницу,
    * возвращает список результатов и сколько всего страниц.
    * @param searchQuery
    * @param page
    * @param limit
    * @return
    */
  def search(searchQuery: String, page: Int = 0, limit: Int = 10): (Iterable[HumanSearchResult], Int) = {
    return (
      List.fill(limit)(new HumanSearchResult("#", "Hello world. How are you? I'm fine, thank you.", "Hello world. How are you? I'm fine, thank you. Hello world. How are you? I'm fine, thank you.\n    Hello world. How are you? I'm fine, thank you. Hello world. How are you? I'm fine, thank you.\n    Hello world. How are you? I'm fine, thank you. Hello world. How are you? I'm fine, thank you.\n    Hello world. How are you? I'm fine, thank you. Hello world. How are you? I'm fine, thank you.")),
      8) //кол-во страниц
  }
}
