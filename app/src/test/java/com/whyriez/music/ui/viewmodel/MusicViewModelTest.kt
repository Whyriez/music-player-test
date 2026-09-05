package com.whyriez.music.ui.viewmodel

import app.cash.turbine.test
import com.whyriez.music.domain.model.Song
import com.whyriez.music.domain.repository.MusicRepository
import com.whyriez.music.player.MusicPlayerManager
import com.whyriez.music.ui.state.MusicUiState
import com.whyriez.music.util.MainDispatcherRule
import com.whyriez.music.utils.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MusicViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: MusicRepository = mockk()
    private val playerManager: MusicPlayerManager = mockk(relaxed = true)
    private lateinit var viewModel: MusicViewModel

    private val dummySongs = listOf(
        Song(
            trackId = 1L,
            trackName = "Song A",
            artistName = "Artist A",
            collectionName = "Album A",
            artworkUrl = "https://example.com/art.jpg",
            previewUrl = "https://example.com/audio.m4a"
        )
    )

    @Before
    fun setup() {
        viewModel = MusicViewModel(repository, playerManager)
    }

    @Test
    fun `initial uiState should be Idle`() {
        assertEquals(MusicUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `searchSongs with valid query should transition to Success when songs are found`() = runTest {
        val flow = MutableSharedFlow<Resource<List<Song>>>()
        every { repository.searchSongs("Coldplay") } returns flow

        viewModel.uiState.test {
            assertEquals(MusicUiState.Idle, awaitItem())

            viewModel.searchSongs("Coldplay")
            assertEquals(MusicUiState.Loading, awaitItem())

            flow.emit(Resource.Success(dummySongs))
            val successState = awaitItem()
            assertTrue(successState is MusicUiState.Success)
            assertEquals(dummySongs, (successState as MusicUiState.Success).songs)
        }
    }

    @Test
    fun `searchSongs should transition to Empty when repository returns empty list`() = runTest {
        val flow = MutableSharedFlow<Resource<List<Song>>>()
        every { repository.searchSongs("randomQuery") } returns flow

        viewModel.uiState.test {
            assertEquals(MusicUiState.Idle, awaitItem())

            viewModel.searchSongs("randomQuery")
            assertEquals(MusicUiState.Loading, awaitItem())

            flow.emit(Resource.Success(emptyList()))
            assertEquals(MusicUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `searchSongs should transition to Error when repository returns Resource Error`() = runTest {
        val flow = MutableSharedFlow<Resource<List<Song>>>()
        val errorMessage = "Koneksi internet terputus."
        every { repository.searchSongs("Coldplay") } returns flow

        viewModel.uiState.test {
            assertEquals(MusicUiState.Idle, awaitItem())

            viewModel.searchSongs("Coldplay")
            assertEquals(MusicUiState.Loading, awaitItem())

            flow.emit(Resource.Error(errorMessage))
            val errorState = awaitItem()
            assertTrue(errorState is MusicUiState.Error)
            assertEquals(errorMessage, (errorState as MusicUiState.Error).message)
        }
    }

    @Test
    fun `searchSongs with blank query should not trigger repository`() = runTest {
        viewModel.searchSongs("   ")

        verify(exactly = 0) { repository.searchSongs(any()) }
        assertEquals(MusicUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `retryLastSearch should re-execute previous search query`() = runTest {
        every { repository.searchSongs("Adele") } returns flowOf(Resource.Success(dummySongs))

        viewModel.searchSongs("Adele")
        viewModel.retryLastSearch()

        verify(exactly = 2) { repository.searchSongs("Adele") }
    }

    @Test
    fun `playSongAt should delegate playback to playerManager with current loaded songs`() = runTest {
        every { repository.searchSongs("Coldplay") } returns flowOf(Resource.Success(dummySongs))

        viewModel.searchSongs("Coldplay")
        viewModel.playSongAt(0)

        verify(exactly = 1) { playerManager.playSongFromList(dummySongs, 0) }
    }
}