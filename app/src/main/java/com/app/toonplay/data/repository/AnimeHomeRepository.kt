package com.app.toonplay.data.repository

import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.HomeFeed
import com.app.toonplay.data.remote.ToonPlayApiService
import com.app.toonplay.data.remote.HomeDataDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnimeHomeRepository(private val apiService: ToonPlayApiService) {

  suspend fun fetchHomeFeed(): HomeFeed =
    withContext(Dispatchers.IO) {
      val response = apiService.getHomeFeed()
      val data = response.data ?: throw IllegalStateException("Empty feed returned from ToonPlay")
      mapFeed(data, response.fromCache)
    }

  private fun mapFeed(data: HomeDataDto, fromCache: Boolean): HomeFeed {
    val heroItems =
      data.hero.mapIndexed { idx, dto ->
        val poster = dto.poster?.takeIf { it.isNotBlank() } ?: dto.backdrop.orEmpty()
        val backdrop = dto.backdrop?.takeIf { it.isNotBlank() } ?: poster
        AnimeItem(
          uid = "hero_${dto.id}_$idx",
          id = dto.id,
          title = dto.title,
          posterUrl = poster,
          backdropUrl = backdrop,
          overview = dto.overview?.trim().orEmpty(),
          rating = dto.rating?.takeIf { it.isNotBlank() },
          year = dto.year?.takeIf { it.isNotBlank() },
          typeLabel = formatType(dto.type),
          seasonLabel = null,
          episodeLabel = null,
          anilistId = dto.anilistId,
          reanimeId = dto.reanimeId,
          rank = idx + 1,
          sectionKey = "hero",
        )
      }

    val trendingItems =
      data.trending.mapIndexed { idx, dto ->
        val poster = dto.posterImage?.takeIf { it.isNotBlank() } ?: dto.image.orEmpty()
        AnimeItem(
          uid = "trending_${dto.id}_$idx",
          id = dto.id,
          title = dto.title,
          posterUrl = poster,
          backdropUrl = poster,
          overview = "",
          rating = null,
          year = null,
          typeLabel = formatType(dto.type),
          seasonLabel = null,
          episodeLabel = null,
          anilistId = null,
          reanimeId = null,
          rank = idx + 1,
          sectionKey = "trending",
        )
      }

    val globalItems =
      data.globalLatestReleases.mapIndexed { idx, dto ->
        val poster = dto.imageUrl.orEmpty()
        AnimeItem(
          uid = "global_${dto.id}_$idx",
          id = dto.id,
          title = dto.title,
          posterUrl = poster,
          backdropUrl = poster,
          overview = "",
          rating = null,
          year = null,
          typeLabel = formatType(dto.type),
          seasonLabel = dto.season?.let { "Season $it" },
          episodeLabel = dto.latestEpisode?.let { "EP $it" },
          anilistId = dto.anilistId,
          reanimeId = dto.reanimeId,
          rank = null,
          sectionKey = "global",
        )
      }

    val latestItems =
      data.latestReleases.mapIndexed { idx, dto ->
        val poster = dto.image.orEmpty()
        AnimeItem(
          uid = "latest_${dto.id}_$idx",
          id = dto.id,
          title = dto.title,
          posterUrl = poster,
          backdropUrl = poster,
          overview = "",
          rating = null,
          year = null,
          typeLabel = formatType(dto.type),
          seasonLabel = dto.season?.takeIf { it.isNotBlank() },
          episodeLabel = dto.episodes?.takeIf { it.isNotBlank() },
          anilistId = null,
          reanimeId = null,
          rank = null,
          sectionKey = "latest",
        )
      }

    val seriesItems =
      data.popularSeries.mapIndexed { idx, dto ->
        val poster = dto.image.orEmpty()
        AnimeItem(
          uid = "series_${dto.id}_$idx",
          id = dto.id,
          title = dto.title,
          posterUrl = poster,
          backdropUrl = poster,
          overview = "",
          rating = null,
          year = null,
          typeLabel = formatType(dto.type),
          seasonLabel = null,
          episodeLabel = null,
          anilistId = null,
          reanimeId = null,
          rank = null,
          sectionKey = "series",
        )
      }

    val movieItems =
      data.popularMovies.mapIndexed { idx, dto ->
        val poster = dto.image.orEmpty()
        AnimeItem(
          uid = "movie_${dto.id}_$idx",
          id = dto.id,
          title = dto.title,
          posterUrl = poster,
          backdropUrl = poster,
          overview = "",
          rating = null,
          year = null,
          typeLabel = formatType(dto.type),
          seasonLabel = null,
          episodeLabel = null,
          anilistId = null,
          reanimeId = null,
          rank = null,
          sectionKey = "movies",
        )
      }

    val combinedLatest =
      (latestItems + globalItems)
        .distinctBy { it.id }

    val combined =
      (heroItems + trendingItems + globalItems + latestItems + seriesItems + movieItems)
        .distinctBy { it.id }

    return HomeFeed(
      hero = heroItems,
      childhoodShows = trendingItems,
      globalLatest = globalItems,
      latestReleases = latestItems,
      combinedLatest = combinedLatest,
      popularSeries = seriesItems,
      popularMovies = movieItems,
      allUnique = combined,
      fromCache = fromCache,
    )
  }

  private fun formatType(raw: String?): String {
    return when (raw?.uppercase()) {
      "SERIES", "TV" -> "SERIES"
      "MOVIE" -> "MOVIE"
      "ONA" -> "ONA"
      "TV_SHORT" -> "SHORT"
      null, "" -> "ANIME"
      else -> raw.uppercase()
    }
  }
}
