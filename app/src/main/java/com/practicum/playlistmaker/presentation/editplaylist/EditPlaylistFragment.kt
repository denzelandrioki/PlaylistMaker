package com.practicum.playlistmaker.presentation.editplaylist

import android.view.View
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.createplaylist.CreatePlaylistFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : CreatePlaylistFragment() {

    override val viewModel: EditPlaylistViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.edit_playlist_screen_title)
        binding.createButton.text = getString(R.string.edit_playlist_button)

        // Заполняем поля только один раз при первой загрузке данных
        var initialFieldsSet = false
        
        // ViewModel уже загружает данные в init, просто наблюдаем за state
        viewModel.state.observe(viewLifecycleOwner) { s ->
            // Заполняем текстовые поля только один раз при первой загрузке
            if (!initialFieldsSet && s.title.isNotEmpty()) {
                binding.titleEdit.setText(s.title)
                binding.descriptionEdit.setText(s.description)
                initialFieldsSet = true
            }
            
            // Обновляем обложку при изменении URI (теперь каждый файл имеет уникальное имя, поэтому URI всегда уникален)
            if (s.coverUri != null) {
                com.bumptech.glide.Glide.with(this)
                    .load(s.coverUri)
                    .centerCrop()
                    .into(binding.coverImage)
                binding.coverPlaceholderIcon.visibility = android.view.View.GONE
            } else {
                com.bumptech.glide.Glide.with(this).clear(binding.coverImage)
                binding.coverPlaceholderIcon.visibility = android.view.View.VISIBLE
            }
        }
    }
}
