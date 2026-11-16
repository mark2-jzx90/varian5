package com.bignerdranch.android.var5

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class SearchFragment : Fragment(R.layout.fragment_search) {

    companion object {
        private const val KEY_ISBN_INPUT = "isbn_input"
        private const val TAG_LIST = "list"
    }
    private var searchJob: Job? = null
    private var lastBook: Book? = null
    private lateinit var etIsbn: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnToList: Button
    private lateinit var progress: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var cardLastBook: LinearLayout
    private lateinit var tvLastTitle: TextView
    private lateinit var tvLastDetails: TextView
    private lateinit var tvLastFact: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BookRepository.init(requireContext())
        etIsbn = view.findViewById(R.id.etIsbn)
        btnSearch = view.findViewById(R.id.btnSearch)
        btnToList = view.findViewById(R.id.btnToList)
        progress = view.findViewById(R.id.progressSearch)
        tvStatus = view.findViewById(R.id.tvStatus)
        cardLastBook = view.findViewById(R.id.cardLastBook)
        tvLastTitle = view.findViewById(R.id.tvLastTitle)
        tvLastDetails = view.findViewById(R.id.tvLastDetails)
        tvLastFact = view.findViewById(R.id.tvLastFact)
        savedInstanceState?.getString(KEY_ISBN_INPUT)?.let { etIsbn.setText(it) }
        etIsbn.error = null
        etIsbn.doAfterTextChanged { etIsbn.error = null }
        lastBook = BookRepository.lastBook()
        showLastBook(lastBook)
        btnSearch.setOnClickListener { startSearch() }
        etIsbn.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch()
                true
            } else {
                false
            }
        }
        btnToList.setOnClickListener { openBookList() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::etIsbn.isInitialized) {
            outState.putString(KEY_ISBN_INPUT, etIsbn.text?.toString().orEmpty())
        }
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
    }

    private fun startSearch() {
        val rawIsbn = etIsbn.text?.toString().orEmpty()
        val isbn = rawIsbn.replace("-", "").replace(" ", "").uppercase()
        if (isbn.isBlank()) {
            etIsbn.error = getString(R.string.error_isbn_empty)
            return
        }
        if (!isValidIsbn(isbn)) {
            etIsbn.error = getString(R.string.error_isbn_invalid)
            return
        }

        hideKeyboard()
        etIsbn.error = null
        setLoading(true)
        tvStatus.text = getString(R.string.searching)
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val book = OpenLibraryApi.fetchByIsbn(isbn)
                BookRepository.save(book)
                lastBook = book
                etIsbn.setText(book.isbn)
                showLastBook(book)
                tvStatus.text = getString(R.string.search_success)
            } catch (e: BookNotFoundException) {
                tvStatus.text = getString(R.string.error_book_not_found, e.isbn)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                tvStatus.text = getString(R.string.error_network)
            } catch (e: Exception) {
                tvStatus.text = getString(R.string.error_unknown)
            } finally {
                setLoading(false)
            }
        }
    }
    private fun showLastBook(book: Book?) {
        if (book == null) {
            cardLastBook.visibility = View.GONE
            return
        }
        cardLastBook.visibility = View.VISIBLE
        tvLastTitle.text = book.title.ifBlank { getString(R.string.unknown_title) }
        tvLastDetails.text = getString(
            R.string.last_book_details,
            book.isbn,
            book.authors.ifBlank { getString(R.string.unknown_author) },
            book.publishDate.ifBlank { getString(R.string.unknown_year) },
            if (book.pages > 0) book.pages.toString() else getString(R.string.unknown_pages),
            book.publishers.ifBlank { getString(R.string.unknown_publisher) }
        )
        tvLastFact.text = getString(
            R.string.fact_format,
            book.fact.ifBlank { BookRepository.makeFact(book) }
        )
    }
    private fun openBookList() {
        val fragment = ListFragment.newInstance(lastBook?.isbn)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(TAG_LIST)
            .commit()
    }
    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnSearch.isEnabled = !loading
        etIsbn.isEnabled = !loading
    }
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>() ?: return
        imm.hideSoftInputFromWindow(etIsbn.windowToken, 0)
    }
    private fun isValidIsbn(isbn: String): Boolean =
        isbn.matches(Regex("^\\d{9}[\\dX]$")) || isbn.matches(Regex("^\\d{13}$"))
}
