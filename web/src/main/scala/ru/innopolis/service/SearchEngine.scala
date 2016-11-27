package ru.innopolis.service

class SearchEngine {
  def search(searchQuery: String, page: Int = 0, limit: Int = 10): Iterable[HumanSearchResult] = {
    return List.fill(limit)(new HumanSearchResult("#", "Hello world. How are you? I'm fine, thank you.", "Hello world. How are you? I'm fine, thank you. Hello world. How are you? I'm fine, thank you.\n    Hello world. How are you? I'm fine, thank you. Hello world. How are you? I'm fine, thank you.\n    Hello world. How are you? I'm fine, thank you. Hello world. How are you? I'm fine, thank you.\n    Hello world. How are you? I'm fine, thank you. Hello world. How are you? I'm fine, thank you."))
  }
}
