package com.app.toonplay.data.repository

import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.SearchContentKind
import com.app.toonplay.data.model.SearchResultItem
import com.app.toonplay.data.model.SearchSource
import com.app.toonplay.data.remote.AnimeInfoApiResponseDto
import com.app.toonplay.data.remote.AnimeSaltSearchItemDto
import com.app.toonplay.data.remote.AnimeSaltSearchResponseDto
import com.app.toonplay.data.remote.MovieboxInfoApiResponseDto
import com.app.toonplay.data.remote.MovieboxSearchItemDto
import com.app.toonplay.data.remote.MovieboxSearchResponseDto
import com.app.toonplay.data.remote.ToonPlayApiService
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class SearchPageResult(
    val query: String,
    val page: Int,
    val items: List<SearchResultItem>,
    val animeSaltHasMore: Boolean,
    val movieBoxHasMore: Boolean,
    val animeSaltSucceeded: Boolean,
    val movieBoxSucceeded: Boolean,
    val errorMessage: String? = null,
) {
    val hasMore: Boolean
        get() = animeSaltHasMore || movieBoxHasMore

    val hasAnySuccess: Boolean
        get() = animeSaltSucceeded || movieBoxSucceeded

    val hasPartialFailure: Boolean
        get() = hasAnySuccess && (!animeSaltSucceeded || !movieBoxSucceeded)
}

private data class CacheEntry<T>(
    val value: T,
    val fetchedAtMs: Long,
)

private sealed interface SourceFetchOutcome<out T> {
    data class Success<T>(val data: T) : SourceFetchOutcome<T>

    data class Failure(val error: Throwable) : SourceFetchOutcome<Nothing>
}

