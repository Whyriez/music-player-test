package com.whyriez.music.ui.state

import com.whyriez.music.domain.model.Song

sealed interface MusicUiState {
    object Idle : MusicUiState
    object Loading : MusicUiState
    data class Success(val songs: List<Song>) : MusicUiState
    object Empty : MusicUiState
    data class Error(val message: String) : MusicUiState
}