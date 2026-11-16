package com.bignerdranch.android.var5

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

object BookRepository {
    private const val PREFS_NAME = "BookPrefs"
    private const val KEY_BOOKS = "books_json"
    private const val KEY_LAST_ISBN = "last_isbn"
    private const val KEY_SEEDED = "seeded"
    private lateinit var prefs: SharedPreferences
    private val books = mutableListOf<Book>()
    private var initialized = false
    private val trivia = listOf(
        "Самая дорогая книга в мире — «Кодекс Лестера», проданная за 30,8 млн долларов.",
        "Первая печатная книга — Библия Гутенберга, созданная около 1455 года.",
        "Люди, которые регулярно читают, в среднем живут на два года дольше.",
        "Чтение снижает уровень стресса почти на 68 % — лучше чем прогулка.",
        "В среднем человек читает около 12 книг в год, а 27 % людей не читают вовсе.",
        "Самая большая библиотека мира — Библиотека Конгресса США: более 170 млн единиц хранения.",
        "Книга «1984» Джорджа Оруэлла переведена более чем на 65 языков.",
        "Самый длинный роман — «В поисках утраченного времени» Марселя Пруста: около 1,2 млн слов."
    )
    fun init(context: Context) {
        if (initialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        load()
        seedDemoBooks()
        initialized = true
    }
    fun all(): List<Book> = books.toList()
    fun findByIsbn(isbn: String): Book? = books.firstOrNull { it.isbn == isbn }
    fun lastBook(): Book? {
        val isbn = prefs.getString(KEY_LAST_ISBN, null) ?: return null
        return findByIsbn(isbn)
    }
    fun save(book: Book): Book {
        val index = books.indexOfFirst { it.isbn == book.isbn }
        if (index >= 0) {
            books[index] = book
        } else {
            books.add(0, book)
        }
        prefs.edit().putString(KEY_LAST_ISBN, book.isbn).apply()
        persist()
        return book
    }

    fun update(originalIsbn: String, book: Book) {
        val index = books.indexOfFirst { it.isbn == originalIsbn }
        if (index >= 0) {
            books[index] = book
            if (prefs.getString(KEY_LAST_ISBN, null) == originalIsbn) {
                prefs.edit().putString(KEY_LAST_ISBN, book.isbn).apply()
            }
            persist()
        }
    }
    fun makeFact(book: Book): String {
        val dataFacts = mutableListOf<String>()
        if (book.publishDate.isNotBlank()) {
            dataFacts += "Книга была опубликована: ${book.publishDate}."
        }
        if (book.pages > 0) {
            dataFacts += "В этом издании ${book.pages} страниц."
        }
        if (book.authors.isNotBlank()) {
            dataFacts += "Автор книги — ${book.authors}."
        }
        dataFacts += trivia
        return dataFacts.random()
    }
    private fun load() {
        books.clear()
        val raw = prefs.getString(KEY_BOOKS, null) ?: return
        runCatching {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                books += Book.fromJson(item)
            }
        }
    }
    private fun persist() {
        val array = JSONArray()
        books.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_BOOKS, array.toString()).apply()
    }
    private fun seedDemoBooks() {
        if (prefs.getBoolean(KEY_SEEDED, false) || books.isNotEmpty()) return
        val demo = listOf(
            Book(
                isbn = "9780451524935",
                title = "1984",
                authors = "George Orwell",
                publishDate = "1949",
                pages = 328,
                publishers = "Signet Classic",
                fact = "Книга «1984» переведена более чем на 65 языков."
            ),
            Book(
                isbn = "9780140449136",
                title = "Преступление и наказание",
                authors = "Фёдор Достоевский",
                publishDate = "1866",
                pages = 671,
                publishers = "Penguin Classics",
                fact = "Роман впервые публиковался частями в журнале «Русский вестник»."
            )
        )
        books.addAll(demo)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        persist()
    }
}
