package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.model.Track
import com.practicum.playlistmaker.ui.TracksAdapter

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var searchText: String? = null

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

        // TextWatcher
        binding.searchEditText.addTextChangedListener(textWatcher)

        // «×»
        binding.clearButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            binding.searchEditText.clearFocus()
            hideKeyboard()
        }


        val adapter = TracksAdapter(demoTracks)
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tracksRecyclerView.adapter = adapter


    }


    // --- TextWatcher ------------------------------------------------------------------
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Сохраняем текст в переменную для дальнейшего сохранения
            searchText = s.toString()

            // Показываем или скрываем кнопку "×" в зависимости от наличия текста
            binding.clearButton.isVisible = !s.isNullOrEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            // Здесь можно запускать логику поиска, если нужно
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }


    // Сохранение состояния при изменении конфигурации
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем значение из EditText в Bundle
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }

    // Восстановление состояния при пересоздании активности
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Восстанавливаем значение из Bundle
        searchText = savedInstanceState.getString(KEY_SEARCH_TEXT)
        // Устанавливаем восстановленный текст в EditText
        binding.searchEditText.setText(searchText)
    }



    override fun onDestroy() {
        binding.searchEditText.removeTextChangedListener(textWatcher)
        super.onDestroy()
    }


    // Ключ для сохранения текста
    companion object {
        private const val KEY_SEARCH_TEXT = "KEY_SEARCH_TEXT"
    }



    private val demoTracks = arrayListOf(
        Track(
            "Smells Like Teen Spirit",
            "Nirvana",
            "5:01",
            "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
        ),
        Track(
            "Billie Jean",
            "Michael Jackson",
            "4:35",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
        ),
        Track(
            "Stayin' Alive",
            "Bee Gees (Bee Gees) (Bee Gees) (Bee Gees)  (Bee Gees) (Bee Gees) (Bee Gees)",
            "4:10",
            "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
        ),
        Track(
            "Whole Lotta Love",
            "Led Zeppelin",
            "5:33",
            "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
        ),
        Track(
            "Sweet Child O'Mine",
            "Guns N' Roses",
            "5:03",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
        ),
        Track(
            "Сладкий Бубалех",
            "Группа Губбы",
            "34:03",
            "https://is5-ssl.mzstatic.com/imsddf/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
        ),
    )


}