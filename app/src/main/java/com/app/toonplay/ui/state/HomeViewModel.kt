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
import com.app.toonplay.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
data class HomeScreenUiState(
  val loadState: FeedLoadState = FeedLoadState.Loading,
  val selectedTab: AnimeSectionTab = AnimeSectionTab.HOME,
  val latestLanguageFilter: LatestLanguageFilter = LatestLanguageFilter.HIN_TAM_TEL,
  val gridModeTabs: Set<AnimeSectionTab> = emptySet(),
  val searchQuery: String = "",
  val isSearchActive: Boolean = false,
  val searchResults: List<AnimeItem> = emptyList(),
  val savedItems: List<AnimeItem> = emptyList(),
  val savedIds: Set<String> = emptySet(),
  val activeHeroIndex: Int = 0,
  val activeDetailItem: AnimeItem? = null,
  val focusedPreviewItem: AnimeItem? = null,
  val layoutOverride: LayoutOverride = LayoutOverride.AUTO,
  val isRefreshing: Boolean = false,
  val isLayoutPickerOpen: Boolean = false,
)

class HomeViewModel(
  private val repository: AnimeHomeRepository,
  private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

  private val loadState = MutableStateFlow<FeedLoadState>(FeedLoadState.Loading)
  private val selectedTab = MutableStateFlow(AnimeSectionTab.HOME)
  private val latestLanguageFilter = MutableStateFlow(LatestLanguageFilter.HIN_TAM_TEL)
  private val gridModeTabs = MutableStateFlow<Set<AnimeSectionTab>>(emptySet())
  private val searchQuery = MutableStateFlow("")
  private val isSearchActive = MutableStateFlow(false)
  private val activeHeroIndex = MutableStateFlow(0)
  private val activeDetailItem = MutableStateFlow<AnimeItem?>(null)
  private val focusedPreviewItem = MutableStateFlow<AnimeItem?>(null)
  private val isRefreshing = MutableStateFlow(false)
  private val isLayoutPickerOpen = MutableStateFlow(false)

  private val baseControls =
    combine(
      loadState,
      selectedTab,
      latestLanguageFilter,
      gridModeTabs,
      searchQuery,
    ) { feedState, tab, langFilter, gridTabs, query ->
      BaseTuple(feedState, tab, langFilter, gridTabs, query)
    }

  private val overlayControls =
    combine(
      isSearchActive,
      activeHeroIndex,
      activeDetailItem,
      focusedPreviewItem,
      isRefreshing,
      isLayoutPickerOpen,
    ) { arr ->
      OverlayTuple(
        isSearchActive = arr[0] as Boolean,
        activeHeroIndex = arr[1] as Int,
        activeDetailItem = arr[2] as AnimeItem?,
        focusedPreviewItem = arr[3] as AnimeItem?,
        isRefreshing = arr[4] as Boolean,
        isLayoutPickerOpen = arr[5] as Boolean,
      )
    }

  val uiState: StateFlow<HomeScreenUiState> =
    combine(
      baseControls,
      overlayControls,
      preferencesRepository.savedIds,
      preferencesRepository.layoutOverride,
    ) { base, overlay, savedIds, layoutOverride ->
      val feed = (base.loadState as? FeedLoadState.Success)?.feed
      val q = base.searchQuery.trim().lowercase()
      val searchResults =
        if (feed != null && q.isNotEmpty()) {
          feed.allUnique.filter { item ->
            item.title.lowercase().contains(q) ||
              item.typeLabel.lowercase().contains(q) ||
              (item.seasonLabel?.lowercase()?.contains(q) == true)
          }
        } else {
          emptyList()
        }
      val savedItems =
        if (feed != null && savedIds.isNotEmpty()) {
          feed.allUnique.filter { it.id in savedIds }
        } else {
          emptyList()
        }
      val defaultPreview = overlay.focusedPreviewItem ?: feed?.hero?.firstOrNull()

      HomeScreenUiState(
        loadState = base.loadState,
        selectedTab = base.selectedTab,
        latestLanguageFilter = base.latestLanguageFilter,
        gridModeTabs = base.gridModeTabs,
        searchQuery = base.searchQuery,
        isSearchActive = overlay.isSearchActive,
        searchResults = searchResults,
        savedItems = savedItems,
        savedIds = savedIds,
        activeHeroIndex = overlay.activeHeroIndex,
        activeDetailItem = overlay.activeDetailItem,
        focusedPreviewItem = defaultPreview,
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
          if (focusedPreviewItem.value == null) {
            focusedPreviewItem.value = feed.hero.firstOrNull()
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
    if (gridMode) current.add(tab) else current.remove(tab)
    gridModeTabs.value = current
  }

  fun toggleSearch(open: Boolean) {
    isSearchActive.value = open
    if (!open) searchQuery.value = ""
  }

  fun updateSearchQuery(query: String) {
    searchQuery.value = query
  }

  fun setActiveHeroIndex(index: Int) {
    activeHeroIndex.value = index
  }

  fun openDetail(item: AnimeItem) {
    activeDetailItem.value = item
    focusedPreviewItem.value = item
  }

  fun closeDetail() {
    activeDetailItem.value = null
  }

  fun setFocusedPreview(item: AnimeItem) {
    focusedPreviewItem.value = item
  }

  fun toggleSaved(item: AnimeItem) {
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

  private data class BaseTuple(
    val loadState: FeedLoadState,
    val selectedTab: AnimeSectionTab,
    val latestLanguageFilter: LatestLanguageFilter,
    val gridModeTabs: Set<AnimeSectionTab>,
    val searchQuery: String,
  )

  private data class OverlayTuple(
    val isSearchActive: Boolean,
    val activeHeroIndex: Int,
    val activeDetailItem: AnimeItem?,
    val focusedPreviewItem: AnimeItem?,
    val isRefreshing: Boolean,
    val isLayoutPickerOpen: Boolean,
  )

  class Factory(
    private val repository: AnimeHomeRepository,
    private val preferencesRepository: UserPreferencesRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return HomeViewModel(repository, preferencesRepository) as T
    }
  }
}
