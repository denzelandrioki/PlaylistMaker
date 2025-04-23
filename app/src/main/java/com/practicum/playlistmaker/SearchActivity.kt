package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.practicum.playlistmaker.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var searchText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ← назад
        binding.backButton.setOnClickListener { finish() }


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


    }


    // --- TextWatcher ------------------------------------------------------------------
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Сохраняем текст в переменную для дальнейшего сохранения
            searchText = s.toString()

            // Показываем или скрываем кнопку "×" в зависимости от наличия текста
            binding.clearButton.visibility =
                if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
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


}