package com.app.toonplay.ui.state

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.SearchContentKind
import com.app.toonplay.data.model.SearchFilter
import com.app.toonplay.data.model.SearchResultItem
import com.app.toonplay.data.model.withoutYearForSearch
import com.app.toonplay.data.repository.SearchRepository
import com.app.toonplay.data.repository.appendUniqueSearchResults
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SearchScreenUiState(
    val query: String = "",
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val isQueryEmpty: Boolean = true,
    val isTrendingLoading: Boolean = false,
    val trendingErrorMessage: String? = null,
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val errorMessage: String? = null,
    val partialFailureWarning: String? = null,
    val paginationError: String? = null,
    val displayedItems: List<AnimeItem> = emptyList(),
    val totalLoadedResultsCount: Int = 0,
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val rawQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(SearchFilter.ALL)
    private val homeFeedState = MutableStateFlow<FeedLoadState>(FeedLoadState.Loading)

    private val rawApiResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    private val isSearching = MutableStateFlow(false)
    private val isLoadingMore = MutableStateFlow(false)
    private val hasMore = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val partialFailureWarning = MutableStateFlow<String?>(null)
    private val paginationError = MutableStateFlow<String?>(null)

    private var activeSearchJob: Job? = null
    private var activePaginationJob: Job? = null
    private var searchGeneration: Long = 0L
    private var currentPage: Int = 1
    private var animeSaltHasMore: Boolean = false
    private var movieBoxHasMore: Boolean = false
    private val inFlightPages = HashSet<String>()

    private val searchFlags =
        combine(
            isSearching,
            isLoadingMore,
            hasMore,
            errorMessage,
            partialFailureWarning,
            paginationError,
        ) { arr ->
            SearchFlagsTuple(
                isSearching = arr[0] as Boolean,
                isLoadingMore = arr[1] as Boolean,
                hasMore = arr[2] as Boolean,
                errorMessage = arr[3] as String?,
                partialFailureWarning = arr[4] as String?,
                paginationError = arr[5] as String?,
            )
        }

    val uiState: StateFlow<SearchScreenUiState> =
        combine(
            rawQuery,
            selectedFilter,
            homeFeedState,
            rawApiResults,
            searchFlags,
        ) { query, filter, homeState, apiResults, flags ->
            val trimmed = query.trim()
            val isQueryEmpty = trimmed.isEmpty()

            if (isQueryEmpty) {
                val feed = (homeState as? FeedLoadState.Success)?.feed
                val trendingItems =
                    if (feed != null) {
                        buildDefaultTrendingItems(
                            popularMovies = feed.popularMovies,
                            popularSeries = feed.popularSeries,
                            filter = filter,
                        )
                    } else {
                        emptyList()
                    }
                val totalTrending =
                    when {
                        feed == null -> 0
                        filter == SearchFilter.ALL -> trendingItems.size
                        else ->
                            buildDefaultTrendingItems(
                                popularMovies = feed.popularMovies,
                                popularSeries = feed.popularSeries,
                                filter = SearchFilter.ALL,
                            ).size
                    }
                SearchScreenUiState(
                    query = query,
                    selectedFilter = filter,
                    isQueryEmpty = true,
                    isTrendingLoading = homeState is FeedLoadState.Loading,
                    trendingErrorMessage = (homeState as? FeedLoadState.Error)?.message,
                    isSearching = false,
                    isLoadingMore = false,
                    hasMore = false,
                    errorMessage = null,
                    partialFailureWarning = null,
                    paginationError = null,
                    displayedItems = trendingItems,
                    totalLoadedResultsCount = totalTrending,
                )
            } else {
                val filteredResults =
                    apiResults
                        .asSequence()
                        .filter { item ->
                            when (filter) {
                                SearchFilter.ALL -> true
                                SearchFilter.MOVIES -> item.contentKind == SearchContentKind.MOVIE
                                SearchFilter.SERIES -> item.contentKind == SearchContentKind.SERIES
                            }
                        }
                        .map { it.toAnimeItem() }
                        .toList()

                SearchScreenUiState(
                    query = query,
                    selectedFilter = filter,
                    isQueryEmpty = false,
                    isTrendingLoading = false,
                    trendingErrorMessage = null,
                    isSearching = flags.isSearching,
                    isLoadingMore = flags.isLoadingMore,
                    hasMore = flags.hasMore,
                    errorMessage = flags.errorMessage,
                    partialFailureWarning = flags.partialFailureWarning,
                    paginationError = flags.paginationError,
                    displayedItems = filteredResults,
                    totalLoadedResultsCount = apiResults.size,
                )
            }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchScreenUiState(),
            )

    fun syncHomeFeedState(state: FeedLoadState) {
        homeFeedState.value = state
    }

    fun onQueryChange(newQuery: String) {
        val previousTrimmed = rawQuery.value.trim()
        rawQuery.value = newQuery
        val newTrimmed = newQuery.trim()
        if (newTrimmed == previousTrimmed) {
            return
        }

        cancelAndResetActiveSearch()

        if (newTrimmed.isEmpty()) {
            isSearching.value = false
            return
        }

        isSearching.value = true
        val generation = searchGeneration
        activeSearchJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                if (generation != searchGeneration) return@launch
                executeInitialSearch(cleanQuery = newTrimmed, generation = generation)
            }
    }

    fun onFilterChange(filter: SearchFilter) {
        selectedFilter.value = filter
    }

    fun retryInitialSearch() {
        val cleanQuery = rawQuery.value.trim()
        if (cleanQuery.isEmpty()) return

        cancelAndResetActiveSearch()
        isSearching.value = true
        val generation = searchGeneration
        activeSearchJob =
            viewModelScope.launch {
                executeInitialSearch(cleanQuery = cleanQuery, generation = generation)
            }
    }

    fun loadNextPage() {
        val cleanQuery = rawQuery.value.trim()
        if (
            cleanQuery.isEmpty() ||
            isSearching.value ||
            isLoadingMore.value ||
            (!animeSaltHasMore && !movieBoxHasMore)
        ) {
            return
        }

        val nextPage = currentPage + 1
        val requestKey = "$cleanQuery:$nextPage"
        if (!inFlightPages.add(requestKey)) {
            return
        }

        val generation = searchGeneration
        val fetchAnimeSalt = animeSaltHasMore
        val fetchMovieBox = movieBoxHasMore

        isLoadingMore.value = true
        paginationError.value = null

        activePaginationJob =
            viewModelScope.launch {
                try {
                    val pageResult =
                        searchRepository.searchCatalog(
                            query = cleanQuery,
                            page = nextPage,
                            includeAnimeSalt = fetchAnimeSalt,
                            includeMoviebox = fetchMovieBox,
                        )
                    if (generation != searchGeneration) return@launch

                    if (!pageResult.hasAnySuccess) {
                        paginationError.value = pageResult.errorMessage ?: "Unable to load more results."
                    } else {
                        val newlyAdded =
                            appendUniqueSearchResults(
                                existing = rawApiResults.value,
                                incoming = pageResult.items,
                            )
                        if (newlyAdded.isNotEmpty()) {
                            rawApiResults.value = rawApiResults.value + newlyAdded
                        }
                        currentPage = nextPage
                        animeSaltHasMore = pageResult.animeSaltHasMore
                        movieBoxHasMore = pageResult.movieBoxHasMore
                        hasMore.value = animeSaltHasMore || movieBoxHasMore
                        if (pageResult.hasPartialFailure) {
                            partialFailureWarning.value = pageResult.errorMessage
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    if (generation == searchGeneration) {
                        paginationError.value = t.localizedMessage ?: "Unable to load more results."
                    }
                } finally {
                    inFlightPages.remove(requestKey)
                    if (generation == searchGeneration) {
                        isLoadingMore.value = false
                    }
                }
            }
    }

    fun closeAndCancelSearch() {
        rawQuery.value = ""
        selectedFilter.value = SearchFilter.ALL
        cancelAndResetActiveSearch()
        isSearching.value = false
    }

    fun dismissPartialWarning() {
        partialFailureWarning.value = null
    }

    private suspend fun executeInitialSearch(
        cleanQuery: String,
        generation: Long,
    ) {
        val requestKey = "$cleanQuery:1"
        inFlightPages.add(requestKey)
        try {
            val pageResult =
                searchRepository.searchCatalog(
                    query = cleanQuery,
                    page = 1,
                    includeAnimeSalt = true,
                    includeMoviebox = true,
                )
            if (generation != searchGeneration) return

            if (!pageResult.hasAnySuccess) {
                rawApiResults.value = emptyList()
                currentPage = 1
                animeSaltHasMore = false
                movieBoxHasMore = false
                hasMore.value = false
                partialFailureWarning.value = null
                errorMessage.value = pageResult.errorMessage ?: "Unable to search right now."
            } else {
                rawApiResults.value = pageResult.items
                currentPage = 1
                animeSaltHasMore = pageResult.animeSaltHasMore
                movieBoxHasMore = pageResult.movieBoxHasMore
                hasMore.value = pageResult.hasMore
                errorMessage.value = null
                partialFailureWarning.value = if (pageResult.hasPartialFailure) pageResult.errorMessage else null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            if (generation == searchGeneration) {
                rawApiResults.value = emptyList()
                hasMore.value = false
                errorMessage.value = t.localizedMessage ?: "Unable to search right now."
            }
        } finally {
            inFlightPages.remove(requestKey)
            if (generation == searchGeneration) {
                isSearching.value = false
            }
        }
    }

    private fun cancelAndResetActiveSearch() {
        searchGeneration += 1
        activeSearchJob?.cancel()
        activeSearchJob = null
        activePaginationJob?.cancel()
        activePaginationJob = null
        inFlightPages.clear()
        rawApiResults.value = emptyList()
        currentPage = 1
        animeSaltHasMore = false
        movieBoxHasMore = false
        hasMore.value = false
        isLoadingMore.value = false
        errorMessage.value = null
        partialFailureWarning.value = null
        paginationError.value = null
    }

    private data class SearchFlagsTuple(
        val isSearching: Boolean,
        val isLoadingMore: Boolean,
        val hasMore: Boolean,
        val errorMessage: String?,
        val partialFailureWarning: String?,
        val paginationError: String?,
    )

    class Factory(
        private val searchRepository: SearchRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(searchRepository) as T
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 350L
    }
}

internal fun buildDefaultTrendingItems(
    popularMovies: List<AnimeItem>,
    popularSeries: List<AnimeItem>,
    filter: SearchFilter,
): List<AnimeItem> {
    val sanitizedMovies = popularMovies.map { it.withoutYearForSearch() }
    val sanitizedSeries = popularSeries.map { it.withoutYearForSearch() }

    return when (filter) {
        SearchFilter.ALL -> {
            val maxCount = maxOf(sanitizedMovies.size, sanitizedSeries.size)
            buildList(sanitizedMovies.size + sanitizedSeries.size) {
                for (i in 0 until maxCount) {
                    if (i < sanitizedMovies.size) add(sanitizedMovies[i])
                    if (i < sanitizedSeries.size) add(sanitizedSeries[i])
                }
            }.distinctBy { it.id.ifBlank { it.uid } }
        }
        SearchFilter.MOVIES -> sanitizedMovies.distinctBy { it.id.ifBlank { it.uid } }
        SearchFilter.SERIES -> sanitizedSeries.distinctBy { it.id.ifBlank { it.uid } }
    }
}
