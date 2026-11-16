package com.bignerdranch.android.var5

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

class ListFragment : Fragment(R.layout.fragment_list) {

    companion object {
        private const val ARG_ISBN = "arg_isbn"

        fun newInstance(isbn: String?): ListFragment = ListFragment().apply {
            arguments = bundleOf(ARG_ISBN to isbn)
        }
    }

    private lateinit var listView: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var adapter: BookAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BookRepository.init(requireContext())
        listView = view.findViewById(R.id.listViewBooks)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvSubtitle = view.findViewById(R.id.tvListSubtitle)

        adapter = BookAdapter(requireContext(), mutableListOf())
        listView.adapter = adapter
        listView.divider = null
        listView.dividerHeight = 0
        listView.setOnItemClickListener { _, _, position, _ ->
            val book = adapter.getItem(position)
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EditFragment.newInstance(book.isbn))
                .addToBackStack("edit")
                .commit()
        }
        view.findViewById<Button>(R.id.btnBack).setOnClickListener { goBack() }
        arguments?.getString(ARG_ISBN)?.let { isbn ->
            val index = adapter.booksIndexOf(isbn)
            if (index >= 0) listView.post { listView.setSelection(index) }
        }
    }
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) refreshList()
    }
    private fun refreshList() {
        val books = BookRepository.all()
        adapter.submit(books)
        tvEmpty.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        tvSubtitle.text = resources.getQuantityString(
            R.plurals.books_count,
            books.size,
            books.size
        )
    }
    private fun goBack() {
        if (parentFragmentManager.popBackStackImmediate()) return
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun BookAdapter.booksIndexOf(isbn: String): Int {
        for (i in 0 until count) {
            if (getItem(i).isbn == isbn) return i
        }
        return -1
    }
}
