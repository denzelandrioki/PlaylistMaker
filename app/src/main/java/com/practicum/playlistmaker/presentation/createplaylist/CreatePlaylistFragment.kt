package com.practicum.playlistmaker.presentation.createplaylist

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import android.view.Gravity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.practicum.playlistmaker.domain.entity.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.widget.Toast

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreatePlaylistViewModel by viewModel()

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCoverUri(uri.toString())
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.coverImage)
            binding.coverPlaceholderIcon.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelableCompat<Track>("trackToAdd")?.let { viewModel.setTrackToAdd(it) }

        binding.toolbar.setNavigationOnClickListener { viewModel.onBackPressed() }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onBackPressed()
                }
            }
        )

        binding.titleEdit.setText(viewModel.title.value)
        binding.descriptionEdit.setText(viewModel.description.value)
        if (viewModel.coverUri.value != null) {
            Glide.with(this).load(viewModel.coverUri.value).centerCrop().into(binding.coverImage)
            binding.coverPlaceholderIcon.visibility = View.GONE
        } else {
            binding.coverPlaceholderIcon.visibility = View.VISIBLE
        }

        binding.coverImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.titleEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setTitle(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.descriptionEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setDescription(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.createButtonEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.createButton.isEnabled = enabled
            binding.createButton.setBackgroundResource(
                if (enabled) R.drawable.bg_button_create_activ else R.drawable.bg_button_create_default
            )
            binding.createButton.backgroundTintList = null
        }

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is CreatePlaylistEvent.NavigateBack -> {
                    viewModel.consumeEvent()
                    findNavController().navigateUp()
                }
                is CreatePlaylistEvent.ShowDiscardDialog -> {
                    showDiscardDialog()
                    viewModel.consumeEvent()
                }
                is CreatePlaylistEvent.ShowToastAndNavigate -> {
                    viewModel.consumeEvent()
                    showPlaylistCreatedToast(event.playlistName)
                    findNavController().navigateUp()
                }
                null -> {}
            }
        }

        binding.createButton.setOnClickListener { viewModel.createPlaylist() }
        binding.createButton.backgroundTintList = null
    }

    private fun showPlaylistCreatedToast(playlistName: String) {
        val ctx = requireContext().applicationContext
        val toast = Toast(ctx)
        val view = layoutInflater.inflate(R.layout.toast_playlist_created, null)
        val widthPx = ctx.resources.displayMetrics.widthPixels
        view.layoutParams = android.view.ViewGroup.LayoutParams(
            widthPx,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.findViewById<android.widget.TextView>(R.id.toast_playlist_text).text =
            getString(R.string.playlist_created_toast, playlistName)
        toast.view = view
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        toast.show()
    }

    private fun showDiscardDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.discard_dialog_title)
            .setMessage(R.string.discard_dialog_message)
            .setNegativeButton(R.string.discard_dialog_cancel) { _, _ ->
                viewModel.onDiscardDialogDismissed()
            }
            .setPositiveButton(R.string.discard_dialog_confirm) { _, _ ->
                viewModel.onDiscardConfirm()
            }
            .setOnCancelListener { viewModel.onDiscardDialogDismissed() }
            .show()
    }

    @Suppress("DEPRECATION")
    private inline fun <reified T : android.os.Parcelable> Bundle.getParcelableCompat(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            getParcelable(key, T::class.java)
        else
            getParcelable(key)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
