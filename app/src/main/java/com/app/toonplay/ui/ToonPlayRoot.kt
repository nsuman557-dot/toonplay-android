package com.app.toonplay.ui

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.DeviceFormFactor
import com.app.toonplay.ui.components.AnimeDetailModal
import com.app.toonplay.ui.components.FeedErrorView
import com.app.toonplay.ui.components.HomeSkeletonLoader
import com.app.toonplay.ui.components.LocalHeroAutoScrollEnabled
import com.app.toonplay.ui.components.LocalIsReducedMotion
import com.app.toonplay.ui.components.LocalLiveDotAlpha
import com.app.toonplay.ui.components.ToonPlaySplashScreen
import com.app.toonplay.ui.components.rememberSystemReducedMotion
import com.app.toonplay.ui.screens.bigtablet.BigTabletHomeScreen
import com.app.toonplay.ui.screens.desktop.DesktopHomeScreen
import com.app.toonplay.ui.screens.mobile.MobileHomeScreen
import com.app.toonplay.ui.screens.search.ToonPlaySearchScreen
import com.app.toonplay.ui.screens.tablet.TabletHomeScreen
import com.app.toonplay.ui.screens.tv.TvHomeScreen
import com.app.toonplay.ui.state.FeedLoadState
import com.app.toonplay.ui.state.HomeViewModel
import com.app.toonplay.ui.state.SearchViewModel
import com.app.toonplay.ui.theme.ObsidianBg
import kotlinx.coroutines.delay

