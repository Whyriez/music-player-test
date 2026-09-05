package com.whyriez.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whyriez.music.domain.model.Song
import com.whyriez.music.domain.repository.MusicRepository
import com.whyriez.music.player.MusicPlayerManager
import com.whyriez.music.ui.state.MusicUiState
import com.whyriez.music.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(
    private val repository: MusicRepository,
    val playerManager: MusicPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MusicUiState>(MusicUiState.Idle)
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var currentLoadedSongs: List<Song> = emptyList()
    private var lastQuery: String = ""

    fun searchSongs(query: String) {
        if (query.isBlank()) return
        lastQuery = query

        viewModelScope.launch {
            _uiState.value = MusicUiState.Loading
            repository.searchSongs(query).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        currentLoadedSongs = result.data
                        if (result.data.isEmpty()) {
                            _uiState.value = MusicUiState.Empty
                        } else {
                            _uiState.value = MusicUiState.Success(result.data)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = MusicUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun retryLastSearch() {
        if (lastQuery.isNotBlank()) {
            searchSongs(lastQuery)
        }
    }

    fun playSongAt(position: Int) {
        playerManager.playSongFromList(currentLoadedSongs, position)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}