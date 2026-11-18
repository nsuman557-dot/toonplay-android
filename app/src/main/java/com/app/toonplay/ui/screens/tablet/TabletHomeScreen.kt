package com.app.toonplay.ui.screens.tablet

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
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
import com.app.toonplay.ui.components.AmbientHeroBackdrop
import com.app.toonplay.ui.components.AnimePosterCard
import com.app.toonplay.ui.components.AnimePosterGrid
import com.app.toonplay.ui.components.CarouselDotIndicator
import com.app.toonplay.ui.components.FannedHeroCarousel
import com.app.toonplay.ui.components.FullScreenSearchSheet
import com.app.toonplay.ui.components.GlassCircleIconButton
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
fun TabletHomeScreen(
  feed: HomeFeed,
  uiState: HomeScreenUiState,
  onSelectTab: (AnimeSectionTab) -> Unit,
  onSelectLanguageFilter: (LatestLanguageFilter) -> Unit,
  onToggleSectionGrid: (AnimeSectionTab, Boolean) -> Unit,
  onHeroIndexChanged: (Int) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onToggleSearch: (Boolean) -> Unit,
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
      .collect { onHeroIndexChanged(it) }
  }
  val activeHero =
    if (heroItems.isNotEmpty()) heroItems[pagerState.currentPage % heroItems.size] else null

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

  Box(modifier = modifier.fillMaxSize().background(ObsidianBg).testTag("tablet_home_screen")) {
    Row(modifier = Modifier.fillMaxSize()) {
      NavigationRail(
        containerColor = ObsidianSurface,
        contentColor = TextPrimary,
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
            modifier = Modifier.testTag("tablet_nav_${navItem.tab.name.lowercase()}"),
          )
        }
      }

      Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
          Row(
            modifier =
              Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            ToonPlayWordmark(height = 26.dp)
            GlassCircleIconButton(
              icon = Icons.Rounded.Search,
              contentDescription = stringResource(R.string.cd_search),
              onClick = { onToggleSearch(true) },
              shape = CircleShape,
              testTag = "tablet_search_button",
            )
          }

          Crossfade(
            targetState = uiState.selectedTab,
            animationSpec = tween(durationMillis = 150),
            label = "tablet_tab_crossfade",
          ) { tab ->
            if (tab == AnimeSectionTab.HOME) {
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
                  contentPadding = PaddingValues(top = 12.dp, bottom = 36.dp),
                  verticalArrangement = Arrangement.spacedBy(26.dp),
                ) {
                  if (heroItems.isNotEmpty() && activeHero != null) {
                    item(key = "tablet_hero_showcase", contentType = "hero") {
                      Box(modifier = Modifier.fillMaxWidth()) {
                        AmbientHeroBackdrop(
                          imageUrl = activeHero.backdropUrl ?: activeHero.posterUrl,
                          height = 350.dp,
                        )
                        TabletHeroBanner(
                          heroItems = heroItems,
                          activeHero = activeHero,
                          pagerState = pagerState,
                          onItemClick = onItemClick,
                        )
                      }
                    }
                  }

                  // 1. Latest Episodes & Releases (HIN/TAM/TEL default, max 12 items)
                  if (homepageLatest.isNotEmpty()) {
                    item(key = "tablet_latest_combined", contentType = "rail") {
                      val isGrid = AnimeSectionTab.LATEST in uiState.gridModeTabs
                      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeaderRow(
                          title = stringResource(R.string.section_latest_combined),
                          kicker = stringResource(R.string.section_latest_combined_sub),
                          itemCount = homepageLatest.size,
                          modifier = Modifier.padding(horizontal = 24.dp),
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
                                  testTagPrefix = "tablet_latest",
                                )
                              }
                            } else null,
                          modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        if (isGrid) {
                          TabletSectionInlineGrid(
                            items = homepageLatest,
                            savedIds = uiState.savedIds,
                            cardWidth = 164.dp,
                            showRank = false,
                            onItemClick = onItemClick,
                            onToggleSave = onToggleSave,
                          )
                        } else {
                          LazyRow(
                            state = latestRowState,
                            contentPadding = PaddingValues(horizontal = 24.dp),
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
                                onClick = { onItemClick(item) },
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
                    item(key = "tablet_childhood", contentType = "rail") {
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
                                  testTagPrefix = "tablet_childhood",
                                )
                              }
                            } else null,
                          modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        if (isGrid) {
                          TabletSectionInlineGrid(
                            items = homepageChildhood,
                            savedIds = uiState.savedIds,
                            cardWidth = 160.dp,
                            showRank = true,
                            onItemClick = onItemClick,
                            onToggleSave = onToggleSave,
                          )
                        } else {
                          LazyRow(
                            state = childhoodRowState,
                            contentPadding = PaddingValues(horizontal = 24.dp),
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
                                onClick = { onItemClick(item) },
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
                    val (list, targetTab, rowState) = triple
                    if (list.isNotEmpty()) {
                      item(key = "tablet_rail_${targetTab.name}", contentType = "rail") {
                        val isGrid = targetTab in uiState.gridModeTabs
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                          SectionHeaderRow(
                            title = stringResource(titleRes),
                            kicker = stringResource(subRes),
                            itemCount = list.size,
                            onSeeAll = { onSelectTab(targetTab) },
                            isGridMode = isGrid,
                            onToggleGridMode = { onToggleSectionGrid(targetTab, it) },
                            trailingLeadingControls =
                              if (!isGrid) {
                                {
                                  HorizontalRailScrollControls(
                                    listState = rowState,
                                    stepDistance = 344.dp,
                                    isReducedMotion = isReducedMotion,
                                    testTagPrefix = "tablet_${targetTab.name.lowercase()}",
                                  )
                                }
                              } else null,
                            modifier = Modifier.padding(horizontal = 24.dp),
                          )
                          if (isGrid) {
                            TabletSectionInlineGrid(
                              items = list,
                              savedIds = uiState.savedIds,
                              cardWidth = 158.dp,
                              showRank = false,
                              onItemClick = onItemClick,
                              onToggleSave = onToggleSave,
                            )
                          } else {
                            LazyRow(
                              state = rowState,
                              contentPadding = PaddingValues(horizontal = 24.dp),
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
                                  onClick = { onItemClick(item) },
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
                onItemClick = onItemClick,
                onToggleSave = onToggleSave,
                onExploreHome = { onSelectTab(AnimeSectionTab.HOME) },
                horizontalPadding = 24.dp,
                bottomPadding = 32.dp,
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
                onItemClick = onItemClick,
                onToggleSave = onToggleSave,
                minCellWidth = 160.dp,
                horizontalPadding = 24.dp,
                itemSpacing = 14.dp,
                bottomPadding = 32.dp,
              )
            }
          }
        }
      }
    }

    if (uiState.isSearchActive) {
      FullScreenSearchSheet(
        query = uiState.searchQuery,
        results = uiState.searchResults,
        allItems = feed.allUnique,
        savedIds = uiState.savedIds,
        onQueryChange = onSearchQueryChange,
        onItemClick = onItemClick,
        onToggleSave = onToggleSave,
        onClose = { onToggleSearch(false) },
      )
    }
  }
}

