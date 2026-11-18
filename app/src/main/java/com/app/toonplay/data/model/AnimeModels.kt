package com.app.toonplay.data.model

import androidx.compose.runtime.Immutable

enum class AnimeSectionTab(val label: String) {
  HOME("Home"),
  ANIME("Anime"),
  CARTOON("Cartoon"),
  LATEST("Latest"),
  CHILDHOOD("Childhood Shows"),
  SERIES("TV Series"),
  MOVIES("Movie"),
  PROFILE("Profile"),
  SAVED("Watchlist"),
}

enum class LatestLanguageFilter(val label: String) {
  JAP_ENG("JAP/ENG"),
  HIN_TAM_TEL("HIN/TAM/TEL"),
}

enum class DeviceFormFactor(val displayName: String) {
  MOBILE("Mobile"),
  TABLET("Tablet"),
  BIG_TABLET("Big Tablet"),
  DESKTOP("Desktop / Windows"),
  TV("Android TV"),
}

enum class LayoutOverride(val displayName: String) {
  AUTO("Auto Detect"),
  MOBILE("Mobile"),
  TABLET("Tablet"),
  BIG_TABLET("Big Tablet"),
  DESKTOP("Desktop / Windows"),
  TV("Android TV"),
}

@Immutable
data class AnimeItem(
  val uid: String,
  val id: String,
  val title: String,
  val posterUrl: String,
  val backdropUrl: String,
  val overview: String,
  val rating: String?,
  val year: String?,
  val typeLabel: String,
  val seasonLabel: String?,
  val episodeLabel: String?,
  val anilistId: Int?,
  val reanimeId: String?,
  val rank: Int? = null,
  val sectionKey: String,
)

@Immutable
data class HomeFeed(
  val hero: List<AnimeItem> = emptyList(),
  val childhoodShows: List<AnimeItem> = emptyList(),
  val globalLatest: List<AnimeItem> = emptyList(),
  val latestReleases: List<AnimeItem> = emptyList(),
  val combinedLatest: List<AnimeItem> = emptyList(),
  val popularSeries: List<AnimeItem> = emptyList(),
  val popularMovies: List<AnimeItem> = emptyList(),
  val allUnique: List<AnimeItem> = emptyList(),
  val fromCache: Boolean = false,
)
