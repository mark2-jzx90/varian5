package com.bignerdranch.android.var5

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class BookAdapter(
    private val context: Context,
    private val books: MutableList<Book>
) : BaseAdapter() {
    fun submit(newBooks: List<Book>) {
        books.clear()
        books.addAll(newBooks)
        notifyDataSetChanged()
    }
    override fun getCount(): Int = books.size
    override fun getItem(position: Int): Book = books[position]
    override fun getItemId(position: Int): Long = books[position].isbn.hashCode().toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_book, parent, false)
        val holder = view.tag as? ViewHolder ?: ViewHolder(view).also { view.tag = it }
        val book = getItem(position)
        holder.title.text = book.title.ifBlank { context.getString(R.string.unknown_title) }
        holder.authors.text = book.authors.ifBlank { context.getString(R.string.unknown_author) }
        holder.details.text = context.getString(
            R.string.book_details_short,
            book.isbn,
            book.publishDate.ifBlank { context.getString(R.string.unknown_year) },
            if (book.pages > 0) book.pages.toString() else context.getString(R.string.unknown_pages)
        )
        holder.fact.text = context.getString(
            R.string.fact_format,
            book.fact.ifBlank { BookRepository.makeFact(book) }
        )
        return view
    }
    private class ViewHolder(view: View) {
        val title: TextView = view.findViewById(R.id.tvItemTitle)
        val authors: TextView = view.findViewById(R.id.tvItemAuthors)
        val details: TextView = view.findViewById(R.id.tvItemDetails)
        val fact: TextView = view.findViewById(R.id.tvItemFact)
    }
}
