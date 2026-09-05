package com.whyriez.music.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.whyriez.music.R
import com.whyriez.music.data.remote.NetworkClient
import com.whyriez.music.data.repository.MusicRepositoryImpl
import com.whyriez.music.databinding.ActivityMainBinding
import com.whyriez.music.domain.model.Song
import com.whyriez.music.player.MusicPlayerManager
import com.whyriez.music.ui.adapter.SongAdapter
import com.whyriez.music.ui.state.MusicUiState
import com.whyriez.music.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var songAdapter: SongAdapter

    private var isUserTrackingSeekBar = false
    private val viewModel: MusicViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = MusicRepositoryImpl(NetworkClient.apiService)
                val playerManager = MusicPlayerManager(applicationContext)
                return MusicViewModel(repository, playerManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupSearch()
        setupPlayerControls()
        setupErrorAndRetry()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter { song, position ->
            viewModel.playSongAt(position)
        }

        binding.rvSong.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = songAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text?.toString().orEmpty().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchSongs(query)
                    hideKeyboard()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupErrorAndRetry() {
        binding.btnRetry.setOnClickListener {
            viewModel.retryLastSearch()
        }
    }

    private fun setupPlayerControls() {
        binding.musicPlayer.apply {
            btnPlayPause.setOnClickListener {
                viewModel.playerManager.togglePlayPause()
            }

            btnNext.setOnClickListener {
                viewModel.playerManager.playNext()
            }

            btnPrevious.setOnClickListener {
                viewModel.playerManager.playPrevious()
            }

            btnClosePlayer.setOnClickListener {
                viewModel.playerManager.stopPlayback()
            }

            seekBarMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        tvCurrentTime.text = formatDuration(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isUserTrackingSeekBar = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.let {
                        viewModel.playerManager.seekTo(it.progress.toLong())
                    }
                    isUserTrackingSeekBar = false
                }
            })
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        renderUiState(state)
                    }
                }
                launch {
                    viewModel.playerManager.currentSong.collect { song ->
                        updatePlayerInfo(song)
                        updateAdapterPlaybackIndicator()
                    }
                }
                launch {
                    viewModel.playerManager.isPlaying.collect { isPlaying ->
                        val iconRes = if (isPlaying) {
                            android.R.drawable.ic_media_pause
                        } else {
                            android.R.drawable.ic_media_play
                        }
                        binding.musicPlayer
                        binding.musicPlayer.btnPlayPause.setImageResource(iconRes)
                        updateAdapterPlaybackIndicator()
                    }
                }
                launch {
                    viewModel.playerManager.currentPosition.collect { positionMs ->
                        if (!isUserTrackingSeekBar) {
                            binding.musicPlayer.seekBarMusic.progress = positionMs.toInt()
                            binding.musicPlayer.tvCurrentTime.text = formatDuration(positionMs)
                        }
                    }
                }
                launch {
                    viewModel.playerManager.duration.collect { durationMs ->
                        binding.musicPlayer.seekBarMusic.max = durationMs.toInt()
                        binding.musicPlayer.tvTotalTime.text = formatDuration(durationMs)
                    }
                }
            }
        }
    }

    private fun renderUiState(state: MusicUiState) {
        binding.progressBar.isVisible = state is MusicUiState.Loading
        binding.rvSong.isVisible = state is MusicUiState.Success
        binding.layoutError.isVisible = state is MusicUiState.Error
        binding.tvEmptyState.isVisible = state is MusicUiState.Empty

        when (state) {
            is MusicUiState.Success -> {
                songAdapter.submitList(state.songs)
            }
            is MusicUiState.Error -> {
                binding.tvErrorMessage.text = state.message
            }
            is MusicUiState.Empty -> {
                binding.tvEmptyState.text = getString(R.string.no_songs_found)
            }
            is MusicUiState.Idle -> {
                binding.tvEmptyState.isVisible = true
                binding.tvEmptyState.text = getString(R.string.search_hint_initial)
            }
            is MusicUiState.Loading -> Unit
        }
    }

    private fun updatePlayerInfo(song: Song?) {
        binding.musicPlayer.apply {
            if (song != null) {
                root.visibility = View.VISIBLE
                tvPlayerCurrentTitle.text = song.trackName
                tvPlayerCurrentArtist.text = song.artistName

                Glide.with(imgPlayerArtwork)
                    .load(song.artworkUrl)
                    .placeholder(R.drawable.ic_android_black_24dp)
                    .error(R.drawable.ic_android_black_24dp)
                    .into(imgPlayerArtwork)

                btnPlayPause.isEnabled = true
                btnNext.isEnabled = true
                btnPrevious.isEnabled = true
                seekBarMusic.isEnabled = true
            } else {
                root.visibility = View.GONE
            }
        }
    }

    private fun updateAdapterPlaybackIndicator() {
        val currentTrackId = viewModel.playerManager.currentSong.value?.trackId
        val isPlaying = viewModel.playerManager.isPlaying.value
        songAdapter.updatePlaybackState(currentTrackId, isPlaying)
    }

    private fun formatDuration(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }
}