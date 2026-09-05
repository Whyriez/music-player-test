package com.whyriez.music.data.repository

import app.cash.turbine.test
import com.whyriez.music.data.remote.ItunesApiService
import com.whyriez.music.domain.model.ItunesSearchResponse
import com.whyriez.music.domain.model.SongDto
import com.whyriez.music.utils.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MusicRepositoryImplTest {

    private val apiService: ItunesApiService = mockk()
    private lateinit var repository: MusicRepositoryImpl

    @Before
    fun setup() {
        repository = MusicRepositoryImpl(apiService)
    }

    @Test
    fun `searchSongs should emit Success with mapped songs when API returns valid data`() = runTest {
        val fakeDtos = listOf(
            SongDto(
                trackId = 101L,
                trackName = "Blinding Lights",
                artistName = "The Weeknd",
                collectionName = "After Hours",
                artworkUrl100 = "https://example.com/art100x100bb.jpg",
                previewUrl = "https://audio-preview.com/track1.m4a"
            )
        )
        val fakeResponse = ItunesSearchResponse(resultCount = 1, results = fakeDtos)

        coEvery { apiService.searchSongs("The Weeknd") } returns fakeResponse

        repository.searchSongs("The Weeknd").test {
            val item = awaitItem()
            assertTrue(item is Resource.Success)

            val songs = (item as Resource.Success).data
            assertEquals(1, songs.size)
            assertEquals(101L, songs[0].trackId)
            assertEquals("Blinding Lights", songs[0].trackName)
            assertEquals("https://example.com/art500x500bb.jpg", songs[0].artworkUrl)
            awaitComplete()
        }
    }

    @Test
    fun `searchSongs should filter out items with null previewUrl or null trackId`() = runTest {
        val fakeDtos = listOf(
            SongDto(
                trackId = null,
                trackName = "Invalid Song 1",
                artistName = "Artist",
                collectionName = "Album",
                artworkUrl100 = null,
                previewUrl = "https://audio-preview.com/track.m4a"
            ),
            SongDto(
                trackId = 102L,
                trackName = "Invalid Song 2",
                artistName = "Artist",
                collectionName = "Album",
                artworkUrl100 = null,
                previewUrl = null
            )
        )
        val fakeResponse = ItunesSearchResponse(resultCount = 2, results = fakeDtos)

        coEvery { apiService.searchSongs("test") } returns fakeResponse

        repository.searchSongs("test").test {
            val item = awaitItem()
            assertTrue(item is Resource.Success)
            assertTrue((item as Resource.Success).data.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `searchSongs should emit Error when UnknownHostException occurs`() = runTest {
        coEvery { apiService.searchSongs(any()) } throws UnknownHostException("No connection")

        repository.searchSongs("test").test {
            val item = awaitItem()
            assertTrue(item is Resource.Error)
            assertEquals("Tidak ada koneksi internet. Periksa jaringan Anda.", (item as Resource.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `searchSongs should emit Error when SocketTimeoutException occurs`() = runTest {
        coEvery { apiService.searchSongs(any()) } throws SocketTimeoutException("Read timeout")

        repository.searchSongs("test").test {
            val item = awaitItem()
            assertTrue(item is Resource.Error)
            assertEquals("Koneksi timeout. Silakan coba beberapa saat lagi.", (item as Resource.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `searchSongs should emit Error when HttpException 500 occurs`() = runTest {
        val errorResponse = Response.error<ItunesSearchResponse>(500, "".toResponseBody(null))
        coEvery { apiService.searchSongs(any()) } throws HttpException(errorResponse)

        repository.searchSongs("test").test {
            val item = awaitItem()
            assertTrue(item is Resource.Error)
            assertTrue((item as Resource.Error).message.contains("500"))
            awaitComplete()
        }
    }
}