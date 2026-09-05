package com.whyriez.music.domain.repository

import com.whyriez.music.domain.model.Song
import com.whyriez.music.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun searchSongs(query: String): Flow<Resource<List<Song>>>
}