class SearchRepository(
    private val apiService: ToonPlayApiService,
    private val clockMs: () -> Long = { System.currentTimeMillis() },
) {

    private val cacheLock = Any()
    private val memoryCache =
        object : LinkedHashMap<String, CacheEntry<Any>>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry<Any>>?): Boolean {
                return size > MEMORY_CACHE_MAX_ENTRIES
            }
        }

    suspend fun searchCatalog(
        query: String,
        page: Int = 1,
        includeAnimeSalt: Boolean = true,
        includeMoviebox: Boolean = true,
    ): SearchPageResult {
        val cleanQuery = query.trim()
        val cleanPage = page.coerceAtLeast(1)
        if (cleanQuery.isEmpty() || (!includeAnimeSalt && !includeMoviebox)) {
            return SearchPageResult(
                query = cleanQuery,
                page = cleanPage,
                items = emptyList(),
                animeSaltHasMore = false,
                movieBoxHasMore = false,
                animeSaltSucceeded = true,
                movieBoxSucceeded = true,
            )
        }

        val cacheKey = buildSearchCacheKey(cleanQuery, cleanPage, includeAnimeSalt, includeMoviebox)
        getCached<SearchPageResult>(cacheKey, SEARCH_TTL_MS)?.let { cached ->
            return cached
        }

        return withContext(Dispatchers.IO) {
            coroutineScope {
                val animeSaltDeferred =
                    if (includeAnimeSalt) {
                        async { fetchAnimeSaltPage(cleanQuery, cleanPage) }
                    } else {
                        null
                    }
                val movieBoxDeferred =
                    if (includeMoviebox) {
                        async { fetchMovieboxPage(cleanQuery, cleanPage) }
                    } else {
                        null
                    }

                val animeSaltOutcome = animeSaltDeferred?.await()
                val movieBoxOutcome = movieBoxDeferred?.await()

                val animeSaltResponse = (animeSaltOutcome as? SourceFetchOutcome.Success)?.data
                val movieBoxResponse = (movieBoxOutcome as? SourceFetchOutcome.Success)?.data

                val animeSaltSucceeded = !includeAnimeSalt || animeSaltResponse?.success == true
                val movieBoxSucceeded = !includeMoviebox || movieBoxResponse?.success == true

                val animeSaltError = (animeSaltOutcome as? SourceFetchOutcome.Failure)?.error
                val movieBoxError = (movieBoxOutcome as? SourceFetchOutcome.Failure)?.error

                val mappedAnimeSalt =
                    if (animeSaltResponse?.success == true) {
                        animeSaltResponse.data.mapNotNull(::mapAnimeSaltItem)
                    } else {
                        emptyList()
                    }

                val mappedMovieBox =
                    if (movieBoxResponse?.success == true) {
                        movieBoxResponse.data.mapNotNull(::mapMovieboxItem)
                    } else {
                        emptyList()
                    }

                val mergedItems =
                    rankAndDeduplicateSearchResults(
                        query = cleanQuery,
                        animeSaltItems = mappedAnimeSalt,
                        movieBoxItems = mappedMovieBox,
                    )

                val animeSaltHasMore =
                    if (includeAnimeSalt && animeSaltResponse?.success == true && animeSaltResponse.data.isNotEmpty()) {
                        val pagination = animeSaltResponse.pagination
                        pagination != null && pagination.currentPage < pagination.totalPages && cleanPage < pagination.totalPages
                    } else {
                        false
                    }

                val movieBoxHasMore =
                    if (includeMoviebox && movieBoxResponse?.success == true && movieBoxResponse.data.isNotEmpty()) {
                        val perPage = movieBoxResponse.perPage
                        perPage > 0 && (cleanPage * perPage) < movieBoxResponse.count
                    } else {
                        false
                    }

                val errorMessage =
                    when {
                        (!includeAnimeSalt || !animeSaltSucceeded) && (!includeMoviebox || !movieBoxSucceeded) ->
                            animeSaltError?.localizedMessage
                                ?: movieBoxError?.localizedMessage
                                ?: "Unable to reach search servers."
                        !animeSaltSucceeded || !movieBoxSucceeded ->
                            "Some catalog results are temporarily unavailable."
                        else -> null
                    }

                val result =
                    SearchPageResult(
                        query = cleanQuery,
                        page = cleanPage,
                        items = mergedItems,
                        animeSaltHasMore = animeSaltHasMore,
                        movieBoxHasMore = movieBoxHasMore,
                        animeSaltSucceeded = animeSaltSucceeded,
                        movieBoxSucceeded = movieBoxSucceeded,
                        errorMessage = errorMessage,
                    )

                if (animeSaltSucceeded && movieBoxSucceeded) {
                    putCache(cacheKey, result)
                }

                result
            }
        }
    }

    suspend fun enrichDetailItem(item: AnimeItem): AnimeItem {
        val cleanId = item.id.trim()
        if (cleanId.isEmpty() && item.subjectId.isNullOrBlank() && item.detailPath.isNullOrBlank()) {
            return item
        }

        val isMovieBox =
            item.sourceProvider == SearchSource.MOVIEBOX || detectInfoSource(cleanId) == SearchSource.MOVIEBOX

        return withContext(Dispatchers.IO) {
            if (isMovieBox) {
                val isNumeric = cleanId.isNotEmpty() && cleanId.all { it.isDigit() }
                val detailPath =
                    item.detailPath?.trim()?.takeIf { it.isNotEmpty() }
                        ?: if (isNumeric) "" else cleanId
                val subjectId =
                    item.subjectId?.trim()?.takeIf { it.isNotEmpty() }
                        ?: if (isNumeric) cleanId else null
                val infoKey = "info:mb:$detailPath:${subjectId.orEmpty()}"
                val dto =
                    getCached<MovieboxInfoApiResponseDto>(infoKey, INFO_TTL_MS)
                        ?: runCatching { apiService.getMovieboxInfo(detailPath = detailPath, subjectId = subjectId) }
                            .onFailure { if (it is CancellationException) throw it }
                            .getOrNull()
                            ?.also { res -> if (res.success && res.anime != null) putCache(infoKey, res) }

                val anime = dto?.anime
                if (anime != null) {
                    val overview = anime.overview?.trim()?.takeIf { it.isNotEmpty() } ?: item.overview
                    val backdrop = anime.backdrop?.trim()?.takeIf { it.isNotEmpty() } ?: item.backdropUrl
                    val poster = anime.image?.trim()?.takeIf { it.isNotEmpty() } ?: item.posterUrl
                    val rating = anime.rating?.trim()?.takeIf { it.isNotEmpty() } ?: item.rating
                    val year = anime.year?.trim()?.takeIf { it.isNotEmpty() } ?: item.year
                    val seasonLabel =
                        item.seasonLabel ?: anime.seasons?.takeIf { it > 0 }?.let { if (it == 1) "1 Season" else "$it Seasons" }
                    val episodeLabel =
                        item.episodeLabel ?: anime.episodes?.takeIf { it > 0 }?.let { "$it Episodes" }
                    item.copy(
                        title = anime.title?.trim()?.takeIf { it.isNotEmpty() } ?: item.title,
                        posterUrl = poster,
                        backdropUrl = backdrop.ifBlank { poster },
                        overview = overview,
                        rating = rating,
                        year = year,
                        seasonLabel = seasonLabel,
                        episodeLabel = episodeLabel,
                        subjectId = anime.subjectId?.trim()?.takeIf { it.isNotEmpty() } ?: item.subjectId,
                        categories = anime.genres?.takeIf { it.isNotEmpty() } ?: item.categories,
                    )
                } else {
                    item
                }
            } else {
                val validAnilist = item.anilistId?.takeIf { it > 0 }
                val infoKey = "info:as:$cleanId:${validAnilist ?: ""}"
                val dto =
                    getCached<AnimeInfoApiResponseDto>(infoKey, INFO_TTL_MS)
                        ?: runCatching { apiService.getAnimeSaltInfo(id = cleanId, anilistId = validAnilist) }
                            .onFailure { if (it is CancellationException) throw it }
                            .getOrNull()
                            ?.also { res -> if (res.anime != null) putCache(infoKey, res) }

                val anime = dto?.anime
                if (anime != null) {
                    val resolvedTitle =
                        anime.title?.trim()?.takeIf { it.isNotEmpty() }
                            ?: anime.alternativeTitle?.trim()?.takeIf { it.isNotEmpty() }
                            ?: anime.alternativeTitles?.firstOrNull { it.isNotBlank() }?.trim()
                            ?: item.title
                    val overview = anime.overview?.trim()?.takeIf { it.isNotEmpty() } ?: item.overview
                    val backdrop = anime.backdrop?.trim()?.takeIf { it.isNotEmpty() } ?: item.backdropUrl
                    val poster = anime.image?.trim()?.takeIf { it.isNotEmpty() } ?: item.posterUrl
                    val rating = anime.rating?.trim()?.takeIf { it.isNotEmpty() } ?: item.rating
                    val year = anime.year?.trim()?.takeIf { it.isNotEmpty() } ?: item.year
                    val seasonLabel =
                        item.seasonLabel ?: anime.seasons?.takeIf { it > 0 }?.let { if (it == 1) "1 Season" else "$it Seasons" }
                    val episodeLabel =
                        item.episodeLabel ?: anime.episodes?.takeIf { it > 0 }?.let { "$it Episodes" }
                    item.copy(
                        title = resolvedTitle,
                        posterUrl = poster,
                        backdropUrl = backdrop.ifBlank { poster },
                        overview = overview,
                        rating = rating,
                        year = year,
                        seasonLabel = seasonLabel,
                        episodeLabel = episodeLabel,
                        anilistId = anime.anilistId ?: item.anilistId,
                        reanimeId = anime.reanimeId?.trim()?.takeIf { it.isNotEmpty() } ?: item.reanimeId,
                        subjectId =
                            anime.subjectId?.trim()?.takeIf { it.isNotEmpty() }
                                ?: anime.subjectIdSnake?.trim()?.takeIf { it.isNotEmpty() }
                                ?: item.subjectId,
                        categories = anime.genres?.takeIf { it.isNotEmpty() } ?: item.categories,
                    )
                } else {
                    item
                }
            }
        }
    }

    private suspend fun fetchAnimeSaltPage(
        query: String,
        page: Int,
    ): SourceFetchOutcome<AnimeSaltSearchResponseDto> {
        return try {
            val response = apiService.searchAnimeSalt(query = query, page = page)
            if (!response.success) {
                SourceFetchOutcome.Failure(IllegalStateException("AnimeSalt search returned unsuccessful response"))
            } else {
                SourceFetchOutcome.Success(response)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            SourceFetchOutcome.Failure(t)
        }
    }

    private suspend fun fetchMovieboxPage(
        query: String,
        page: Int,
    ): SourceFetchOutcome<MovieboxSearchResponseDto> {
        return try {
            val response = apiService.searchMoviebox(query = query, page = page)
            if (!response.success) {
                SourceFetchOutcome.Failure(IllegalStateException("MovieBox search returned unsuccessful response"))
            } else {
                SourceFetchOutcome.Success(response)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            SourceFetchOutcome.Failure(t)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getCached(key: String, ttlMs: Long): T? {
        val now = clockMs()
        synchronized(cacheLock) {
            val entry = memoryCache[key] ?: return null
            if (now - entry.fetchedAtMs > ttlMs) {
                memoryCache.remove(key)
                return null
            }
            return entry.value as? T
        }
    }

    private fun putCache(key: String, value: Any) {
        val now = clockMs()
        synchronized(cacheLock) {
            memoryCache[key] = CacheEntry(value = value, fetchedAtMs = now)
        }
    }

    companion object {
        const val SEARCH_TTL_MS: Long = 2L * 60L * 1000L
        const val INFO_TTL_MS: Long = 5L * 60L * 1000L
        private const val MEMORY_CACHE_MAX_ENTRIES = 250

        internal fun buildSearchCacheKey(
            query: String,
            page: Int,
            includeAnimeSalt: Boolean,
            includeMoviebox: Boolean,
        ): String {
            return "search:${query.trim().lowercase(Locale.ROOT)}:$page:$includeAnimeSalt:$includeMoviebox"
        }
    }
}

private val DIACRITICAL_MARKS_REGEX = Regex("[\\u0300-\\u036f]+")
private val NON_ALNUM_REGEX = Regex("[^a-z0-9]+")
private val MULTI_SPACE_REGEX = Regex("\\s+")
private val ANIMESALT_PREFIX_REGEX = Regex("^(anime|series|movies)[-_]", RegexOption.IGNORE_CASE)
private val UPPERCASE_LETTER_REGEX = Regex("[A-Z]")

internal fun normalizeSearchTitle(value: String): String {
    val nfd = Normalizer.normalize(value, Normalizer.Form.NFD)
    val withoutMarks = DIACRITICAL_MARKS_REGEX.replace(nfd, "")
    return withoutMarks
        .lowercase(Locale.ROOT)
        .replace(NON_ALNUM_REGEX, " ")
        .trim()
        .replace(MULTI_SPACE_REGEX, " ")
}

internal fun detectInfoSource(id: String): SearchSource {
    val clean = id.trim()
    if (ANIMESALT_PREFIX_REGEX.containsMatchIn(clean)) return SearchSource.ANIMESALT
    if (clean.isNotEmpty() && clean.all { it.isDigit() }) return SearchSource.MOVIEBOX
    val tail = clean.substringAfterLast('-', "")
    if (tail.isNotEmpty() && UPPERCASE_LETTER_REGEX.containsMatchIn(tail)) return SearchSource.MOVIEBOX
    return SearchSource.ANIMESALT
}

internal fun resolveContentKind(rawType: String?, subjectType: Int? = null): SearchContentKind {
    val clean = rawType?.trim()?.lowercase(Locale.ROOT).orEmpty()
    return when (clean) {
        "movie", "movies", "film", "films" -> SearchContentKind.MOVIE
        "series",
        "tv",
        "tv_series",
        "tv series",
        "show",
        "shows",
        "anime",
        "ova",
        "ona",
        "special",
        "tv_short",
        "music", -> SearchContentKind.SERIES
        else -> if (subjectType == 1) SearchContentKind.MOVIE else SearchContentKind.SERIES
    }
}

internal fun resolveDisplayTypeLabel(rawType: String?, kind: SearchContentKind): String {
    return when (val upper = rawType?.trim()?.uppercase(Locale.ROOT).orEmpty()) {
        "MOVIE", "MOVIES", "FILM", "FILMS" -> "MOVIE"
        "SERIES", "TV", "TV_SERIES", "TV SERIES", "SHOW", "SHOWS" -> "SERIES"
        "TV_SHORT" -> "SHORT"
        "OVA", "ONA", "SPECIAL", "MUSIC" -> upper
        else -> if (kind == SearchContentKind.MOVIE) "MOVIE" else "SERIES"
    }
}

internal fun mapAnimeSaltItem(dto: AnimeSaltSearchItemDto): SearchResultItem? {
    val cleanId = dto.id.trim()
    if (cleanId.isEmpty()) return null
    val kind = resolveContentKind(dto.type)
    val displayType = resolveDisplayTypeLabel(dto.type, kind)
    return SearchResultItem(
        uniqueKey = "as_${cleanId}_${kind.name}",
        id = cleanId,
        title = dto.title.trim().ifEmpty { "Untitled" },
        posterUrl = dto.image.orEmpty().trim(),
        contentKind = kind,
        displayType = displayType,
        source = SearchSource.ANIMESALT,
        animeSaltId = cleanId,
        anilistId = dto.anilistId,
        reanimeId = dto.reanimeId?.trim()?.takeIf { it.isNotEmpty() },
        movieBoxSubjectId = dto.subjectId?.trim()?.takeIf { it.isNotEmpty() },
        movieBoxDetailPath = null,
        link = dto.link?.trim()?.takeIf { it.isNotEmpty() },
        categories = dto.categories?.mapNotNull { cat -> cat.trim().takeIf { it.isNotEmpty() } }.orEmpty(),
    )
}

internal fun mapMovieboxItem(dto: MovieboxSearchItemDto): SearchResultItem? {
    val cleanDetailPath = dto.detailPath.trim()
    val cleanSubjectId = dto.subjectId.trim()
    if (cleanDetailPath.isEmpty() && cleanSubjectId.isEmpty()) return null
    val primaryId = cleanDetailPath.ifEmpty { cleanSubjectId }
    val kind = resolveContentKind(dto.type, dto.subjectType)
    val displayType = resolveDisplayTypeLabel(dto.type, kind)
    return SearchResultItem(
        uniqueKey = "mb_${primaryId}_${kind.name}",
        id = primaryId,
        title = dto.title.trim().ifEmpty { "Untitled" },
        posterUrl = dto.image.orEmpty().trim(),
        contentKind = kind,
        displayType = displayType,
        source = SearchSource.MOVIEBOX,
        animeSaltId = null,
        anilistId = null,
        reanimeId = null,
        movieBoxSubjectId = cleanSubjectId.takeIf { it.isNotEmpty() },
        movieBoxDetailPath = cleanDetailPath.takeIf { it.isNotEmpty() },
        link = null,
        categories = emptyList(),
    )
}

internal fun rankAndDeduplicateSearchResults(
    query: String,
    animeSaltItems: List<SearchResultItem>,
    movieBoxItems: List<SearchResultItem>,
): List<SearchResultItem> {
    val normalizedQuery = normalizeSearchTitle(query)

    fun isExact(item: SearchResultItem): Boolean {
        return normalizedQuery.isNotEmpty() && normalizeSearchTitle(item.title) == normalizedQuery
    }

    fun isPartial(item: SearchResultItem): Boolean {
        val normTitle = normalizeSearchTitle(item.title)
        return normalizedQuery.isNotEmpty() &&
                normTitle.isNotEmpty() &&
                normTitle != normalizedQuery &&
                normTitle.contains(normalizedQuery)
    }

    val exactAnimeSalt = animeSaltItems.filter(::isExact)
    val exactMovieBox = movieBoxItems.filter(::isExact)
    val partialAnimeSalt = animeSaltItems.filter { !isExact(it) && isPartial(it) }
    val partialMovieBox = movieBoxItems.filter { !isExact(it) && isPartial(it) }
    val otherAnimeSalt = animeSaltItems.filter { !isExact(it) && !isPartial(it) }
    val otherMovieBox = movieBoxItems.filter { !isExact(it) && !isPartial(it) }

    val ordered =
        buildList(animeSaltItems.size + movieBoxItems.size) {
            addAll(exactAnimeSalt)
            addAll(exactMovieBox)
            addAll(partialAnimeSalt)
            addAll(partialMovieBox)
            addAll(otherAnimeSalt)
            addAll(otherMovieBox)
        }

    return appendUniqueSearchResults(existing = emptyList(), incoming = ordered)
}

internal fun appendUniqueSearchResults(
    existing: List<SearchResultItem>,
    incoming: List<SearchResultItem>,
): List<SearchResultItem> {
    val seenIds = HashSet<String>(existing.size + incoming.size)
    val seenAnilist = HashSet<Pair<Int, SearchContentKind>>(existing.size + incoming.size)
    val seenCrossProviderTitle = HashMap<Pair<String, SearchContentKind>, SearchSource>(existing.size + incoming.size)

    fun register(item: SearchResultItem): Boolean {
        if (item.id.isBlank() || item.id in seenIds) return false

        val anilist = item.anilistId?.takeIf { it > 0 }
        if (anilist != null && (anilist to item.contentKind) in seenAnilist) {
            return false
        }

        val normTitle = normalizeSearchTitle(item.title)
        if (normTitle.isNotEmpty()) {
            val key = normTitle to item.contentKind
            val priorSource = seenCrossProviderTitle[key]
            if (priorSource != null && priorSource != item.source) {
                return false
            }
            if (priorSource == null) {
                seenCrossProviderTitle[key] = item.source
            }
        }

        seenIds.add(item.id)
        if (anilist != null) {
            seenAnilist.add(anilist to item.contentKind)
        }
        return true
    }

    existing.forEach { register(it) }

    val appended = ArrayList<SearchResultItem>(incoming.size)
    for (item in incoming) {
        if (register(item)) {
            appended.add(item)
        }
    }
    return appended
}
