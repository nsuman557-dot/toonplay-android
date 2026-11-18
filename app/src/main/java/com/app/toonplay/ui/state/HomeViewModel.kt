package com.app.toonplay.ui.state

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.HomeFeed
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.data.model.LayoutOverride
import com.app.toonplay.data.repository.AnimeHomeRepository
import com.app.toonplay.data.repository.SearchRepository
import com.app.toonplay.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
sealed interface FeedLoadState {
  data object Loading : FeedLoadState

  data class Success(val feed: HomeFeed) : FeedLoadState

  data class Error(val message: String) : FeedLoadState
}

@Immutable
data class HomeFeedControlsState(
  val selectedTab: AnimeSectionTab = AnimeSectionTab.HOME,
  val latestLanguageFilter: LatestLanguageFilter = LatestLanguageFilter.HIN_TAM_TEL,
  val gridModeTabs: Set<AnimeSectionTab> = emptySet(),
  val savedItems: List<AnimeItem> = emptyList(),
  val savedIds: Set<String> = emptySet(),
)

@Immutable
data class HomeScreenUiState(
  val loadState: FeedLoadState = FeedLoadState.Loading,
  val feedControls: HomeFeedControlsState = HomeFeedControlsState(),
  val isSearchActive: Boolean = false,
  val activeDetailItem: AnimeItem? = null,
  val layoutOverride: LayoutOverride = LayoutOverride.AUTO,
  val isRefreshing: Boolean = false,
  val isLayoutPickerOpen: Boolean = false,
) {
  val selectedTab: AnimeSectionTab get() = feedControls.selectedTab
  val latestLanguageFilter: LatestLanguageFilter get() = feedControls.latestLanguageFilter
  val gridModeTabs: Set<AnimeSectionTab> get() = feedControls.gridModeTabs
  val savedItems: List<AnimeItem> get() = feedControls.savedItems
  val savedIds: Set<String> get() = feedControls.savedIds
}

