package com.app.toonplay.ui.screens.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.HomeFeed
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.ui.components.AnimeDetailInspectorPane
import com.app.toonplay.ui.components.AnimePosterCard
import com.app.toonplay.ui.components.AnimePosterGrid
import com.app.toonplay.ui.components.CarouselDotIndicator
import com.app.toonplay.ui.components.FannedHeroCarousel
import com.app.toonplay.ui.components.GlassSearchBar
import com.app.toonplay.ui.components.LatestControlsBar
import com.app.toonplay.ui.components.MetadataPill
import com.app.toonplay.ui.components.SectionHeaderRow
import com.app.toonplay.ui.components.StarRatingPill
import com.app.toonplay.ui.components.ToonPlayNavItems
import com.app.toonplay.ui.components.ToonPlayProfileView
import com.app.toonplay.ui.components.ToonPlayWordmark
import com.app.toonplay.ui.components.calculateCompleteGridColumns
import com.app.toonplay.ui.state.HomeScreenUiState
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianSurface
import com.app.toonplay.ui.theme.TextOffWhite
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonVioletPrimary
import com.app.toonplay.ui.theme.ToonVioletSoft
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun DesktopHomeScreen(
  feed: HomeFeed,
  uiState: HomeScreenUiState,
  onSelectTab: (AnimeSectionTab) -> Unit,
  onSelectLanguageFilter: (LatestLanguageFilter) -> Unit,
  onToggleSectionGrid: (AnimeSectionTab, Boolean) -> Unit,
  onHeroIndexChanged: (Int) -> Unit,
  onFocusItem: (AnimeItem) -> Unit,
  onExpandDetail: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showInspector by remember { mutableStateOf(false) }
  val heroItems = feed.hero
  val heroCount = heroItems.size.coerceAtLeast(1)
  val virtualCount = if (heroItems.size > 1) heroCount * 400 else heroCount
  val initialPage =
    if (heroItems.size > 1) heroCount * 200 + (uiState.activeHeroIndex % heroCount) else 0

  val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { virtualCount })
  LaunchedEffect(pagerState, heroCount) {
    snapshotFlow { pagerState.currentPage % heroCount }
      .distinctUntilChanged()
      .collect { idx ->
        onHeroIndexChanged(idx)
        heroItems.getOrNull(idx)?.let(onFocusItem)
      }
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

  val inspectorItem =
    uiState.focusedPreviewItem ?: heroItems.firstOrNull() ?: latestList.firstOrNull()
  val homeListState = rememberLazyListState()
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
    modifier = modifier.fillMaxSize().background(ObsidianBg).testTag("desktop_home_screen"),
  ) {
    // Persistent Desktop / Windows Sidebar (224.dp)
    Surface(
      color = ObsidianSurface,
      border = BorderStroke(1.dp, BorderSubtle),
      modifier = Modifier.width(224.dp).fillMaxHeight(),
    ) {
      Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ToonPlayWordmark(
          height = 26.dp,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        )

        HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))

        ToonPlayNavItems.forEach { navItem ->
          val label = stringResource(navItem.labelRes)
          DesktopSidebarItem(
            label = label,
            iconRes = navItem.iconRes,
            selected = uiState.selectedTab == navItem.tab,
            onClick = { onSelectTab(navItem.tab) },
            testTag = "desktop_nav_${navItem.tab.name.lowercase()}",
          )
        }

        Spacer(modifier = Modifier.weight(1f))
      }
    }

    // Main Desktop Content + Slide-in Inspector
    Column(modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding()) {
      Row(
        modifier =
          Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = uiState.selectedTab.label,
          style = MaterialTheme.typography.headlineSmall,
          color = TextPrimary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        GlassSearchBar(
          query = uiState.searchQuery,
          onQueryChange = onSearchQueryChange,
          modifier = Modifier.weight(1f),
        )
        Surface(
          onClick = { showInspector = !showInspector },
          shape = RoundedCornerShape(12.dp),
          color = if (showInspector) ToonVioletSoft else ObsidianCard,
          border = BorderStroke(1.dp, if (showInspector) ToonVioletPrimary else BorderSubtle),
        ) {
          Text(
            text = if (showInspector) "Inspector On" else "Inspector",
            style = MaterialTheme.typography.labelLarge,
            color = if (showInspector) TextPrimary else TextOffWhite,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          )
        }
      }

      Row(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        val paneMode = if (uiState.searchQuery.isNotBlank()) null else uiState.selectedTab
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          Crossfade(
            targetState = paneMode,
            animationSpec = tween(durationMillis = 150),
            label = "desktop_tab_crossfade",
          ) { tab ->
            if (tab == null) {
              AnimePosterGrid(
                items = uiState.searchResults,
                savedIds = uiState.savedIds,
                onItemClick = {
                  onFocusItem(it)
                  showInspector = true
                },
                onToggleSave = onToggleSave,
                minCellWidth = 156.dp,
                enableHoverLift = true,
              )
            } else if (tab == AnimeSectionTab.HOME) {
              LazyColumn(
                state = homeListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
              ) {
                if (heroItems.isNotEmpty()) {
                  item(key = "desktop_hero", contentType = "hero") {
                    DesktopWidescreenHero(
                      heroItems = heroItems,
                      pagerState = pagerState,
                      onExplore = onExpandDetail,
                      onFocusItem = {
                        onFocusItem(it)
                        showInspector = true
                      },
                    )
                  }
                }

                // 1. Latest Episodes & Releases (HIN/TAM/TEL default, max 12 items)
                if (homepageLatest.isNotEmpty()) {
                  item(key = "desktop_latest_rail", contentType = "rail") {
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
                      )
                      if (isGrid) {
                        DesktopSectionInlineGrid(
                          items = homepageLatest,
                          savedIds = uiState.savedIds,
                          showRank = false,
                          onItemClick = {
                            onFocusItem(it)
                            showInspector = true
                          },
                          onToggleSave = onToggleSave,
                          cardWidth = 168.dp,
                        )
                      } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                          items(items = homepageLatest, key = { it.uid }) { item ->
                            AnimePosterCard(
                              item = item,
                              onClick = {
                                onFocusItem(item)
                                showInspector = true
                              },
                              width = 168.dp,
                              isSaved = item.id in uiState.savedIds,
                              onToggleSave = { onToggleSave(item) },
                              showActionFooter = true,
                              enableHoverLift = true,
                            )
                          }
                        }
                      }
                    }
                  }
                }

                // 2. Childhood Shows (max 12 items)
                if (homepageChildhood.isNotEmpty()) {
                  item(key = "desktop_childhood_rail", contentType = "rail") {
                    val isGrid = AnimeSectionTab.CHILDHOOD in uiState.gridModeTabs
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                      SectionHeaderRow(
                        title = stringResource(R.string.section_childhood_shows),
                        kicker = stringResource(R.string.section_childhood_shows_sub),
                        itemCount = homepageChildhood.size,
                        onSeeAll = { onSelectTab(AnimeSectionTab.CHILDHOOD) },
                        isGridMode = isGrid,
                        onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.CHILDHOOD, it) },
                      )
                      if (isGrid) {
                        DesktopSectionInlineGrid(
                          items = homepageChildhood,
                          savedIds = uiState.savedIds,
                          showRank = true,
                          onItemClick = {
                            onFocusItem(it)
                            showInspector = true
                          },
                          onToggleSave = onToggleSave,
                          cardWidth = 164.dp,
                        )
                      } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                          itemsIndexed(
                            items = homepageChildhood,
                            key = { _, it -> it.uid },
                          ) { idx, item ->
                            AnimePosterCard(
                              item = item,
                              onClick = {
                                onFocusItem(item)
                                showInspector = true
                              },
                              width = 164.dp,
                              isSaved = item.id in uiState.savedIds,
                              onToggleSave = { onToggleSave(item) },
                              rankBadge = idx + 1,
                              showActionFooter = true,
                              enableHoverLift = true,
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
                      homepageSeries to AnimeSectionTab.SERIES,
                    ),
                    Triple(
                      R.string.section_popular_movies,
                      R.string.section_popular_movies_sub,
                      homepageMovies to AnimeSectionTab.MOVIES,
                    ),
                  )
                railConfigs.forEach { (titleRes, subRes, pair) ->
                  val (list, tab) = pair
                  if (list.isNotEmpty()) {
                    item(key = "desktop_rail_${tab.name}", contentType = "rail") {
                      val isGrid = tab in uiState.gridModeTabs
                      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeaderRow(
                          title = stringResource(titleRes),
                          kicker = stringResource(subRes),
                          itemCount = list.size,
                          onSeeAll = { onSelectTab(tab) },
                          isGridMode = isGrid,
                          onToggleGridMode = { onToggleSectionGrid(tab, it) },
                        )
                        if (isGrid) {
                          DesktopSectionInlineGrid(
                            items = list,
                            savedIds = uiState.savedIds,
                            showRank = false,
                            onItemClick = {
                              onFocusItem(it)
                              showInspector = true
                            },
                            onToggleSave = onToggleSave,
                            cardWidth = 162.dp,
                          )
                        } else {
                          LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(items = list, key = { it.uid }) { item ->
                              AnimePosterCard(
                                item = item,
                                onClick = {
                                  onFocusItem(item)
                                  showInspector = true
                                },
                                width = 162.dp,
                                isSaved = item.id in uiState.savedIds,
                                onToggleSave = { onToggleSave(item) },
                                showActionFooter = true,
                                enableHoverLift = true,
                              )
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
                onItemClick = {
                  onFocusItem(it)
                  showInspector = true
                },
                onToggleSave = onToggleSave,
                onExploreHome = { onSelectTab(AnimeSectionTab.HOME) },
                horizontalPadding = 0.dp,
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
                onItemClick = {
                  onFocusItem(it)
                  showInspector = true
                },
                onToggleSave = onToggleSave,
                minCellWidth = 156.dp,
                enableHoverLift = true,
              )
            }
          }
        }

        AnimatedVisibility(visible = showInspector && inspectorItem != null) {
          inspectorItem?.let { current ->
            Box(modifier = Modifier.width(340.dp).fillMaxHeight().padding(bottom = 20.dp)) {
              AnimeDetailInspectorPane(
                item = current,
                isSaved = current.id in uiState.savedIds,
                onToggleSave = { onToggleSave(current) },
                onExpandFull = { onExpandDetail(current) },
                onClose = { showInspector = false },
              )
            }
          }
        }
      }
    }
  }
}

  @Composable
  private fun DesktopSectionInlineGrid(
    items: List<AnimeItem>,
    savedIds: Set<String>,
    showRank: Boolean,
    onItemClick: (AnimeItem) -> Unit,
    onToggleSave: (AnimeItem) -> Unit,
    cardWidth: androidx.compose.ui.unit.Dp,
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

      val itemSpacing = 16.dp
      val availableWidth = this.maxWidth
      val columnCount = calculateCompleteGridColumns(
        availableWidth = availableWidth,
        cardWidth = cardWidth,
        spacing = itemSpacing,
        minColumns = 1,
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
      ) {
        items.chunked(columnCount).forEachIndexed { rowIdx, row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
          ) {
            row.forEachIndexed { colIdx, item ->
              val rank = if (showRank) {
                rowIdx * columnCount + colIdx + 1
              } else {
                null
              }

              AnimePosterCard(
                item = item,
                onClick = { onItemClick(item) },
                width = cardWidth,
                isSaved = item.id in savedIds,
                onToggleSave = { onToggleSave(item) },
                rankBadge = rank,
                showActionFooter = true,
                enableHoverLift = true,
                modifier = Modifier.width(cardWidth),
              )
            }

            repeat(columnCount - row.size) {
              Spacer(modifier = Modifier.width(cardWidth))
            }
          }
        }
      }
    }
  }

