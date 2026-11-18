package com.app.toonplay.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimeSaltSearchResponseDto(
    val success: Boolean = false,
    val query: String = "",
    val pagination: AnimeSaltPaginationDto? = null,
    val data: List<AnimeSaltSearchItemDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class AnimeSaltPaginationDto(
    val currentPage: Int = 1,
    val totalPages: Int = 1,
)

@JsonClass(generateAdapter = true)
data class AnimeSaltSearchItemDto(
    val title: String = "",
    val image: String? = null,
    val type: String? = null,
    val id: String = "",
    val link: String? = null,
    val categories: List<String>? = null,
    @param:Json(name = "anilist_id") val anilistId: Int? = null,
    val subjectId: String? = null,
    @param:Json(name = "reanime_id") val reanimeId: String? = null,
)

@JsonClass(generateAdapter = true)
data class MovieboxSearchResponseDto(
    val success: Boolean = false,
    val query: String = "",
    val page: Int = 1,
    val perPage: Int = 20,
    val count: Int = 0,
    val data: List<MovieboxSearchItemDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class MovieboxSearchItemDto(
    val title: String = "",
    val subjectId: String = "",
    val detailPath: String = "",
    val subjectType: Int = 0,
    val image: String? = null,
    val type: String? = null,
)

@JsonClass(generateAdapter = true)
data class AnimeInfoApiResponseDto(
    val success: Boolean = false,
    val anime: AnimeInfoDataDto? = null,
)

@JsonClass(generateAdapter = true)
data class AnimeInfoDataDto(
    val id: String? = null,
    @param:Json(name = "anime_id") val animeId: String? = null,
    @param:Json(name = "reanime_id") val reanimeId: String? = null,
    val slug: String? = null,
    val subjectId: String? = null,
    @param:Json(name = "subject_id") val subjectIdSnake: String? = null,
    val title: String? = null,
    @param:Json(name = "alternative_title") val alternativeTitle: String? = null,
    @param:Json(name = "alternative_titles") val alternativeTitles: List<String>? = null,
    val image: String? = null,
    val backdrop: String? = null,
    val overview: String? = null,
    val year: String? = null,
    val type: String? = null,
    val rating: String? = null,
    val seasons: Int? = null,
    val episodes: Int? = null,
    val duration: String? = null,
    val genres: List<String>? = null,
    val languages: List<String>? = null,
    @param:Json(name = "anilist_id") val anilistId: Int? = null,
)

@JsonClass(generateAdapter = true)
data class MovieboxInfoApiResponseDto(
    val success: Boolean = false,
    val anime: MovieboxInfoDataDto? = null,
)

@JsonClass(generateAdapter = true)
data class MovieboxInfoDataDto(
    val id: String? = null,
    val subjectId: String? = null,
    val title: String? = null,
    val type: String? = null,
    val image: String? = null,
    val backdrop: String? = null,
    val overview: String? = null,
    val year: String? = null,
    val genres: List<String>? = null,
    val languages: List<String>? = null,
    val duration: String? = null,
    val seasons: Int? = null,
    val episodes: Int? = null,
    val rating: String? = null,
)
