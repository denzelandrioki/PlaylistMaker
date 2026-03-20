package com.practicum.playlistmaker.presentation.createplaylist

import android.net.Uri
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import android.view.Gravity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.util.getParcelableCompat
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.widget.Toast

open class CreatePlaylistFragment : Fragment() {

    protected var _binding: FragmentCreatePlaylistBinding? = null
    protected val binding get() = _binding!!
    open val viewModel: CreatePlaylistViewModel by viewModel()

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriString = uri.toString()
            viewModel.setCoverUri(uriString)
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

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

        val state = viewModel.state.value
        binding.titleEdit.setText(state?.title)
        binding.descriptionEdit.setText(state?.description)
        if (state?.coverUri != null) {
            Glide.with(this).load(state.coverUri).centerCrop().into(binding.coverImage)
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

        viewModel.state.observe(viewLifecycleOwner) { s ->
            if (s.coverUri != null) {
                Glide.with(this).load(s.coverUri).centerCrop().into(binding.coverImage)
                binding.coverPlaceholderIcon.visibility = View.GONE
            } else {
                binding.coverPlaceholderIcon.visibility = View.VISIBLE
            }
            binding.createButton.isEnabled = s.createButtonEnabled
            binding.createButton.setBackgroundResource(
                if (s.createButtonEnabled) R.drawable.bg_button_create_activ else R.drawable.bg_button_create_default
            )
            binding.createButton.backgroundTintList = null
        }

        viewModel.events.observeEvent(viewLifecycleOwner) { event ->
            when (event) {
                is CreatePlaylistEvent.NavigateBack -> findNavController().navigateUp()
                is CreatePlaylistEvent.ShowDiscardDialog -> showDiscardDialog()
                is CreatePlaylistEvent.ShowToastAndNavigate -> {
                    showPlaylistCreatedToast(event.playlistName)
                    findNavController().navigateUp()
                }
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
        val dialog = AlertDialog.Builder(requireContext())
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
        val buttonColor = ContextCompat.getColor(requireContext(), R.color.primary_blue)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(buttonColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(buttonColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
