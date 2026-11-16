package com.bignerdranch.android.var5

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

class EditFragment : Fragment(R.layout.fragment_edit) {

    companion object {
        private const val ARG_ISBN = "arg_isbn"
        private const val KEY_TITLE = "title"
        private const val KEY_AUTHORS = "authors"
        private const val KEY_PAGES = "pages"
        private const val KEY_FACT = "fact"

        fun newInstance(isbn: String): EditFragment = EditFragment().apply {
            arguments = bundleOf(ARG_ISBN to isbn)
        }
    }

    private var originalIsbn: String = ""
    private lateinit var etTitle: EditText
    private lateinit var etAuthors: EditText
    private lateinit var etPages: EditText
    private lateinit var etFact: EditText
    private lateinit var tvIsbn: TextView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BookRepository.init(requireContext())

        etTitle = view.findViewById(R.id.etEditTitle)
        etAuthors = view.findViewById(R.id.etEditAuthors)
        etPages = view.findViewById(R.id.etEditPages)
        etFact = view.findViewById(R.id.etEditFact)
        tvIsbn = view.findViewById(R.id.tvEditIsbn)

        originalIsbn = arguments?.getString(ARG_ISBN).orEmpty()
        val book = BookRepository.findByIsbn(originalIsbn)
        if (book == null) {
            Toast.makeText(requireContext(), R.string.error_book_missing, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        tvIsbn.text = getString(R.string.isbn_value, book.isbn)
        if (savedInstanceState == null) {
            etTitle.setText(book.title)
            etAuthors.setText(book.authors)
            etPages.setText(if (book.pages > 0) book.pages.toString() else "")
            etFact.setText(book.fact)
        } else {
            etTitle.setText(savedInstanceState.getString(KEY_TITLE))
            etAuthors.setText(savedInstanceState.getString(KEY_AUTHORS))
            etPages.setText(savedInstanceState.getString(KEY_PAGES))
            etFact.setText(savedInstanceState.getString(KEY_FACT))
        }
        view.findViewById<Button>(R.id.btnSaveEdit).setOnClickListener { saveChanges() }
        view.findViewById<Button>(R.id.btnCancelEdit).setOnClickListener { goBack() }
        view.findViewById<Button>(R.id.btnBack).setOnClickListener { goBack() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!::etTitle.isInitialized) return
        outState.putString(KEY_TITLE, etTitle.text?.toString())
        outState.putString(KEY_AUTHORS, etAuthors.text?.toString())
        outState.putString(KEY_PAGES, etPages.text?.toString())
        outState.putString(KEY_FACT, etFact.text?.toString())
    }

    private fun saveChanges() {
        val title = etTitle.text?.toString()?.trim().orEmpty()
        if (title.isBlank()) {
            etTitle.error = getString(R.string.error_title_empty)
            return
        }
        etTitle.error = null

        val pagesText = etPages.text?.toString()?.trim().orEmpty()
        val pages = if (pagesText.isBlank()) 0 else pagesText.toIntOrNull()
        if (pages == null || pages < 0) {
            etPages.error = getString(R.string.error_pages_invalid)
            return
        }
        etPages.error = null
        val previous = BookRepository.findByIsbn(originalIsbn) ?: return
        val updated = previous.copy(
            title = title,
            authors = etAuthors.text?.toString()?.trim().orEmpty(),
            pages = pages,
            fact = etFact.text?.toString()?.trim().orEmpty()
        )
        val withFact = if (updated.fact.isBlank()) {
            updated.copy(fact = BookRepository.makeFact(updated))
        } else {
            updated
        }
        BookRepository.update(originalIsbn, withFact)
        Toast.makeText(requireContext(), R.string.edit_saved, Toast.LENGTH_SHORT).show()
        goBack()
    }

    private fun goBack() {
        if (parentFragmentManager.popBackStackImmediate()) return
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
