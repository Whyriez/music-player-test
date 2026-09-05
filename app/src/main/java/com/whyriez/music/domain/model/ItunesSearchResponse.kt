package com.whyriez.music.domain.model

import com.google.gson.annotations.SerializedName

data class ItunesSearchResponse(
    @SerializedName("resultCount")
    val resultCount: Int?,
    @SerializedName("results")
    val results: List<SongDto>?
)

data class SongDto(
    @SerializedName("trackId")
    val trackId: Long?,
    @SerializedName("trackName")
    val trackName: String?,
    @SerializedName("artistName")
    val artistName: String?,
    @SerializedName("collectionName")
    val collectionName: String?,
    @SerializedName("artworkUrl100")
    val artworkUrl100: String?,
    @SerializedName("previewUrl")
    val previewUrl: String?
)

fun SongDto.toDomain(): Song? {
    val id = trackId ?: return null
    val preview = previewUrl ?: return null
    return Song(
        trackId = id,
        trackName = trackName ?: "Unknown Title",
        artistName = artistName ?: "Unknown Artist",
        collectionName = collectionName ?: "-",
        artworkUrl = artworkUrl100?.replace("100x100bb", "500x500bb") ?: "",
        previewUrl = preview
    )
}