class HomeViewModel(
  private val repository: AnimeHomeRepository,
  private val preferencesRepository: UserPreferencesRepository,
  private val searchRepository: SearchRepository? = null,
) : ViewModel() {

  private val loadState = MutableStateFlow<FeedLoadState>(FeedLoadState.Loading)
  private val selectedTab = MutableStateFlow(AnimeSectionTab.HOME)
  private val latestLanguageFilter = MutableStateFlow(LatestLanguageFilter.HIN_TAM_TEL)
  private val gridModeTabs = MutableStateFlow<Set<AnimeSectionTab>>(emptySet())
  private val isSearchActive = MutableStateFlow(false)
  private val activeDetailItem = MutableStateFlow<AnimeItem?>(null)
  private val _focusedPreviewItem = MutableStateFlow<AnimeItem?>(null)
  val focusedPreview: StateFlow<AnimeItem?> = _focusedPreviewItem.asStateFlow()
  private val isRefreshing = MutableStateFlow(false)
  private val isLayoutPickerOpen = MutableStateFlow(false)
  private val extraSavedCatalog = MutableStateFlow<Map<String, AnimeItem>>(emptyMap())

  @Volatile
  var currentHeroIndex: Int = 0
    private set

  @Volatile
  private var latestSavedIdsSnapshot: Set<String> = emptySet()

  private var activeDetailJob: Job? = null

  private val savedCatalogState =
    combine(
      loadState,
      extraSavedCatalog,
      preferencesRepository.savedIds,
    ) { feedState, extraCatalog, savedIds ->
      latestSavedIdsSnapshot = savedIds
      val feed = (feedState as? FeedLoadState.Success)?.feed
      val feedSaved = feed?.allUnique?.filter { it.id in savedIds }.orEmpty()
      val feedSavedIds = feedSaved.mapTo(HashSet()) { it.id }
      val extraSaved =
        savedIds
          .asSequence()
          .filter { it !in feedSavedIds }
          .mapNotNull { extraCatalog[it] }
          .toList()
      (feedSaved + extraSaved) to savedIds
    }
      .distinctUntilChanged()

  private val feedControlsFlow =
    combine(
      selectedTab,
      latestLanguageFilter,
      gridModeTabs,
      savedCatalogState,
    ) { tab, langFilter, gridTabs, savedPair ->
      HomeFeedControlsState(
        selectedTab = tab,
        latestLanguageFilter = langFilter,
        gridModeTabs = gridTabs,
        savedItems = savedPair.first,
        savedIds = savedPair.second,
      )
    }
      .distinctUntilChanged()

  private val overlayControls =
    combine(
      isSearchActive,
      activeDetailItem,
      isRefreshing,
      isLayoutPickerOpen,
    ) { searchOpen, detailItem, refreshing, pickerOpen ->
      OverlayTuple(
        isSearchActive = searchOpen,
        activeDetailItem = detailItem,
        isRefreshing = refreshing,
        isLayoutPickerOpen = pickerOpen,
      )
    }

  val uiState: StateFlow<HomeScreenUiState> =
    combine(
      loadState,
      feedControlsFlow,
      overlayControls,
      preferencesRepository.layoutOverride,
    ) { feedState, feedControls, overlay, layoutOverride ->
      HomeScreenUiState(
        loadState = feedState,
        feedControls = feedControls,
        isSearchActive = overlay.isSearchActive,
        activeDetailItem = overlay.activeDetailItem,
        layoutOverride = layoutOverride,
        isRefreshing = overlay.isRefreshing,
        isLayoutPickerOpen = overlay.isLayoutPickerOpen,
      )
    }
      .flowOn(Dispatchers.Default)
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenUiState(),
      )

  init {
    loadFeed(isPullRefresh = false)
  }

  fun loadFeed(isPullRefresh: Boolean = false) {
    viewModelScope.launch {
      if (isPullRefresh) {
        isRefreshing.value = true
      } else if (loadState.value !is FeedLoadState.Success) {
        loadState.value = FeedLoadState.Loading
      }
      runCatching { repository.fetchHomeFeed() }
        .onSuccess { feed ->
          loadState.value = FeedLoadState.Success(feed)
          if (_focusedPreviewItem.value == null) {
            _focusedPreviewItem.value = feed.hero.firstOrNull()
          }
        }
        .onFailure { err ->
          if (loadState.value !is FeedLoadState.Success) {
            loadState.value =
              FeedLoadState.Error(err.localizedMessage ?: "Failed to load ToonPlay feed")
          }
        }
      isRefreshing.value = false
    }
  }

  fun selectTab(tab: AnimeSectionTab) {
    selectedTab.value = tab
  }

  fun setLatestLanguageFilter(filter: LatestLanguageFilter) {
    latestLanguageFilter.value = filter
  }

  fun setSectionGridMode(tab: AnimeSectionTab, gridMode: Boolean) {
    val current = gridModeTabs.value.toMutableSet()
    val changed = if (gridMode) current.add(tab) else current.remove(tab)
    if (changed) {
      gridModeTabs.value = current
    }
  }

  fun toggleSearch(open: Boolean) {
    isSearchActive.value = open
  }

  fun setActiveHeroIndex(index: Int) {
    currentHeroIndex = index
  }

  fun openDetail(item: AnimeItem) {
    activeDetailJob?.cancel()
    activeDetailItem.value = item
    _focusedPreviewItem.value = item

    val enricher = searchRepository ?: return
    val needsDetailEnrichment = item.overview.isBlank() || item.sourceProvider != null
    if (!needsDetailEnrichment) return

    activeDetailJob =
      viewModelScope.launch {
        try {
          val enriched = enricher.enrichDetailItem(item)
          if (activeDetailItem.value?.uid == item.uid) {
            activeDetailItem.value = enriched
            if (_focusedPreviewItem.value?.uid == item.uid) {
              _focusedPreviewItem.value = enriched
            }
            if (enriched.id.isNotBlank() && enriched.id in latestSavedIdsSnapshot) {
              extraSavedCatalog.value = extraSavedCatalog.value + (enriched.id to enriched)
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (_: Throwable) {
          // Keep instant fallback metadata when optional detail enrichment fails.
        }
      }
  }

  fun closeDetail() {
    activeDetailJob?.cancel()
    activeDetailJob = null
    activeDetailItem.value = null
  }

  fun setFocusedPreview(item: AnimeItem) {
    if (_focusedPreviewItem.value?.uid != item.uid || _focusedPreviewItem.value != item) {
      _focusedPreviewItem.value = item
    }
  }

  fun toggleSaved(item: AnimeItem) {
    if (item.id.isNotBlank() && extraSavedCatalog.value[item.id] != item) {
      extraSavedCatalog.value = extraSavedCatalog.value + (item.id to item)
    }
    viewModelScope.launch { preferencesRepository.toggleSaved(item.id) }
  }

  fun setLayoutPickerOpen(open: Boolean) {
    isLayoutPickerOpen.value = open
  }

  fun setLayoutOverride(override: LayoutOverride) {
    viewModelScope.launch {
      preferencesRepository.setLayoutOverride(override)
      isLayoutPickerOpen.value = false
    }
  }

  private data class OverlayTuple(
    val isSearchActive: Boolean,
    val activeDetailItem: AnimeItem?,
    val isRefreshing: Boolean,
    val isLayoutPickerOpen: Boolean,
  )

  class Factory(
    private val repository: AnimeHomeRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val searchRepository: SearchRepository? = null,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return HomeViewModel(repository, preferencesRepository, searchRepository) as T
    }
  }
}