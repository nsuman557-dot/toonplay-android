package com.app.toonplay.ui.screens.bigtablet

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.HomeFeed
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.ui.components.AnimePosterCard
import com.app.toonplay.ui.components.AnimePosterGrid
import com.app.toonplay.ui.components.CarouselDotIndicator
import com.app.toonplay.ui.components.FannedHeroCarousel
import com.app.toonplay.ui.components.GlassSearchBar
import com.app.toonplay.ui.components.HorizontalRailScrollControls
import com.app.toonplay.ui.components.LatestControlsBar
import com.app.toonplay.ui.components.MetadataPill
import com.app.toonplay.ui.components.ProvideSmoothBringIntoView
import com.app.toonplay.ui.components.ResponsiveSectionGrid
import com.app.toonplay.ui.components.SectionHeaderRow
import com.app.toonplay.ui.components.StarRatingPill
import com.app.toonplay.ui.components.ToonPlayNavItems
import com.app.toonplay.ui.components.ToonPlayProfileView
import com.app.toonplay.ui.components.ToonPlayWordmark
import com.app.toonplay.ui.components.rememberIsReducedMotion
import com.app.toonplay.ui.components.smoothMouseWheelScroll
import com.app.toonplay.ui.state.HomeScreenUiState
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianSurface
import com.app.toonplay.ui.theme.TextOffWhite
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonVioletPrimary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun BigTabletHomeScreen(
  feed: HomeFeed,
  uiState: HomeScreenUiState,
  onSelectTab: (AnimeSectionTab) -> Unit,
  onSelectLanguageFilter: (LatestLanguageFilter) -> Unit,
  onToggleSectionGrid: (AnimeSectionTab, Boolean) -> Unit,
  onHeroIndexChanged: (Int) -> Unit,
  onExpandDetail: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val heroItems = feed.hero
  val heroCount = heroItems.size.coerceAtLeast(1)
  val virtualCount = if (heroItems.size > 1) heroCount * 400 else heroCount
  val initialPage =
    if (heroItems.size > 1) heroCount * 200 + (uiState.activeHeroIndex % heroCount) else 0

  val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { virtualCount })
  LaunchedEffect(pagerState, heroCount) {
    snapshotFlow { pagerState.currentPage % heroCount }
      .distinctUntilChanged()
      .collect { idx -> onHeroIndexChanged(idx) }
  }

  val latestList =
    remember(feed, uiState.latestLanguageFilter) {
      when (uiState.latestLanguageFilter) {
        LatestLanguageFilter.JAP_ENG -> feed.globalLatest
        LatestLanguageFilter.HIN_TAM_TEL -> feed.latestReleases
      }
    }

  val homepageLatest = remember(latestList) { latestList.take(12) }
  val homepageChildhood = remember(feed.childhoodShows) { feed.childhoodShows.take(12) }
  val homepageSeries = remember(feed.popularSeries) { feed.popularSeries.take(12) }
  val homepageMovies = remember(feed.popularMovies) { feed.popularMovies.take(12) }

  val homeListState = rememberLazyListState()
  val latestRowState = rememberLazyListState()
  val childhoodRowState = rememberLazyListState()
  val seriesRowState = rememberLazyListState()
  val moviesRowState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val isReducedMotion = rememberIsReducedMotion()

  val animeCatalog =
    remember(feed) {
      (feed.popularSeries + feed.globalLatest + feed.latestReleases).distinctBy { it.id }
    }
  val cartoonCatalog =
    remember(feed) {
      (feed.childhoodShows + feed.latestReleases.filter { it.typeLabel == "SHORT" })
        .distinctBy { it.id }
    }

  Row(
    modifier = modifier.fillMaxSize().background(ObsidianBg).testTag("big_tablet_home_screen"),
  ) {
    NavigationRail(
      containerColor = ObsidianSurface,
      modifier = Modifier.fillMaxHeight().statusBarsPadding().navigationBarsPadding(),
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      ToonPlayNavItems.forEach { navItem ->
        val label = stringResource(navItem.labelRes)
        NavigationRailItem(
          selected = uiState.selectedTab == navItem.tab,
          onClick = {
            if (navItem.tab == AnimeSectionTab.HOME && uiState.selectedTab == AnimeSectionTab.HOME) {
              scope.launch {
                if (isReducedMotion) {
                  homeListState.scrollToItem(0)
                } else {
                  homeListState.animateScrollToItem(0)
                }
              }
            } else {
              onSelectTab(navItem.tab)
            }
          },
          icon = {
            Icon(
              painter = painterResource(id = navItem.iconRes),
              contentDescription = label,
              modifier = Modifier.size(22.dp),
            )
          },
          label = {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          },
          colors =
            NavigationRailItemDefaults.colors(
              selectedIconColor = TextPrimary,
              selectedTextColor = TextPrimary,
              indicatorColor = ToonVioletPrimary.copy(alpha = 0.28f),
              unselectedIconColor = TextOffWhite,
              unselectedTextColor = TextSecondary,
            ),
          modifier = Modifier.testTag("big_tablet_nav_${navItem.tab.name.lowercase()}"),
        )
      }
    }

    Column(modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding()) {
      Row(
        modifier =
          Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ToonPlayWordmark(height = 27.dp)
        Spacer(modifier = Modifier.weight(0.15f))
        GlassSearchBar(
          query = uiState.searchQuery,
          onQueryChange = onSearchQueryChange,
          modifier = Modifier.weight(1f),
        )
      }

      // Full-Width Feed (no right-side inspector pane on Big Tablet)
      val paneMode = if (uiState.searchQuery.isNotBlank()) null else uiState.selectedTab
      Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp)) {
        Crossfade(
          targetState = paneMode,
          animationSpec = tween(durationMillis = 150),
          label = "big_tablet_tab_crossfade",
        ) { tab ->
          if (tab == null) {
            AnimePosterGrid(
              items = uiState.searchResults,
              savedIds = uiState.savedIds,
              onItemClick = onExpandDetail,
              onToggleSave = onToggleSave,
              minCellWidth = 160.dp,
              horizontalPadding = 0.dp,
              itemSpacing = 14.dp,
            )
          } else if (tab == AnimeSectionTab.HOME) {
            ProvideSmoothBringIntoView(edgeMargin = 24.dp, isReducedMotion = isReducedMotion) {
              LazyColumn(
                state = homeListState,
                modifier =
                  Modifier.fillMaxSize()
                    .smoothMouseWheelScroll(
                      scrollableState = homeListState,
                      orientation = Orientation.Vertical,
                      isReducedMotion = isReducedMotion,
                    ),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(26.dp),
              ) {
                if (heroItems.isNotEmpty()) {
                  item(key = "big_tablet_hero", contentType = "hero") {
                    BigTabletHeroBanner(
                      heroItems = heroItems,
                      pagerState = pagerState,
                      onExplore = onExpandDetail,
                    )
                  }
                }

                // 1. Latest Episodes & Releases (HIN/TAM/TEL default, max 12 items)
                if (homepageLatest.isNotEmpty()) {
                  item(key = "big_tablet_latest", contentType = "rail") {
                    val isGrid = AnimeSectionTab.LATEST in uiState.gridModeTabs
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                      SectionHeaderRow(
                        title = stringResource(R.string.section_latest_combined),
                        kicker = stringResource(R.string.section_latest_combined_sub),
                        itemCount = homepageLatest.size,
                      )
                      LatestControlsBar(
                        selectedFilter = uiState.latestLanguageFilter,
                        onSelectFilter = onSelectLanguageFilter,
                        onSeeAll = { onSelectTab(AnimeSectionTab.LATEST) },
                        isGridMode = isGrid,
                        onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.LATEST, it) },
                        trailingLeadingControls =
                          if (!isGrid) {
                            {
                              HorizontalRailScrollControls(
                                listState = latestRowState,
                                stepDistance = 356.dp,
                                isReducedMotion = isReducedMotion,
                                testTagPrefix = "big_tablet_latest",
                              )
                            }
                          } else null,
                      )
                      if (isGrid) {
                        BigTabletSectionInlineGrid(
                          items = homepageLatest,
                          savedIds = uiState.savedIds,
                          cardWidth = 164.dp,
                          showRank = false,
                          onItemClick = onExpandDetail,
                          onToggleSave = onToggleSave,
                        )
                      } else {
                        LazyRow(
                          state = latestRowState,
                          horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                              onClick = { onExpandDetail(item) },
                              width = 164.dp,
                              isSaved = item.id in uiState.savedIds,
                              onToggleSave = { onToggleSave(item) },
                              showActionFooter = true,
                            )
                          }
                        }
                      }
                    }
                  }
                }

                // 2. Childhood Shows (max 12 items)
                if (homepageChildhood.isNotEmpty()) {
                  item(key = "big_tablet_childhood", contentType = "rail") {
                    val isGrid = AnimeSectionTab.CHILDHOOD in uiState.gridModeTabs
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                      SectionHeaderRow(
                        title = stringResource(R.string.section_childhood_shows),
                        kicker = stringResource(R.string.section_childhood_shows_sub),
                        itemCount = homepageChildhood.size,
                        onSeeAll = { onSelectTab(AnimeSectionTab.CHILDHOOD) },
                        isGridMode = isGrid,
                        onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.CHILDHOOD, it) },
                        trailingLeadingControls =
                          if (!isGrid) {
                            {
                              HorizontalRailScrollControls(
                                listState = childhoodRowState,
                                stepDistance = 348.dp,
                                isReducedMotion = isReducedMotion,
                                testTagPrefix = "big_tablet_childhood",
                              )
                            }
                          } else null,
                      )
                      if (isGrid) {
                        BigTabletSectionInlineGrid(
                          items = homepageChildhood,
                          savedIds = uiState.savedIds,
                          cardWidth = 160.dp,
                          showRank = true,
                          onItemClick = onExpandDetail,
                          onToggleSave = onToggleSave,
                        )
                      } else {
                        LazyRow(
                          state = childhoodRowState,
                          horizontalArrangement = Arrangement.spacedBy(14.dp),
                          modifier =
                            Modifier.smoothMouseWheelScroll(
                              scrollableState = childhoodRowState,
                              orientation = Orientation.Horizontal,
                              isReducedMotion = isReducedMotion,
                            ),
                        ) {
                          itemsIndexed(
                            items = homepageChildhood,
                            key = { _, it -> it.uid },
                            contentType = { _, _ -> "unified_card" },
                          ) { idx, item ->
                            AnimePosterCard(
                              item = item,
                              onClick = { onExpandDetail(item) },
                              width = 160.dp,
                              isSaved = item.id in uiState.savedIds,
                              onToggleSave = { onToggleSave(item) },
                              rankBadge = idx + 1,
                              showActionFooter = true,
                            )
                          }
                        }
                      }
                    }
                  }
                }

                // 3. Most Popular TV Series & 4. Blockbuster Anime Movies (max 12 items)
                val railConfigs =
                  listOf(
                    Triple(
                      R.string.section_popular_series,
                      R.string.section_popular_series_sub,
                      Triple(homepageSeries, AnimeSectionTab.SERIES, seriesRowState),
                    ),
                    Triple(
                      R.string.section_popular_movies,
                      R.string.section_popular_movies_sub,
                      Triple(homepageMovies, AnimeSectionTab.MOVIES, moviesRowState),
                    ),
                  )
                railConfigs.forEach { (titleRes, subRes, triple) ->
                  val (list, tab, rowState) = triple
                  if (list.isNotEmpty()) {
                    item(key = "big_tablet_${tab.name}", contentType = "rail") {
                      val isGrid = tab in uiState.gridModeTabs
                      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeaderRow(
                          title = stringResource(titleRes),
                          kicker = stringResource(subRes),
                          itemCount = list.size,
                          onSeeAll = { onSelectTab(tab) },
                          isGridMode = isGrid,
                          onToggleGridMode = { onToggleSectionGrid(tab, it) },
                          trailingLeadingControls =
                            if (!isGrid) {
                              {
                                HorizontalRailScrollControls(
                                  listState = rowState,
                                  stepDistance = 344.dp,
                                  isReducedMotion = isReducedMotion,
                                  testTagPrefix = "big_tablet_${tab.name.lowercase()}",
                                )
                              }
                            } else null,
                        )
                        if (isGrid) {
                          BigTabletSectionInlineGrid(
                            items = list,
                            savedIds = uiState.savedIds,
                            cardWidth = 158.dp,
                            showRank = false,
                            onItemClick = onExpandDetail,
                            onToggleSave = onToggleSave,
                          )
                        } else {
                          LazyRow(
                            state = rowState,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier =
                              Modifier.smoothMouseWheelScroll(
                                scrollableState = rowState,
                                orientation = Orientation.Horizontal,
                                isReducedMotion = isReducedMotion,
                              ),
                          ) {
                            items(
                              items = list,
                              key = { it.uid },
                              contentType = { "unified_card" },
                            ) { item ->
                              AnimePosterCard(
                                item = item,
                                onClick = { onExpandDetail(item) },
                                width = 158.dp,
                                isSaved = item.id in uiState.savedIds,
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
              savedItems = uiState.savedItems,
              savedIds = uiState.savedIds,
              selectedLanguageFilter = uiState.latestLanguageFilter,
              onSelectLanguageFilter = onSelectLanguageFilter,
              onItemClick = onExpandDetail,
              onToggleSave = onToggleSave,
              onExploreHome = { onSelectTab(AnimeSectionTab.HOME) },
              horizontalPadding = 0.dp,
              bottomPadding = 28.dp,
            )
          } else {
            val items =
              when (tab) {
                AnimeSectionTab.ANIME -> animeCatalog
                AnimeSectionTab.CARTOON -> cartoonCatalog
                AnimeSectionTab.LATEST -> latestList
                AnimeSectionTab.CHILDHOOD -> feed.childhoodShows
                AnimeSectionTab.SERIES -> feed.popularSeries
                AnimeSectionTab.MOVIES -> feed.popularMovies
                AnimeSectionTab.SAVED -> uiState.savedItems
                AnimeSectionTab.HOME,
                AnimeSectionTab.PROFILE -> emptyList()
              }
            AnimePosterGrid(
              items = items,
              savedIds = uiState.savedIds,
              onItemClick = onExpandDetail,
              onToggleSave = onToggleSave,
              minCellWidth = 160.dp,
              horizontalPadding = 0.dp,
              itemSpacing = 14.dp,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BigTabletSectionInlineGrid(
  items: List<AnimeItem>,
  savedIds: Set<String>,
  cardWidth: Dp,
  showRank: Boolean,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
) {
  ResponsiveSectionGrid(
    items = items,
    cardWidth = cardWidth,
    itemSpacing = 14.dp,
    horizontalPadding = 0.dp,
  ) { flatIndex, item, cellModifier ->
    val rank = if (showRank) flatIndex + 1 else null
    AnimePosterCard(
      item = item,
      onClick = { onItemClick(item) },
      width = null,
      isSaved = item.id in savedIds,
      onToggleSave = { onToggleSave(item) },
      rankBadge = rank,
      showActionFooter = true,
      modifier = cellModifier,
    )
  }
}

@Composable
private fun BigTabletHeroBanner(
  heroItems: List<AnimeItem>,
  pagerState: androidx.compose.foundation.pager.PagerState,
  onExplore: (AnimeItem) -> Unit,
) {
  val context = LocalContext.current
  val activeHero = heroItems[pagerState.currentPage % heroItems.size]
  val backdropUrl = activeHero.backdropUrl.ifBlank { activeHero.posterUrl }
  val backdropRequest =
    remember(backdropUrl) {
      ImageRequest.Builder(context)
        .data(backdropUrl)
        .crossfade(false)
        .size(960, 540)
        .build()
    }
  val bannerGradient =
    remember {
      Brush.horizontalGradient(
        0f to ObsidianBg.copy(alpha = 0.97f),
        0.38f to ObsidianBg.copy(alpha = 0.86f),
        0.66f to ObsidianBg.copy(alpha = 0.66f),
        1f to ObsidianBg.copy(alpha = 0.48f),
      )
    }

  Surface(
    shape = RoundedCornerShape(26.dp),
    color = ObsidianCard,
    border = BorderStroke(1.dp, BorderSubtle),
    modifier = Modifier.fillMaxWidth().height(340.dp),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      AsyncImage(
        model = backdropRequest,
        contentDescription = activeHero.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
      Box(modifier = Modifier.fillMaxSize().background(bannerGradient))

      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        Crossfade(
          targetState = activeHero,
          animationSpec = tween(durationMillis = 320),
          label = "big_tablet_hero_meta",
          modifier = Modifier.weight(0.50f),
        ) { hero ->
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              hero.rating?.let { StarRatingPill(rating = it) }
              hero.year?.let { MetadataPill(text = it) }
              MetadataPill(text = hero.typeLabel)
            }

            Text(
              text = hero.title,
              style = MaterialTheme.typography.headlineMedium,
              color = TextPrimary,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )

            if (hero.overview.isNotBlank()) {
              Text(
                text = hero.overview,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
              onClick = { onExplore(hero) },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ToonVioletPrimary),
              modifier = Modifier.height(44.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp),
              )
              Text(
                text = stringResource(R.string.action_explore),
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }
        }

        Column(
          modifier = Modifier.weight(0.50f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          FannedHeroCarousel(
            items = heroItems,
            pagerState = pagerState,
            onItemClick = onExplore,
            cardWidth = 172.dp,
            overlapFactor = 30f,
          )
          Spacer(modifier = Modifier.height(10.dp))
          CarouselDotIndicator(
            count = heroItems.size,
            currentIndex = pagerState.currentPage % heroItems.size,
          )
        }
      }
    }
  }
}