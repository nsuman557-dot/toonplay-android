package com.app.toonplay.ui

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.DeviceFormFactor
import com.app.toonplay.ui.components.AnimeDetailModal
import com.app.toonplay.ui.components.FeedErrorView
import com.app.toonplay.ui.components.HomeSkeletonLoader
import com.app.toonplay.ui.components.ToonPlaySplashScreen
import com.app.toonplay.ui.screens.bigtablet.BigTabletHomeScreen
import com.app.toonplay.ui.screens.desktop.DesktopHomeScreen
import com.app.toonplay.ui.screens.mobile.MobileHomeScreen
import com.app.toonplay.ui.screens.tablet.TabletHomeScreen
import com.app.toonplay.ui.screens.tv.TvHomeScreen
import com.app.toonplay.ui.state.FeedLoadState
import com.app.toonplay.ui.state.HomeViewModel
import com.app.toonplay.ui.theme.ObsidianBg
import kotlinx.coroutines.delay

@Composable
fun ToonPlayRoot(viewModel: HomeViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val isTvSystem = remember(context) { isAndroidTvDevice(context) }
  var minSplashDone by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(950)
    minSplashDone = true
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBg)
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

    val isSplashVisible = !minSplashDone
    BackHandler(enabled = !isSplashVisible && (uiState.isSearchActive || uiState.selectedTab != AnimeSectionTab.HOME)) {
      when {
        uiState.isSearchActive -> viewModel.toggleSearch(false)
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
                uiState = uiState,
                onSelectTab = viewModel::selectTab,
                onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                onToggleSectionGrid = viewModel::setSectionGridMode,
                onHeroIndexChanged = viewModel::setActiveHeroIndex,
                onItemClick = viewModel::openDetail,
                onToggleSave = viewModel::toggleSaved,
                onToggleSearch = viewModel::toggleSearch,
                onSearchQueryChange = viewModel::updateSearchQuery,
              )
            DeviceFormFactor.TABLET ->
              TabletHomeScreen(
                feed = feed,
                uiState = uiState,
                onSelectTab = viewModel::selectTab,
                onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                onToggleSectionGrid = viewModel::setSectionGridMode,
                onHeroIndexChanged = viewModel::setActiveHeroIndex,
                onItemClick = viewModel::openDetail,
                onToggleSave = viewModel::toggleSaved,
                onToggleSearch = viewModel::toggleSearch,
                onSearchQueryChange = viewModel::updateSearchQuery,
              )
            DeviceFormFactor.BIG_TABLET ->
              BigTabletHomeScreen(
                feed = feed,
                uiState = uiState,
                onSelectTab = viewModel::selectTab,
                onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                onToggleSectionGrid = viewModel::setSectionGridMode,
                onHeroIndexChanged = viewModel::setActiveHeroIndex,
                onExpandDetail = viewModel::openDetail,
                onToggleSave = viewModel::toggleSaved,
                onSearchQueryChange = viewModel::updateSearchQuery,
              )
            DeviceFormFactor.DESKTOP ->
              DesktopHomeScreen(
                feed = feed,
                uiState = uiState,
                onSelectTab = viewModel::selectTab,
                onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                onToggleSectionGrid = viewModel::setSectionGridMode,
                onHeroIndexChanged = viewModel::setActiveHeroIndex,
                onFocusItem = viewModel::setFocusedPreview,
                onExpandDetail = viewModel::openDetail,
                onToggleSave = viewModel::toggleSaved,
                onSearchQueryChange = viewModel::updateSearchQuery,
              )
            DeviceFormFactor.TV ->
              TvHomeScreen(
                feed = feed,
                uiState = uiState,
                onSelectTab = viewModel::selectTab,
                onSelectLanguageFilter = viewModel::setLatestLanguageFilter,
                onToggleSectionGrid = viewModel::setSectionGridMode,
                onHeroIndexChanged = viewModel::setActiveHeroIndex,
                onFocusItem = viewModel::setFocusedPreview,
                onItemClick = viewModel::openDetail,
                onToggleSave = viewModel::toggleSaved,
                onToggleSearch = viewModel::toggleSearch,
                onSearchQueryChange = viewModel::updateSearchQuery,
              )
          }

          uiState.activeDetailItem?.let { detailItem ->
            val related =
              remember(detailItem.id, feed.allUnique) {
                feed.allUnique.filter { it.id != detailItem.id }.take(12)
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
}

private fun isAndroidTvDevice(context: Context): Boolean {
  val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
  return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
}