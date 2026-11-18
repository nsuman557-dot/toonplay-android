package com.app.toonplay.data.model

import androidx.compose.runtime.Immutable

enum class SearchSource {
    ANIMESALT,
    MOVIEBOX,
}

enum class SearchContentKind {
    MOVIE,
    SERIES,
}

@Immutable
data class SearchResultItem(
    val uniqueKey: String,
    val id: String,
    val title: String,
    val posterUrl: String,
    val contentKind: SearchContentKind,
    val displayType: String,
    val source: SearchSource,
    val animeSaltId: String? = null,
    val anilistId: Int? = null,
    val reanimeId: String? = null,
    val movieBoxSubjectId: String? = null,
    val movieBoxDetailPath: String? = null,
    val link: String? = null,
    val categories: List<String> = emptyList(),
) {
    /**
     * Converts a Search result to [AnimeItem] for poster grid rendering and detail opening.
     * Deliberately sets [AnimeItem.year] to null so Search cards never display release years.
     */
    fun toAnimeItem(): AnimeItem {
        return AnimeItem(
            uid = uniqueKey,
            id = id,
            title = title,
            posterUrl = posterUrl,
            backdropUrl = posterUrl,
            overview = "",
            rating = null,
            year = null,
            typeLabel = displayType,
            seasonLabel = null,
            episodeLabel = null,
            anilistId = anilistId,
            reanimeId = reanimeId,
            rank = null,
            sectionKey = "search",
            subjectId = movieBoxSubjectId,
            detailPath = movieBoxDetailPath,
            sourceProvider = source,
            link = link,
            categories = categories,
        )
    }
}

fun AnimeItem.withoutYearForSearch(): AnimeItem {
    return if (year == null && sectionKey == "search") {
        this
    } else {
        copy(
            uid = "trending_${id.ifBlank { uid }}_${sectionKey}",
            year = null,
            sectionKey = "search",
        )
    }
}
