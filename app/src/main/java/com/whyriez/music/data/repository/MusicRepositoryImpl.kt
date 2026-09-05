package com.whyriez.music.data.repository

import com.whyriez.music.data.remote.ItunesApiService
import com.whyriez.music.domain.model.Song
import com.whyriez.music.domain.model.toDomain
import com.whyriez.music.domain.repository.MusicRepository
import com.whyriez.music.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MusicRepositoryImpl(
    private val apiService: ItunesApiService
) : MusicRepository {

    override fun searchSongs(query: String): Flow<Resource<List<Song>>> = flow {
        try {
            val response = apiService.searchSongs(term = query)
            val songs = response.results?.mapNotNull { it.toDomain() } ?: emptyList()
            emit(Resource.Success(songs))
        } catch (e: UnknownHostException) {
            emit(Resource.Error("Tidak ada koneksi internet. Periksa jaringan Anda.", e))
        } catch (e: SocketTimeoutException) {
            emit(Resource.Error("Koneksi timeout. Silakan coba beberapa saat lagi.", e))
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan pada server (${e.code()}).", e))
        } catch (e: IOException) {
            emit(Resource.Error("Gagal membaca data dari server.", e))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Unknown error"}", e))
        }
    }.flowOn(Dispatchers.IO)
}