@Composable
fun ToonPlayRoot(
  viewModel: HomeViewModel,
  searchViewModel: SearchViewModel,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val isTvSystem = remember(context) { isAndroidTvDevice(context) }
  val isReducedMotion = rememberSystemReducedMotion()
  var minSplashDone by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(950)
    minSplashDone = true
  }

  LaunchedEffect(uiState.loadState) {
    searchViewModel.syncHomeFeedState(uiState.loadState)
  }

  val isSplashVisible = !minSplashDone
  val heroAutoScrollActive =
    !isSplashVisible && !uiState.isSearchActive && uiState.activeDetailItem == null
  val heroAutoScrollState = remember { mutableStateOf(heroAutoScrollActive) }
  SideEffect { heroAutoScrollState.value = heroAutoScrollActive }

  val liveDotActive =
    heroAutoScrollActive && !isReducedMotion && uiState.loadState is FeedLoadState.Success
  val liveDotAlphaState = remember { mutableFloatStateOf(1f) }
  LaunchedEffect(liveDotActive) {
    if (!liveDotActive) {
      liveDotAlphaState.floatValue = 1f
      return@LaunchedEffect
    }
    val anim = Animatable(1f)
    try {
      anim.animateTo(
        targetValue = 0.22f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 640, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
          ),
      ) {
        liveDotAlphaState.floatValue = value
      }
    } finally {
      liveDotAlphaState.floatValue = 1f
    }
  }

  val closeSearch =
    remember(viewModel, searchViewModel) {
      {
        viewModel.toggleSearch(false)
        searchViewModel.closeAndCancelSearch()
      }
    }

  CompositionLocalProvider(
    LocalIsReducedMotion provides isReducedMotion,
    LocalLiveDotAlpha provides liveDotAlphaState,
    LocalHeroAutoScrollEnabled provides heroAutoScrollState,
  ) {
    BoxWithConstraints(
      modifier =
        Modifier.fillMaxSize().background(ObsidianBg)
    ) {
      val availableWidth = this.maxWidth

      val activeFormFactor =
        remember(availableWidth, isTvSystem) {
          when {
            isTvSystem -> DeviceFormFactor.TV
            availableWidth >= 1200.dp -> DeviceFormFactor.DESKTOP
            availableWidth >= 900.dp -> DeviceFormFactor.BIG_TABLET
            availableWidth >= 600.dp -> DeviceFormFactor.TABLET
            else -> DeviceFormFactor.MOBILE
          }
        }

      BackHandler(
        enabled = !isSplashVisible && (uiState.isSearchActive || uiState.selectedTab != AnimeSectionTab.HOME),
      ) {
        when {
          uiState.isSearchActive -> closeSearch()
          uiState.selectedTab != AnimeSectionTab.HOME -> viewModel.selectTab(AnimeSectionTab.HOME)
        }
      }

      AnimatedContent(
        targetState = if (isSplashVisible) null else uiState.loadState,
        transitionSpec = {
          (fadeIn(tween(durationMillis = 480)) + scaleIn(initialScale = 0.97f, animationSpec = tween(480)))
            .togetherWith(fadeOut(tween(durationMillis = 340)))
        },
        contentKey = { state ->
          when (state) {
            null -> "splash"
            is FeedLoadState.Loading -> "skeleton"
            is FeedLoadState.Error -> "error"
            is FeedLoadState.Success -> "home"
          }
        },
        label = "splash_to_home_transition",
      ) { targetState ->
        when (targetState) {
          null -> ToonPlaySplashScreen()
          is FeedLoadState.Loading -> HomeSkeletonLoader(formFactor = activeFormFactor)
          is FeedLoadState.Error ->
            FeedErrorView(
              message = targetState.message,
              onRetry = { viewModel.loadFeed(isPullRefresh = false) },
            )
          is FeedLoadState.Success -> {
            val feed = targetState.feed
            when (activeFormFactor) {
              DeviceFormFactor.MOBILE ->
                MobileHomeScreen(
                  feed = feed,
                  controls = uiState.feedControls,
                  initialHeroIndex = viewModel.currentHeroIndex,
                  onSelectTab = viewModel::selectTab,
                  onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                  onToggleSectionGrid = viewModel::setSectionGridMode,
                  onHeroIndexChanged = viewModel::setActiveHeroIndex,
                  onItemClick = viewModel::openDetail,
                  onToggleSave = viewModel::toggleSaved,
                  onToggleSearch = viewModel::toggleSearch,
                )
              DeviceFormFactor.TABLET ->
                TabletHomeScreen(
                  feed = feed,
                  controls = uiState.feedControls,
                  initialHeroIndex = viewModel.currentHeroIndex,
                  onSelectTab = viewModel::selectTab,
                  onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                  onToggleSectionGrid = viewModel::setSectionGridMode,
                  onHeroIndexChanged = viewModel::setActiveHeroIndex,
                  onItemClick = viewModel::openDetail,
                  onToggleSave = viewModel::toggleSaved,
                  onToggleSearch = viewModel::toggleSearch,
                )
              DeviceFormFactor.BIG_TABLET ->
                BigTabletHomeScreen(
                  feed = feed,
                  controls = uiState.feedControls,
                  initialHeroIndex = viewModel.currentHeroIndex,
                  onSelectTab = viewModel::selectTab,
                  onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                  onToggleSectionGrid = viewModel::setSectionGridMode,
                  onHeroIndexChanged = viewModel::setActiveHeroIndex,
                  onExpandDetail = viewModel::openDetail,
                  onToggleSave = viewModel::toggleSaved,
                  onToggleSearch = viewModel::toggleSearch,
                )
              DeviceFormFactor.DESKTOP ->
                DesktopHomeScreen(
                  feed = feed,
                  controls = uiState.feedControls,
                  focusedPreviewFlow = viewModel.focusedPreview,
                  initialHeroIndex = viewModel.currentHeroIndex,
                  onSelectTab = viewModel::selectTab,
                  onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                  onToggleSectionGrid = viewModel::setSectionGridMode,
                  onHeroIndexChanged = viewModel::setActiveHeroIndex,
                  onFocusItem = viewModel::setFocusedPreview,
                  onExpandDetail = viewModel::openDetail,
                  onToggleSave = viewModel::toggleSaved,
                  onToggleSearch = viewModel::toggleSearch,
                )
              DeviceFormFactor.TV ->
                TvHomeScreen(
                  feed = feed,
                  controls = uiState.feedControls,
                  focusedPreviewFlow = viewModel.focusedPreview,
                  initialHeroIndex = viewModel.currentHeroIndex,
                  onSelectTab = viewModel::selectTab,
                  onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                  onToggleSectionGrid = viewModel::setSectionGridMode,
                  onHeroIndexChanged = viewModel::setActiveHeroIndex,
                  onFocusItem = viewModel::setFocusedPreview,
                  onItemClick = viewModel::openDetail,
                  onToggleSave = viewModel::toggleSaved,
                  onToggleSearch = viewModel::toggleSearch,
                )
            }
          }
        }
      }

      AnimatedVisibility(
        visible = !isSplashVisible && uiState.isSearchActive,
        enter =
          if (isReducedMotion) fadeIn(tween(0))
          else fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 20 },
        exit = if (isReducedMotion) fadeOut(tween(0)) else fadeOut(tween(150)),
        label = "search_screen_transition",
      ) {
        val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
        ToonPlaySearchScreen(
          formFactor = activeFormFactor,
          searchState = searchState,
          savedIds = uiState.savedIds,
          onQueryChange = searchViewModel::onQueryChange,
          onFilterChange = searchViewModel::onFilterChange,
          onItemClick = viewModel::openDetail,
          onToggleSave = viewModel::toggleSaved,
          onLoadMore = searchViewModel::loadNextPage,
          onRetrySearch = searchViewModel::retryInitialSearch,
          onRetryTrending = { viewModel.loadFeed(isPullRefresh = false) },
          onDismissPartialWarning = searchViewModel::dismissPartialWarning,
          onClose = closeSearch,
        )
      }

      val loadedFeed = (uiState.loadState as? FeedLoadState.Success)?.feed
      if (!isSplashVisible) {
        uiState.activeDetailItem?.let { detailItem ->
          val related =
            remember(detailItem.id, loadedFeed) {
              loadedFeed?.allUnique.orEmpty().filter { it.id != detailItem.id }.take(12)
            }
          AnimeDetailModal(
            item = detailItem,
            isSaved = detailItem.id in uiState.savedIds,
            relatedItems = related,
            onToggleSave = { viewModel.toggleSaved(detailItem) },
            onSelectRelated = viewModel::openDetail,
            onDismiss = viewModel::closeDetail,
          )
        }
      }
    }
  }
}

private fun isAndroidTvDevice(context: Context): Boolean {
  val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
  return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
}