@Composable
private fun DesktopSidebarItem(
  label: String,
  @DrawableRes iconRes: Int,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = if (selected) ToonVioletSoft else Color.Transparent,
    border = if (selected) BorderStroke(1.dp, ToonVioletPrimary.copy(alpha = 0.7f)) else null,
    modifier = Modifier.fillMaxWidth().testTag(testTag),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        painter = painterResource(id = iconRes),
        contentDescription = label,
        tint = if (selected) ToonVioletPrimary else TextOffWhite,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = label,
        style =
          MaterialTheme.typography.titleSmall.copy(
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
          ),
        color = if (selected) TextPrimary else TextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun DesktopWidescreenHero(
  heroItems: List<AnimeItem>,
  pagerState: androidx.compose.foundation.pager.PagerState,
  onExplore: (AnimeItem) -> Unit,
  onFocusItem: (AnimeItem) -> Unit,
) {
  val context = LocalContext.current
  val activeHero = heroItems[pagerState.currentPage % heroItems.size]
  val backdropRequest =
    remember(activeHero.backdropUrl) {
      ImageRequest.Builder(context)
        .data(activeHero.backdropUrl)
        .crossfade(false)
        .size(1024, 576)
        .build()
    }
  val bannerGradient =
    remember {
      Brush.horizontalGradient(
        0f to ObsidianBg.copy(alpha = 0.97f),
        0.52f to ObsidianBg.copy(alpha = 0.86f),
        0.78f to ObsidianBg.copy(alpha = 0.66f),
        1f to ObsidianBg.copy(alpha = 0.48f),
      )
    }

  Surface(
    shape = RoundedCornerShape(28.dp),
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
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        Column(
          modifier = Modifier.weight(0.52f),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            activeHero.rating?.let { StarRatingPill(rating = it) }
            activeHero.year?.let { MetadataPill(text = it) }
            MetadataPill(text = activeHero.typeLabel)
          }
          Text(
            text = activeHero.title,
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          if (activeHero.overview.isNotBlank()) {
            Text(
              text = activeHero.overview,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Button(
              onClick = { onExplore(activeHero) },
              colors = ButtonDefaults.buttonColors(containerColor = ToonVioletPrimary),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp),
              )
              Text(
                text = stringResource(R.string.action_explore),
                style = MaterialTheme.typography.labelLarge,
              )
            }
            CarouselDotIndicator(
              count = heroItems.size,
              currentIndex = pagerState.currentPage % heroItems.size,
            )
          }
        }

        Box(modifier = Modifier.weight(0.48f), contentAlignment = Alignment.Center) {
          FannedHeroCarousel(
            items = heroItems,
            pagerState = pagerState,
            onItemClick = onFocusItem,
            cardWidth = 166.dp,
            overlapFactor = 28f,
          )
        }
      }
    }
  }
}