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

        var initialFieldsSet = false
        viewModel.state.observe(viewLifecycleOwner) { s ->
            if (!initialFieldsSet && s.title.isNotEmpty()) {
                binding.titleEdit.setText(s.title)
                binding.descriptionEdit.setText(s.description)
                initialFieldsSet = true
            }
        }
    }
}
