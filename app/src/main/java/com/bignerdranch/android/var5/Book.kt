package com.bignerdranch.android.var5

import org.json.JSONObject

data class Book(
    val isbn: String,
    val title: String,
    val authors: String = "",
    val publishDate: String = "",
    val pages: Int = 0,
    val publishers: String = "",
    val fact: String = "",
    val coverUrl: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("isbn", isbn)
        put("title", title)
        put("authors", authors)
        put("publish_date", publishDate)
        put("pages", pages)
        put("publishers", publishers)
        put("fact", fact)
        put("cover_url", coverUrl)
    }
    companion object {
        fun fromJson(json: JSONObject): Book = Book(
            isbn = json.optString("isbn"),
            title = json.optString("title"),
            authors = json.optString("authors"),
            publishDate = json.optString("publish_date"),
            pages = json.optInt("pages", 0),
            publishers = json.optString("publishers"),
            fact = json.optString("fact"),
            coverUrl = json.optString("cover_url")
        )
    }
}