@Composable
private fun TabletSectionInlineGrid(
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
    horizontalPadding = 24.dp,
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
private fun TabletHeroBanner(
  heroItems: List<AnimeItem>,
  activeHero: AnimeItem,
  pagerState: androidx.compose.foundation.pager.PagerState,
  onItemClick: (AnimeItem) -> Unit,
) {
  val context = LocalContext.current
  val backdropRequest =
    remember(activeHero.backdropUrl) {
      ImageRequest.Builder(context)
        .data(activeHero.backdropUrl)
        .crossfade(false)
        .size(960, 540)
        .build()
    }
  val bannerGradient =
    remember {
      Brush.horizontalGradient(
        0f to ObsidianBg.copy(alpha = 0.48f),
        0.46f to ObsidianBg.copy(alpha = 0.78f),
        0.78f to ObsidianBg.copy(alpha = 0.92f),
        1f to ObsidianBg.copy(alpha = 0.97f),
      )
    }
  Surface(
    shape = RoundedCornerShape(26.dp),
    color = ObsidianCard,
    border = BorderStroke(1.dp, BorderSubtle),
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(340.dp),
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
        modifier = Modifier.fillMaxSize().padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        Column(
          modifier = Modifier.weight(0.50f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          FannedHeroCarousel(
            items = heroItems,
            pagerState = pagerState,
            onItemClick = onItemClick,
            cardWidth = 166.dp,
            overlapFactor = 28f,
          )
          Spacer(modifier = Modifier.height(10.dp))
          CarouselDotIndicator(
            count = heroItems.size,
            currentIndex = pagerState.currentPage % heroItems.size,
          )
        }

        Column(
          modifier = Modifier.weight(0.50f),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            activeHero.rating?.let { StarRatingPill(rating = it) }
            activeHero.year?.let { MetadataPill(text = it) }
            MetadataPill(text = activeHero.typeLabel)
          }
          Text(
            text = activeHero.title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          if (activeHero.overview.isNotBlank()) {
            Text(
              text = activeHero.overview,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              maxLines = 4,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Button(
            onClick = { onItemClick(activeHero) },
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
    }
  }
}