package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.Instance.NetworkClient
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.dto.toDomain
import com.practicum.playlistmaker.model.PlaylistApiResponse
import com.practicum.playlistmaker.model.Track
import com.practicum.playlistmaker.ui.TracksAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var tracksAdapter: TracksAdapter
    private var searchText: String? = null
    private var lastSearchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.root.findViewById<MaterialToolbar>(R.id.searchToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Восстанавливаем текст из состояния, если оно было сохранено
        savedInstanceState?.let {
            searchText = it.getString(KEY_SEARCH_TEXT)
        }

        // Устанавливаем восстановленный текст в EditText
        binding.searchEditText.setText(searchText)

        // TextWatcher для показа/скрытия clearButton
        binding.searchEditText.addTextChangedListener(textWatcher)

        // Кнопка очистки
        binding.clearButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            binding.searchEditText.clearFocus()
            hideKeyboard()
            clearSearchResultViews()
        }

        // Инициализация адаптера и RecyclerView
        tracksAdapter = TracksAdapter(mutableListOf())
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tracksRecyclerView.adapter = tracksAdapter

        // Обработка нажатия "Done" на клавиатуре
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                    hideKeyboard()
                }
                true
            } else false
        }

        // Кнопка retry (для errorPlaceholder)
        binding.retryButton.setOnClickListener {
            lastSearchQuery?.let { performSearch(it) }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchText = s.toString()
            binding.clearButton.isVisible = !s.isNullOrEmpty()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }




    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(KEY_SEARCH_TEXT)
        binding.searchEditText.setText(searchText)
    }

    override fun onDestroy() {
        binding.searchEditText.removeTextChangedListener(textWatcher)
        super.onDestroy()
    }

    private fun performSearch(query: String) {
        lastSearchQuery = query
        // Скрываем старые результаты, плейсхолдеры
        clearSearchResultViews()

        // Тут можешь показать ProgressBar (если захочешь)

        NetworkClient.itunesApi.searchTracks(query)
            .enqueue(object : Callback<PlaylistApiResponse> {
                override fun onResponse(
                    call: Call<PlaylistApiResponse>,
                    response: Response<PlaylistApiResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val tracks = response.body()!!.results
                            ?.mapNotNull { it.toDomain() }
                            ?: emptyList()

                        if (tracks.isEmpty()) {
                            showEmptyPlaceholder()
                        } else {
                            showTracks(tracks)
                        }
                    } else {
                        showErrorPlaceholder()
                    }
                }

                override fun onFailure(call: Call<PlaylistApiResponse>, t: Throwable) {
                    showErrorPlaceholder()
                }
            })
    }

    private fun showTracks(tracks: List<Track>) {
        binding.emptyPlaceholder.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.GONE
        binding.tracksRecyclerView.visibility = View.VISIBLE
        tracksAdapter.setData(tracks)
    }

    private fun showEmptyPlaceholder() {
        binding.tracksRecyclerView.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.GONE
        binding.emptyPlaceholder.visibility = View.VISIBLE
    }

    private fun showErrorPlaceholder() {
        binding.tracksRecyclerView.visibility = View.GONE
        binding.emptyPlaceholder.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.VISIBLE
    }

    private fun clearSearchResultViews() {
        tracksAdapter.setData(emptyList())
        binding.tracksRecyclerView.visibility = View.GONE
        binding.emptyPlaceholder.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.GONE
    }

    companion object {
        private const val KEY_SEARCH_TEXT = "KEY_SEARCH_TEXT"
    }
}
