package com.app.toonplay.ui.screens.desktop

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.DeviceFormFactor
import com.app.toonplay.data.model.HomeFeed
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.ui.components.AnimeDetailInspectorPane
import com.app.toonplay.ui.components.AnimePosterCard
import com.app.toonplay.ui.components.AnimePosterGrid
import com.app.toonplay.ui.components.CarouselDotIndicator
import com.app.toonplay.ui.components.FannedHeroCarousel
import com.app.toonplay.ui.components.HorizontalRailScrollControls
import com.app.toonplay.ui.components.LanguageCardsRail
import com.app.toonplay.ui.components.LatestControlsBar
import com.app.toonplay.ui.components.MetadataPill
import com.app.toonplay.ui.components.ProvideSmoothBringIntoView
import com.app.toonplay.ui.components.SearchBarTrigger
import com.app.toonplay.ui.components.SectionHeaderRow
import com.app.toonplay.ui.components.StarRatingPill
import com.app.toonplay.ui.components.ToonPlayNavItems
import com.app.toonplay.ui.components.ToonPlayProfileView
import com.app.toonplay.ui.components.ToonPlayWordmark
import com.app.toonplay.ui.components.calculateCompleteGridColumns
import com.app.toonplay.ui.components.rememberIsReducedMotion
import com.app.toonplay.ui.components.smoothMouseWheelScroll
import com.app.toonplay.ui.state.HomeFeedControlsState
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianSurface
import com.app.toonplay.ui.theme.TextOffWhite
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonVioletPrimary
import com.app.toonplay.ui.theme.ToonVioletSoft
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun DesktopHomeScreen(
  feed: HomeFeed,
  controls: HomeFeedControlsState,
  focusedPreviewFlow: StateFlow<AnimeItem?>,
  initialHeroIndex: Int,
  onSelectTab: (AnimeSectionTab) -> Unit,
  onSelectLanguageFilter: (LatestLanguageFilter) -> Unit,
  onToggleSectionGrid: (AnimeSectionTab, Boolean) -> Unit,
  onHeroIndexChanged: (Int) -> Unit,
  onFocusItem: (AnimeItem) -> Unit,
  onExpandDetail: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onToggleSearch: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showInspector by remember { mutableStateOf(false) }
  var inspectorHeroItem by remember { mutableStateOf<AnimeItem?>(null) }
  val heroItems = feed.hero
  val heroCount = heroItems.size.coerceAtLeast(1)
  val virtualCount = if (heroItems.size > 1) heroCount * 400 else heroCount
  val initialPage =
    if (heroItems.size > 1) heroCount * 200 + (initialHeroIndex % heroCount) else 0

  val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { virtualCount })
  LaunchedEffect(pagerState, heroCount) {
    snapshotFlow { pagerState.currentPage % heroCount }
      .distinctUntilChanged()
      .collect { idx ->
        onHeroIndexChanged(idx)
        inspectorHeroItem = null
      }
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
            selected = controls.selectedTab == navItem.tab,
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
          text = controls.selectedTab.label,
          style = MaterialTheme.typography.headlineSmall,
          color = TextPrimary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        SearchBarTrigger(
          onClick = { onToggleSearch(true) },
          modifier = Modifier.weight(1f),
          testTag = "desktop_search_button",
        )
        Surface(
          onClick = {
            if (showInspector) {
              showInspector = false
              inspectorHeroItem = null
            } else {
              inspectorHeroItem = null
              showInspector = true
            }
          },
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
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          ProvideSmoothBringIntoView(edgeMargin = 24.dp, isReducedMotion = isReducedMotion) {
            Crossfade(
              targetState = controls.selectedTab,
              animationSpec = tween(durationMillis = if (isReducedMotion) 0 else 150),
              label = "desktop_tab_crossfade",
            ) { tab ->
              if (tab == AnimeSectionTab.HOME) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                  val contentWidth = this.maxWidth
                  val latestCols =
                    remember(contentWidth) {
                      calculateCompleteGridColumns(contentWidth, 168.dp, 16.dp, minColumns = 1)
                    }
                  val childhoodCols =
                    remember(contentWidth) {
                      calculateCompleteGridColumns(contentWidth, 164.dp, 16.dp, minColumns = 1)
                    }
                  val railCols =
                    remember(contentWidth) {
                      calculateCompleteGridColumns(contentWidth, 162.dp, 16.dp, minColumns = 1)
                    }
                  val railConfigs =
                    remember(homepageSeries, homepageMovies) {
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
                    }

                  LazyColumn(
                    state = homeListState,
                    modifier =
                      Modifier.fillMaxSize()
                        .smoothMouseWheelScroll(homeListState, Orientation.Vertical, isReducedMotion),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.Top,
                  ) {
                    if (heroItems.isNotEmpty()) {
                      item(key = "desktop_hero", contentType = "hero") {
                        DesktopWidescreenHero(
                          heroItems = heroItems,
                          pagerState = pagerState,
                          onExplore = onExpandDetail,
                          onFocusItem = {
                            onFocusItem(it)
                            inspectorHeroItem = it
                            showInspector = true
                          },
                        )
                      }
                    }

                    // 1. Explore by Language (9 sliding 2:1 banner cards)
                    item(key = "desktop_language_cards", contentType = "language_rail") {
                      val latestItemIndex = if (heroItems.isNotEmpty()) 2 else 1
                      Box(modifier = Modifier.padding(top = if (heroItems.isNotEmpty()) 28.dp else 0.dp)) {
                        LanguageCardsRail(
                          formFactor = DeviceFormFactor.DESKTOP,
                          listState = languageRowState,
                          horizontalPadding = 0.dp,
                          itemSpacing = 10.dp,
                          sectionSpacing = 12.dp,
                          showScrollControls = true,
                          enableHoverLift = true,
                          isReducedMotion = isReducedMotion,
                          testTagPrefix = "desktop_languages",
                          onCardClick = { card ->
                            onSelectLanguageFilter(card.latestFilter)
                            scope.launch {
                              if (isReducedMotion) {
                                homeListState.scrollToItem(latestItemIndex)
                              } else {
                                homeListState.animateScrollToItem(latestItemIndex)
                              }
                            }
                          },
                        )
                      }
                    }

                    // 2. Latest Episodes & Releases (HIN/TAM/TEL default, max 12 items)
                    if (homepageLatest.isNotEmpty()) {
                      val isGrid = AnimeSectionTab.LATEST in controls.gridModeTabs
                      if (isGrid) {
                        item(key = "desktop_latest_header", contentType = "section_header") {
                          Column(
                            modifier = Modifier.padding(top = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                          ) {
                            SectionHeaderRow(
                              title = stringResource(R.string.section_latest_combined),
                              kicker = stringResource(R.string.section_latest_combined_sub),
                              itemCount = homepageLatest.size,
                            )
                            LatestControlsBar(
                              selectedFilter = controls.latestLanguageFilter,
                              onSelectFilter = onSelectLanguageFilter,
                              onSeeAll = { onSelectTab(AnimeSectionTab.LATEST) },
                              isGridMode = true,
                              onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.LATEST, it) },
                              trailingLeadingControls = null,
                            )
                          }
                        }
                        desktopInlinePosterGridRows(
                          sectionKey = "desktop_latest",
                          items = homepageLatest,
                          columns = latestCols,
                          cardWidth = 168.dp,
                          savedIds = controls.savedIds,
                          showRank = false,
                          onItemClick = onExpandDetail,
                          onToggleSave = onToggleSave,
                        )
                      } else {
                        item(key = "desktop_latest_rail", contentType = "rail") {
                          Column(
                            modifier = Modifier.padding(top = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                          ) {
                            SectionHeaderRow(
                              title = stringResource(R.string.section_latest_combined),
                              kicker = stringResource(R.string.section_latest_combined_sub),
                              itemCount = homepageLatest.size,
                            )
                            LatestControlsBar(
                              selectedFilter = controls.latestLanguageFilter,
                              onSelectFilter = onSelectLanguageFilter,
                              onSeeAll = { onSelectTab(AnimeSectionTab.LATEST) },
                              isGridMode = false,
                              onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.LATEST, it) },
                              trailingLeadingControls = {
                                HorizontalRailScrollControls(
                                  listState = latestRowState,
                                  stepDistance = 368.dp,
                                  isReducedMotion = isReducedMotion,
                                  testTagPrefix = "desktop_latest",
                                )
                              },
                            )
                            LazyRow(
                              state = latestRowState,
                              horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                                  width = 168.dp,
                                  isSaved = item.id in controls.savedIds,
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

                    // 3. Childhood Shows (max 12 items)
                    if (homepageChildhood.isNotEmpty()) {
                      val isGrid = AnimeSectionTab.CHILDHOOD in controls.gridModeTabs
                      if (isGrid) {
                        item(key = "desktop_childhood_header", contentType = "section_header") {
                          Box(modifier = Modifier.padding(top = 28.dp)) {
                            SectionHeaderRow(
                              title = stringResource(R.string.section_childhood_shows),
                              kicker = stringResource(R.string.section_childhood_shows_sub),
                              itemCount = homepageChildhood.size,
                              onSeeAll = { onSelectTab(AnimeSectionTab.CHILDHOOD) },
                              isGridMode = true,
                              onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.CHILDHOOD, it) },
                              trailingLeadingControls = null,
                            )
                          }
                        }
                        desktopInlinePosterGridRows(
                          sectionKey = "desktop_childhood",
                          items = homepageChildhood,
                          columns = childhoodCols,
                          cardWidth = 164.dp,
                          savedIds = controls.savedIds,
                          showRank = true,
                          onItemClick = onExpandDetail,
                          onToggleSave = onToggleSave,
                        )
                      } else {
                        item(key = "desktop_childhood_rail", contentType = "rail") {
                          Column(
                            modifier = Modifier.padding(top = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                          ) {
                            SectionHeaderRow(
                              title = stringResource(R.string.section_childhood_shows),
                              kicker = stringResource(R.string.section_childhood_shows_sub),
                              itemCount = homepageChildhood.size,
                              onSeeAll = { onSelectTab(AnimeSectionTab.CHILDHOOD) },
                              isGridMode = false,
                              onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.CHILDHOOD, it) },
                              trailingLeadingControls = {
                                HorizontalRailScrollControls(
                                  listState = childhoodRowState,
                                  stepDistance = 360.dp,
                                  isReducedMotion = isReducedMotion,
                                  testTagPrefix = "desktop_childhood",
                                )
                              },
                            )
                            LazyRow(
                              state = childhoodRowState,
                              horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                                  width = 164.dp,
                                  isSaved = item.id in controls.savedIds,
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

                    // 4. Most Popular TV Series & 5. Blockbuster Anime Movies (max 12 items)
                    railConfigs.forEach { (titleRes, subRes, triple) ->
                      val (list, railTab, rowState) = triple
                      if (list.isNotEmpty()) {
                        val isGrid = railTab in controls.gridModeTabs
                        if (isGrid) {
                          item(
                            key = "desktop_rail_header_${railTab.name}",
                            contentType = "section_header",
                          ) {
                            Box(modifier = Modifier.padding(top = 28.dp)) {
                              SectionHeaderRow(
                                title = stringResource(titleRes),
                                kicker = stringResource(subRes),
                                itemCount = list.size,
                                onSeeAll = { onSelectTab(railTab) },
                                isGridMode = true,
                                onToggleGridMode = { onToggleSectionGrid(railTab, it) },
                                trailingLeadingControls = null,
                              )
                            }
                          }
                          desktopInlinePosterGridRows(
                            sectionKey = "desktop_${railTab.name.lowercase()}",
                            items = list,
                            columns = railCols,
                            cardWidth = 162.dp,
                            savedIds = controls.savedIds,
                            showRank = false,
                            onItemClick = onExpandDetail,
                            onToggleSave = onToggleSave,
                          )
                        } else {
                          item(key = "desktop_rail_${railTab.name}", contentType = "rail") {
                            Column(
                              modifier = Modifier.padding(top = 28.dp),
                              verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                              SectionHeaderRow(
                                title = stringResource(titleRes),
                                kicker = stringResource(subRes),
                                itemCount = list.size,
                                onSeeAll = { onSelectTab(railTab) },
                                isGridMode = false,
                                onToggleGridMode = { onToggleSectionGrid(railTab, it) },
                                trailingLeadingControls = {
                                  HorizontalRailScrollControls(
                                    listState = rowState,
                                    stepDistance = 356.dp,
                                    isReducedMotion = isReducedMotion,
                                    testTagPrefix = "desktop_${railTab.name.lowercase()}",
                                  )
                                },
                              )
                              LazyRow(
                                state = rowState,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                                    width = 162.dp,
                                    isSaved = item.id in controls.savedIds,
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
                }
              } else if (tab == AnimeSectionTab.PROFILE) {
                ToonPlayProfileView(
                  feed = feed,
                  savedItems = controls.savedItems,
                  savedIds = controls.savedIds,
                  selectedLanguageFilter = controls.latestLanguageFilter,
                  onSelectLanguageFilter = onSelectLanguageFilter,
                  onItemClick = onExpandDetail,
                  onToggleSave = onToggleSave,
                  onExploreHome = { onSelectTab(AnimeSectionTab.HOME) },
                  horizontalPadding = 0.dp,
                  bottomPadding = 32.dp,
                  enableHoverLift = true,
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
                    AnimeSectionTab.SAVED -> controls.savedItems
                    AnimeSectionTab.HOME,
                    AnimeSectionTab.PROFILE -> emptyList()
                  }
                AnimePosterGrid(
                  items = items,
                  savedIds = controls.savedIds,
                  onItemClick = onExpandDetail,
                  onToggleSave = onToggleSave,
                  minCellWidth = 156.dp,
                  enableHoverLift = true,
                )
              }
            }
          }
        }

        AnimatedVisibility(visible = showInspector) {
          DesktopInspectorSlot(
            selectedHeroItem = inspectorHeroItem,
            heroItems = heroItems,
            pagerState = pagerState,
            fallbackLatestItem = latestList.firstOrNull(),
            savedIds = controls.savedIds,
            onToggleSave = onToggleSave,
            onExpandFull = onExpandDetail,
            onClose = {
              showInspector = false
              inspectorHeroItem = null
            },
          )
        }
      }
    }
  }
}

@Composable
private fun DesktopInspectorSlot(
  selectedHeroItem: AnimeItem?,
  heroItems: List<AnimeItem>,
  pagerState: androidx.compose.foundation.pager.PagerState,
  fallbackLatestItem: AnimeItem?,
  savedIds: Set<String>,
  onToggleSave: (AnimeItem) -> Unit,
  onExpandFull: (AnimeItem) -> Unit,
  onClose: () -> Unit,
) {
  val activeHeroIndex by remember(heroItems, pagerState) {
    derivedStateOf {
      if (heroItems.isEmpty()) 0 else pagerState.currentPage % heroItems.size
    }
  }
  val current = selectedHeroItem ?: heroItems.getOrNull(activeHeroIndex) ?: fallbackLatestItem ?: return
  Box(modifier = Modifier.width(340.dp).fillMaxHeight().padding(bottom = 20.dp)) {
    AnimeDetailInspectorPane(
      item = current,
      isSaved = current.id in savedIds,
      onToggleSave = { onToggleSave(current) },
      onExpandFull = { onExpandFull(current) },
      onClose = onClose,
    )
  }
}

private fun LazyListScope.desktopInlinePosterGridRows(
  sectionKey: String,
  items: List<AnimeItem>,
  columns: Int,
  cardWidth: Dp,
  savedIds: Set<String>,
  showRank: Boolean,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
) {
  val itemSpacing = 16.dp
  val safeColumns = columns.coerceAtLeast(1)
  val rowCount = (items.size + safeColumns - 1) / safeColumns
  items(
    count = rowCount,
    key = { rowIdx ->
      val start = rowIdx * safeColumns
      val end = minOf(start + safeColumns, items.size)
      val rowIds =
        buildString {
          for (i in start until end) {
            if (i > start) append('_')
            append(items[i].uid)
          }
        }
      "${sectionKey}_grid_r${safeColumns}_${rowIdx}_$rowIds"
    },
    contentType = { "desktop_inline_grid_row" },
  ) { rowIdx ->
    val start = rowIdx * safeColumns
    val end = minOf(start + safeColumns, items.size)
    val topPad = if (rowIdx == 0) 12.dp else itemSpacing
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = topPad),
      horizontalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
      for (idx in start until end) {
        val item = items[idx]
        AnimePosterCard(
          item = item,
          onClick = { onItemClick(item) },
          width = cardWidth,
          isSaved = item.id in savedIds,
          onToggleSave = { onToggleSave(item) },
          rankBadge = if (showRank) idx + 1 else null,
          showActionFooter = true,
          enableHoverLift = true,
          modifier = Modifier.width(cardWidth),
        )
      }
      repeat(safeColumns - (end - start)) {
        Spacer(modifier = Modifier.width(cardWidth))
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
  val currentHeroIndex by remember(heroItems, pagerState) {
    derivedStateOf { pagerState.currentPage % heroItems.size }
  }
  val activeHero = heroItems[currentHeroIndex]
  val backdropUrl = activeHero.backdropUrl.ifBlank { activeHero.posterUrl }
  val backdropRequest =
    remember(backdropUrl) {
      ImageRequest.Builder(context)
        .data(backdropUrl)
        .crossfade(false)
        .size(1024, 576)
        .precision(Precision.INEXACT)
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
              currentIndex = currentHeroIndex,
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