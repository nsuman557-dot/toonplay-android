package com.app.toonplay.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeApiResponse(
  val success: Boolean = false,
  val data: HomeDataDto? = null,
  @param:Json(name = "_fromCache") val fromCache: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class HomeDataDto(
  val hero: List<HeroDto> = emptyList(),
  val trending: List<TrendingDto> = emptyList(),
  @param:Json(name = "global_latest_releases") val globalLatestReleases: List<GlobalLatestDto> = emptyList(),
  @param:Json(name = "latest_releases") val latestReleases: List<LatestReleaseDto> = emptyList(),
  @param:Json(name = "popular_series") val popularSeries: List<MediaCardDto> = emptyList(),
  @param:Json(name = "popular_movies") val popularMovies: List<MediaCardDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class HeroDto(
  val id: String = "",
  val title: String = "",
  val overview: String? = null,
  val backdrop: String? = null,
  val poster: String? = null,
  val rating: String? = null,
  val year: String? = null,
  val type: String? = null,
  @param:Json(name = "anilist_id") val anilistId: Int? = null,
  @param:Json(name = "reanime_id") val reanimeId: String? = null,
)

@JsonClass(generateAdapter = true)
data class TrendingDto(
  val id: String = "",
  val title: String = "",
  val type: String? = null,
  val image: String? = null,
  @param:Json(name = "poster_image") val posterImage: String? = null,
)

@JsonClass(generateAdapter = true)
data class GlobalLatestDto(
  val id: String = "",
  val title: String = "",
  @param:Json(name = "reanime_id") val reanimeId: String? = null,
  @param:Json(name = "image_url") val imageUrl: String? = null,
  val type: String? = null,
  @param:Json(name = "anilist_id") val anilistId: Int? = null,
  val season: Int? = null,
  @param:Json(name = "latest_episode") val latestEpisode: Int? = null,
)

@JsonClass(generateAdapter = true)
data class LatestReleaseDto(
  val id: String = "",
  val title: String = "",
  val image: String? = null,
  val type: String? = null,
  val season: String? = null,
  val episodes: String? = null,
)

@JsonClass(generateAdapter = true)
data class MediaCardDto(
  val id: String = "",
  val title: String = "",
  val image: String? = null,
  val type: String? = null,
)
