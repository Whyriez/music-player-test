package com.whyriez.music.data.remote

import com.whyriez.music.domain.model.ItunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 30
    ): ItunesSearchResponse
}