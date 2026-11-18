package com.app.toonplay.ui.screens.mobile

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.DeviceFormFactor
import com.app.toonplay.data.model.HomeFeed
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.ui.components.AmbientHeroBackdrop
import com.app.toonplay.ui.components.AnimePosterCard
import com.app.toonplay.ui.components.AnimePosterGrid
import com.app.toonplay.ui.components.CarouselDotIndicator
import com.app.toonplay.ui.components.FannedHeroCarousel
import com.app.toonplay.ui.components.GlassCircleIconButton
import com.app.toonplay.ui.components.LanguageCardsRail
import com.app.toonplay.ui.components.LatestControlsBar
import com.app.toonplay.ui.components.ProvideSmoothBringIntoView
import com.app.toonplay.ui.components.SectionHeaderRow
import com.app.toonplay.ui.components.StarRatingPill
import com.app.toonplay.ui.components.ToonPlayNavItems
import com.app.toonplay.ui.components.ToonPlayProfileView
import com.app.toonplay.ui.components.ToonPlayWordmark
import com.app.toonplay.ui.components.calculateCompleteGridColumns
import com.app.toonplay.ui.components.inlinePosterGridRows
import com.app.toonplay.ui.components.rememberIsReducedMotion
import com.app.toonplay.ui.components.smoothMouseWheelScroll
import com.app.toonplay.ui.state.HomeFeedControlsState
import com.app.toonplay.ui.theme.BorderBright
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianGlass
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.TextSlate400
import com.app.toonplay.ui.theme.ToonIndigoLight
import com.app.toonplay.ui.theme.ToonVioletPrimary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun MobileHomeScreen(
  feed: HomeFeed,
  controls: HomeFeedControlsState,
  initialHeroIndex: Int,
  onSelectTab: (AnimeSectionTab) -> Unit,
  onSelectLanguageFilter: (LatestLanguageFilter) -> Unit,
  onToggleSectionGrid: (AnimeSectionTab, Boolean) -> Unit,
  onHeroIndexChanged: (Int) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onToggleSearch: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val heroItems = feed.hero
  val heroCount = heroItems.size.coerceAtLeast(1)
  val virtualPageCount = if (heroItems.size > 1) heroCount * 400 else heroCount
  val initialVirtualPage =
    if (heroItems.size > 1) heroCount * 200 + (initialHeroIndex % heroCount) else 0

  val pagerState =
    rememberPagerState(
      initialPage = initialVirtualPage,
      pageCount = { virtualPageCount },
    )

  LaunchedEffect(pagerState, heroCount) {
    snapshotFlow { pagerState.currentPage % heroCount }
      .distinctUntilChanged()
      .collect { idx -> onHeroIndexChanged(idx) }
  }

  val latestList =
    remember(feed, controls.latestLanguageFilter) {
      when (controls.latestLanguageFilter) {
        LatestLanguageFilter.JAP_ENG -> feed.globalLatest
        LatestLanguageFilter.HIN_TAM_TEL -> feed.latestReleases
      }
    }

  val homepageLatest = remember(latestList) { latestList.take(12) }
  val homepageChildhood = remember(feed.childhoodShows) { feed.childhoodShows.take(12) }
  val homepageSeries = remember(feed.popularSeries) { feed.popularSeries.take(12) }
  val homepageMovies = remember(feed.popularMovies) { feed.popularMovies.take(12) }

  val homeListState = rememberLazyListState()
  val languageRowState = rememberLazyListState()
  val latestRowState = rememberLazyListState()
  val childhoodRowState = rememberLazyListState()
  val seriesRowState = rememberLazyListState()
  val moviesRowState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val isReducedMotion = rememberIsReducedMotion()

  val bottomBarClearance =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 96.dp

  val animeCatalog =
    remember(feed) {
      (feed.popularSeries + feed.globalLatest + feed.latestReleases).distinctBy { it.id }
    }
  val cartoonCatalog =
    remember(feed) {
      (feed.childhoodShows + feed.latestReleases.filter { it.typeLabel == "SHORT" })
        .distinctBy { it.id }
    }

  Box(modifier = modifier.fillMaxSize().background(ObsidianBg).testTag("mobile_home_screen")) {
    Crossfade(
      targetState = controls.selectedTab,
      animationSpec = tween(durationMillis = 150),
      label = "mobile_tab_crossfade",
    ) { tab ->
      if (tab == AnimeSectionTab.HOME) {
        ProvideSmoothBringIntoView(edgeMargin = 16.dp, isReducedMotion = isReducedMotion) {
          BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val inlineGridColumns =
              remember(this.maxWidth) {
                calculateCompleteGridColumns(
                  availableWidth = (this.maxWidth - 32.dp).coerceAtLeast(0.dp),
                  cardWidth = 158.dp,
                  spacing = 12.dp,
                  minColumns = 1,
                )
              }

            LazyColumn(
              state = homeListState,
              modifier =
                Modifier.fillMaxSize()
                  .smoothMouseWheelScroll(
                    scrollableState = homeListState,
                    orientation = Orientation.Vertical,
                    isReducedMotion = isReducedMotion,
                  )
                  .testTag("mobile_home_feed"),
              contentPadding = PaddingValues(bottom = bottomBarClearance),
              verticalArrangement = Arrangement.Top,
            ) {
              // 1. Bidirectional Hero Fanned Carousel with scrolling ambient backdrop so header stays transparent
              if (heroItems.isNotEmpty()) {
                item(key = "mobile_hero_carousel", contentType = "hero") {
                  MobileHeroSection(
                    heroItems = heroItems,
                    pagerState = pagerState,
                    onItemClick = onItemClick,
                  )
                }
              } else {
                item(key = "mobile_header_spacer", contentType = "spacer") {
                  Spacer(modifier = Modifier.statusBarsPadding().height(64.dp))
                }
              }

              // 2. Explore by Language (9 sliding 2:1 banner cards)
              item(key = "mobile_language_cards", contentType = "language_rail") {
                LanguageCardsRail(
                  formFactor = DeviceFormFactor.MOBILE,
                  listState = languageRowState,
                  horizontalPadding = 16.dp,
                  itemSpacing = 6.dp,
                  sectionSpacing = 12.dp,
                  isReducedMotion = isReducedMotion,
                  testTagPrefix = "mobile_languages",
                  modifier = Modifier.padding(top = 26.dp),
                  onCardClick = { card ->
                    onSelectLanguageFilter(card.latestFilter)
                    scope.launch {
                      if (isReducedMotion) {
                        homeListState.scrollToItem(2)
                      } else {
                        homeListState.animateScrollToItem(2)
                      }
                    }
                  },
                )
              }

              // 3. Latest Episodes & Releases (HIN/TAM/TEL default, max 12 items on homepage)
              if (homepageLatest.isNotEmpty()) {
                val isGrid = AnimeSectionTab.LATEST in controls.gridModeTabs
                if (isGrid) {
                  item(key = "mobile_latest_header", contentType = "section_header") {
                    Column(
                      modifier = Modifier.padding(top = 26.dp),
                      verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                      SectionHeaderRow(
                        title = stringResource(R.string.section_latest_combined),
                        kicker = stringResource(R.string.section_latest_combined_sub),
                        itemCount = homepageLatest.size,
                        modifier = Modifier.padding(horizontal = 16.dp),
                      )
                      LatestControlsBar(
                        selectedFilter = controls.latestLanguageFilter,
                        onSelectFilter = onSelectLanguageFilter,
                        onSeeAll = { onSelectTab(AnimeSectionTab.LATEST) },
                        isGridMode = true,
                        onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.LATEST, it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                      )
                    }
                  }
                  inlinePosterGridRows(
                    sectionKey = "mobile_latest",
                    items = homepageLatest,
                    columns = inlineGridColumns,
                    cardWidth = 158.dp,
                    itemSpacing = 12.dp,
                    horizontalPadding = 16.dp,
                    firstRowTopPadding = 12.dp,
                    rowKeyPrefix = { it.uid },
                  ) { _, item, cellModifier ->
                    AnimePosterCard(
                      item = item,
                      onClick = { onItemClick(item) },
                      width = null,
                      isSaved = item.id in controls.savedIds,
                      onToggleSave = { onToggleSave(item) },
                      showActionFooter = true,
                      modifier = cellModifier,
                    )
                  }
                } else {
                  item(key = "mobile_latest_combined", contentType = "latest_combined") {
                    Column(
                      modifier = Modifier.padding(top = 26.dp),
                      verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                      SectionHeaderRow(
                        title = stringResource(R.string.section_latest_combined),
                        kicker = stringResource(R.string.section_latest_combined_sub),
                        itemCount = homepageLatest.size,
                        modifier = Modifier.padding(horizontal = 16.dp),
                      )
                      LatestControlsBar(
                        selectedFilter = controls.latestLanguageFilter,
                        onSelectFilter = onSelectLanguageFilter,
                        onSeeAll = { onSelectTab(AnimeSectionTab.LATEST) },
                        isGridMode = false,
                        onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.LATEST, it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                      )
                      LazyRow(
                        state = latestRowState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier =
                          Modifier.smoothMouseWheelScroll(
                            scrollableState = latestRowState,
                            orientation = Orientation.Horizontal,
                            isReducedMotion = isReducedMotion,
                          ),
                      ) {
                        items(
                          items = homepageLatest,
                          key = { it.uid },
                          contentType = { "unified_card" },
                        ) { item ->
                          AnimePosterCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            width = 158.dp,
                            isSaved = item.id in controls.savedIds,
                            onToggleSave = { onToggleSave(item) },
                            showActionFooter = true,
                          )
                        }
                      }
                    }
                  }
                }
              }

              // 4. Childhood Shows (max 12 items on homepage)
              if (homepageChildhood.isNotEmpty()) {
                val isGrid = AnimeSectionTab.CHILDHOOD in controls.gridModeTabs
                if (isGrid) {
                  item(key = "mobile_childhood_header", contentType = "section_header") {
                    SectionHeaderRow(
                      title = stringResource(R.string.section_childhood_shows),
                      kicker = stringResource(R.string.section_childhood_shows_sub),
                      itemCount = homepageChildhood.size,
                      onSeeAll = { onSelectTab(AnimeSectionTab.CHILDHOOD) },
                      isGridMode = true,
                      onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.CHILDHOOD, it) },
                      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 26.dp),
                    )
                  }
                  inlinePosterGridRows(
                    sectionKey = "mobile_childhood",
                    items = homepageChildhood,
                    columns = inlineGridColumns,
                    cardWidth = 158.dp,
                    itemSpacing = 12.dp,
                    horizontalPadding = 16.dp,
                    firstRowTopPadding = 12.dp,
                    rowKeyPrefix = { it.uid },
                  ) { flatIndex, item, cellModifier ->
                    AnimePosterCard(
                      item = item,
                      onClick = { onItemClick(item) },
                      width = null,
                      isSaved = item.id in controls.savedIds,
                      onToggleSave = { onToggleSave(item) },
                      rankBadge = flatIndex + 1,
                      showActionFooter = true,
                      modifier = cellModifier,
                    )
                  }
                } else {
                  item(key = "mobile_childhood_section", contentType = "rail") {
                    Column(
                      modifier = Modifier.padding(top = 26.dp),
                      verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                      SectionHeaderRow(
                        title = stringResource(R.string.section_childhood_shows),
                        kicker = stringResource(R.string.section_childhood_shows_sub),
                        itemCount = homepageChildhood.size,
                        onSeeAll = { onSelectTab(AnimeSectionTab.CHILDHOOD) },
                        isGridMode = false,
                        onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.CHILDHOOD, it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                      )
                      LazyRow(
                        state = childhoodRowState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier =
                          Modifier.smoothMouseWheelScroll(
                            scrollableState = childhoodRowState,
                            orientation = Orientation.Horizontal,
                            isReducedMotion = isReducedMotion,
                          ),
                      ) {
                        itemsIndexed(
                          items = homepageChildhood,
                          key = { _, item -> item.uid },
                          contentType = { _, _ -> "unified_card" },
                        ) { index, item ->
                          AnimePosterCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            width = 158.dp,
                            isSaved = item.id in controls.savedIds,
                            onToggleSave = { onToggleSave(item) },
                            rankBadge = index + 1,
                            showActionFooter = true,
                          )
                        }
                      }
                    }
                  }
                }
              }

              // 5. Most Popular TV Series (max 12 items on homepage)
              if (homepageSeries.isNotEmpty()) {
                val isGrid = AnimeSectionTab.SERIES in controls.gridModeTabs
                if (isGrid) {
                  item(key = "mobile_series_header", contentType = "section_header") {
                    SectionHeaderRow(
                      title = stringResource(R.string.section_popular_series),
                      kicker = stringResource(R.string.section_popular_series_sub),
                      itemCount = homepageSeries.size,
                      onSeeAll = { onSelectTab(AnimeSectionTab.SERIES) },
                      isGridMode = true,
                      onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.SERIES, it) },
                      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 26.dp),
                    )
                  }
                  inlinePosterGridRows(
                    sectionKey = "mobile_series",
                    items = homepageSeries,
                    columns = inlineGridColumns,
                    cardWidth = 158.dp,
                    itemSpacing = 12.dp,
                    horizontalPadding = 16.dp,
                    firstRowTopPadding = 12.dp,
                    rowKeyPrefix = { it.uid },
                  ) { _, item, cellModifier ->
                    AnimePosterCard(
                      item = item,
                      onClick = { onItemClick(item) },
                      width = null,
                      isSaved = item.id in controls.savedIds,
                      onToggleSave = { onToggleSave(item) },
                      showActionFooter = true,
                      modifier = cellModifier,
                    )
                  }
                } else {
                  item(key = "mobile_series_section", contentType = "rail") {
                    Column(
                      modifier = Modifier.padding(top = 26.dp),
                      verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                      SectionHeaderRow(
                        title = stringResource(R.string.section_popular_series),
                        kicker = stringResource(R.string.section_popular_series_sub),
                        itemCount = homepageSeries.size,
                        onSeeAll = { onSelectTab(AnimeSectionTab.SERIES) },
                        isGridMode = false,
                        onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.SERIES, it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                      )
                      LazyRow(
                        state = seriesRowState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier =
                          Modifier.smoothMouseWheelScroll(
                            scrollableState = seriesRowState,
                            orientation = Orientation.Horizontal,
                            isReducedMotion = isReducedMotion,
                          ),
                      ) {
                        items(
                          items = homepageSeries,
                          key = { it.uid },
                          contentType = { "unified_card" },
                        ) { item ->
                          AnimePosterCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            width = 158.dp,
                            isSaved = item.id in controls.savedIds,
                            onToggleSave = { onToggleSave(item) },
                            showActionFooter = true,
                          )
                        }
                      }
                    }
                  }
                }
              }

              // 6. Blockbuster Anime Movies (max 12 items on homepage)
              if (homepageMovies.isNotEmpty()) {
                val isGrid = AnimeSectionTab.MOVIES in controls.gridModeTabs
                if (isGrid) {
                  item(key = "mobile_movies_header", contentType = "section_header") {
                    SectionHeaderRow(
                      title = stringResource(R.string.section_popular_movies),
                      kicker = stringResource(R.string.section_popular_movies_sub),
                      itemCount = homepageMovies.size,
                      onSeeAll = { onSelectTab(AnimeSectionTab.MOVIES) },
                      isGridMode = true,
                      onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.MOVIES, it) },
                      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 26.dp),
                    )
                  }
                  inlinePosterGridRows(
                    sectionKey = "mobile_movies",
                    items = homepageMovies,
                    columns = inlineGridColumns,
                    cardWidth = 158.dp,
                    itemSpacing = 12.dp,
                    horizontalPadding = 16.dp,
                    firstRowTopPadding = 12.dp,
                    rowKeyPrefix = { it.uid },
                  ) { _, item, cellModifier ->
                    AnimePosterCard(
                      item = item,
                      onClick = { onItemClick(item) },
                      width = null,
                      isSaved = item.id in controls.savedIds,
                      onToggleSave = { onToggleSave(item) },
                      showActionFooter = true,
                      modifier = cellModifier,
                    )
                  }
                } else {
                  item(key = "mobile_movies_section", contentType = "rail") {
                    Column(
                      modifier = Modifier.padding(top = 26.dp),
                      verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                      SectionHeaderRow(
                        title = stringResource(R.string.section_popular_movies),
                        kicker = stringResource(R.string.section_popular_movies_sub),
                        itemCount = homepageMovies.size,
                        onSeeAll = { onSelectTab(AnimeSectionTab.MOVIES) },
                        isGridMode = false,
                        onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.MOVIES, it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                      )
                      LazyRow(
                        state = moviesRowState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier =
                          Modifier.smoothMouseWheelScroll(
                            scrollableState = moviesRowState,
                            orientation = Orientation.Horizontal,
                            isReducedMotion = isReducedMotion,
                          ),
                      ) {
                        items(
                          items = homepageMovies,
                          key = { it.uid },
                          contentType = { "unified_card" },
                        ) { item ->
                          AnimePosterCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            width = 158.dp,
                            isSaved = item.id in controls.savedIds,
                            onToggleSave = { onToggleSave(item) },
                            showActionFooter = true,
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      } else if (tab == AnimeSectionTab.PROFILE) {
        ToonPlayProfileView(
          feed = feed,
          savedItems = controls.savedItems,
          savedIds = controls.savedIds,
          selectedLanguageFilter = controls.latestLanguageFilter,
          onSelectLanguageFilter = onSelectLanguageFilter,
          onItemClick = onItemClick,
          onToggleSave = onToggleSave,
          onExploreHome = { onSelectTab(AnimeSectionTab.HOME) },
          bottomPadding = bottomBarClearance,
          modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 62.dp),
        )
      } else {
        val (title, items) =
          when (tab) {
            AnimeSectionTab.ANIME ->
              stringResource(R.string.section_anime_library) to animeCatalog
            AnimeSectionTab.CARTOON ->
              stringResource(R.string.section_cartoon_library) to cartoonCatalog
            AnimeSectionTab.LATEST ->
              stringResource(R.string.section_latest_combined) to latestList
            AnimeSectionTab.CHILDHOOD ->
              stringResource(R.string.section_childhood_shows) to feed.childhoodShows
            AnimeSectionTab.SERIES ->
              stringResource(R.string.section_popular_series) to feed.popularSeries
            AnimeSectionTab.MOVIES ->
              stringResource(R.string.section_popular_movies) to feed.popularMovies
            AnimeSectionTab.SAVED ->
              stringResource(R.string.section_saved) to controls.savedItems
            AnimeSectionTab.HOME,
            AnimeSectionTab.PROFILE -> "" to emptyList()
          }
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 62.dp)) {
          SectionHeaderRow(
            title = title,
            kicker = "${items.size} titles",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          )
          AnimePosterGrid(
            items = items,
            savedIds = controls.savedIds,
            onItemClick = onItemClick,
            onToggleSave = onToggleSave,
            minCellWidth = 158.dp,
            horizontalPadding = 16.dp,
            itemSpacing = 12.dp,
            bottomPadding = bottomBarClearance,
            emptyTitle =
              if (tab == AnimeSectionTab.SAVED)
                stringResource(R.string.empty_saved_title)
              else stringResource(R.string.empty_search_title),
            emptySubtitle =
              if (tab == AnimeSectionTab.SAVED)
                stringResource(R.string.empty_saved_sub)
              else stringResource(R.string.empty_search_sub),
          )
        }
      }
    }

    MobileTopHeader(
      onOpenSearch = { onToggleSearch(true) },
      modifier = Modifier.align(Alignment.TopCenter),
    )

    MobileFloatingBottomBar(
      selectedTab = controls.selectedTab,
      onSelectTab = { selected ->
        if (selected == AnimeSectionTab.HOME && controls.selectedTab == AnimeSectionTab.HOME) {
          scope.launch {
            if (isReducedMotion) {
              homeListState.scrollToItem(0)
            } else {
              homeListState.animateScrollToItem(0)
            }
          }
        } else {
          onSelectTab(selected)
        }
      },
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

@Composable
private fun MobileHeroSection(
  heroItems: List<AnimeItem>,
  pagerState: androidx.compose.foundation.pager.PagerState,
  onItemClick: (AnimeItem) -> Unit,
) {
  val currentHeroIndex by remember(heroItems, pagerState) {
    derivedStateOf { pagerState.currentPage % heroItems.size }
  }
  val activeHero = heroItems[currentHeroIndex]

  Box(modifier = Modifier.fillMaxWidth()) {
    AmbientHeroBackdrop(
      imageUrl = activeHero.backdropUrl ?: activeHero.posterUrl,
      height = 492.dp,
    )

    Column(
      modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 64.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      FannedHeroCarousel(
        items = heroItems,
        pagerState = pagerState,
        onItemClick = onItemClick,
        cardWidth = 198.dp,
        overlapFactor = 38f,
      )

      Spacer(modifier = Modifier.height(16.dp))

      Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 138.dp),
        contentAlignment = Alignment.TopCenter,
      ) {
        Crossfade(
          targetState = activeHero,
          animationSpec = tween(durationMillis = 320),
          label = "mobile_hero_meta",
        ) { hero ->
          Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            val yearType =
              listOfNotNull(hero.year, hero.typeLabel).joinToString("  •  ")
            if (yearType.isNotBlank()) {
              Text(
                text = yearType,
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
              )
            }
            Text(
              text = hero.title,
              style = MaterialTheme.typography.headlineMedium,
              color = TextPrimary,
              textAlign = TextAlign.Center,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            hero.rating?.let { r ->
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                StarRatingPill(rating = r)
              }
            }
            if (hero.overview.isNotBlank()) {
              Text(
                text = hero.overview,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp),
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      CarouselDotIndicator(
        count = heroItems.size,
        currentIndex = currentHeroIndex,
      )
    }
  }
}

@Composable
private fun MobileTopHeader(
  onOpenSearch: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(Color.Transparent)
        .statusBarsPadding()
        .padding(start = 16.dp, end = 16.dp, top = 3.dp, bottom = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    ToonPlayWordmark(compact = true)

    GlassCircleIconButton(
      icon = Icons.Rounded.Search,
      contentDescription = stringResource(R.string.cd_search),
      onClick = onOpenSearch,
      shape = CircleShape,
      testTag = "mobile_search_button",
    )
  }
}

@Composable
private fun MobileFloatingBottomBar(
  selectedTab: AnimeSectionTab,
  onSelectTab: (AnimeSectionTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            0.0f to Color.Transparent,
            1.0f to ObsidianBg.copy(alpha = 0.94f),
          )
        )
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(32.dp),
      color = ObsidianGlass,
      border = BorderStroke(1.dp, BorderBright.copy(alpha = 0.45f)),
      shadowElevation = 16.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ToonPlayNavItems.forEach { navItem ->
          val label = stringResource(navItem.labelRes)
          val selected = selectedTab == navItem.tab
          Column(
            modifier =
              Modifier.weight(1f)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(24.dp))
                .clickable { onSelectTab(navItem.tab) }
                .padding(vertical = 7.dp)
                .testTag("mobile_nav_${navItem.tab.name.lowercase()}"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
          ) {
            Icon(
              painter = painterResource(id = navItem.iconRes),
              contentDescription = label,
              tint = if (selected) ToonIndigoLight else TextSlate400,
              modifier = Modifier.size(22.dp),
            )
            Text(
              text = label,
              style =
                MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
              color = if (selected) ToonIndigoLight else TextSlate400,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}