package com.bignerdranch.android.var5

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class BookNotFoundException(val isbn: String) :
    Exception("Книга с ISBN $isbn не найдена в OpenLibrary")


object OpenLibraryApi {

    private const val BASE_URL = "https://openlibrary.org/api/books"

    suspend fun fetchByIsbn(isbn: String): Book = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL?bibkeys=ISBN:$isbn&format=json&jscmd=data")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "BookFinder/1.0 (Android educational project)")
        }

        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("OpenLibrary ответил кодом $code")
            }
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(body)
            val bookJson = root.optJSONObject("ISBN:$isbn")
                ?: throw BookNotFoundException(isbn)
            parseBook(isbn, bookJson)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseBook(isbn: String, json: JSONObject): Book {
        val title = json.optString("title").ifBlank { "Без названия" }
        val authors = joinNames(json.optJSONArray("authors"))
        val publishers = joinNames(json.optJSONArray("publishers"))
        val publishDate = json.optString("publish_date")
        val pages = json.optInt("number_of_pages", 0)
        val cover = json.optJSONObject("cover")
        val coverUrl = cover?.optString("medium")
            ?.ifBlank { cover.optString("small") }
            ?.ifBlank { cover.optString("large") }
            .orEmpty()

        val book = Book(
            isbn = isbn,
            title = title,
            authors = authors,
            publishDate = publishDate,
            pages = pages,
            publishers = publishers,
            coverUrl = coverUrl
        )
        return book.copy(fact = BookRepository.makeFact(book))
    }
    private fun joinNames(array: JSONArray?): String {
        if (array == null || array.length() == 0) return ""
        val names = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val name = array.optJSONObject(i)?.optString("name").orEmpty()
            if (name.isNotBlank()) names += name
        }
        return names.joinToString(", ")
    }
}
