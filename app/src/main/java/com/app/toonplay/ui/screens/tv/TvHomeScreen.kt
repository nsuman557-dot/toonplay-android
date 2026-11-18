package com.app.toonplay.ui.screens.tv

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewCarousel
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.DeviceFormFactor
import com.app.toonplay.data.model.HomeFeed
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.ui.components.AnimePosterGrid
import com.app.toonplay.ui.components.BlinkingGreenDot
import com.app.toonplay.ui.components.CarouselDotIndicator
import com.app.toonplay.ui.components.FannedHeroCarousel
import com.app.toonplay.ui.components.LanguageCardsRail
import com.app.toonplay.ui.components.MetadataPill
import com.app.toonplay.ui.components.ProvideSmoothBringIntoView
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
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.EmeraldLive
import com.app.toonplay.ui.theme.IndigoBadgeDark
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.ObsidianGlass
import com.app.toonplay.ui.theme.ObsidianSurface
import com.app.toonplay.ui.theme.TextOffWhite
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonVioletPrimary
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun TvHomeScreen(
  feed: HomeFeed,
  controls: HomeFeedControlsState,
  focusedPreviewFlow: StateFlow<AnimeItem?>,
  initialHeroIndex: Int,
  onSelectTab: (AnimeSectionTab) -> Unit,
  onSelectLanguageFilter: (LatestLanguageFilter) -> Unit,
  onToggleSectionGrid: (AnimeSectionTab, Boolean) -> Unit,
  onHeroIndexChanged: (Int) -> Unit,
  onFocusItem: (AnimeItem) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onToggleSearch: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
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
        heroItems.getOrNull(idx)?.let(onFocusItem)
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
  val isReducedMotion = rememberIsReducedMotion()
  val coroutineScope = rememberCoroutineScope()
  val animeCatalog =
    remember(feed) {
      (feed.popularSeries + feed.globalLatest + feed.latestReleases).distinctBy { it.id }
    }
  val cartoonCatalog =
    remember(feed) {
      (feed.childhoodShows + feed.latestReleases.filter { it.typeLabel == "SHORT" })
        .distinctBy { it.id }
    }

  Box(modifier = modifier.fillMaxSize().background(ObsidianBg).testTag("tv_home_screen")) {
    Row(modifier = Modifier.fillMaxSize()) {
      // Compact 10-foot Left TV Navigation Rail
      Surface(
        color = ObsidianSurface,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.width(92.dp).fillMaxHeight(),
      ) {
        Column(
          modifier = Modifier.fillMaxSize().padding(vertical = 24.dp, horizontal = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          ToonPlayNavItems.forEach { navItem ->
            val label = stringResource(navItem.labelRes)
            TvSideRailIcon(
              iconRes = navItem.iconRes,
              label = label,
              selected = controls.selectedTab == navItem.tab,
              isReducedMotion = isReducedMotion,
              onClick = {
                if (navItem.tab == AnimeSectionTab.HOME && controls.selectedTab == AnimeSectionTab.HOME) {
                  coroutineScope.launch {
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
              testTag = "tv_tab_${navItem.tab.name.lowercase()}",
            )
          }
          Spacer(modifier = Modifier.weight(1f))
          TvSideRailIcon(
            iconVector = Icons.Rounded.Search,
            label = "Search",
            selected = false,
            isReducedMotion = isReducedMotion,
            onClick = { onToggleSearch(true) },
            testTag = "tv_search_button",
          )
        }
      }

      // Main 10-foot TV Canvas
      Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 42.dp, vertical = 18.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          ToonPlayWordmark(height = 30.dp)
          if (controls.selectedTab != AnimeSectionTab.HOME) {
            Text(
              text = controls.selectedTab.label,
              style = MaterialTheme.typography.headlineSmall,
              color = TextOffWhite,
            )
          }
        }

        ProvideSmoothBringIntoView(edgeMargin = 42.dp, isReducedMotion = isReducedMotion) {
          Crossfade(
            targetState = controls.selectedTab,
            animationSpec = tween(durationMillis = if (isReducedMotion) 0 else 150),
            label = "tv_tab_crossfade",
          ) { tab ->
            if (tab == AnimeSectionTab.HOME) {
              BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val contentWidth = (this.maxWidth - 84.dp).coerceAtLeast(0.dp)
                val tvGridColumns =
                  remember(contentWidth) {
                    calculateCompleteGridColumns(contentWidth, 176.dp, 18.dp, minColumns = 1)
                  }
                LazyColumn(
                  state = homeListState,
                  modifier =
                    Modifier.fillMaxSize()
                      .smoothMouseWheelScroll(homeListState, Orientation.Vertical, isReducedMotion),
                  contentPadding = PaddingValues(top = 14.dp, bottom = 40.dp),
                  verticalArrangement = Arrangement.Top,
                ) {
                  // 1. Bidirectional TV Hero Showcase (no arrow buttons)
                  if (heroItems.isNotEmpty()) {
                    item(key = "tv_spotlight_banner", contentType = "tv_hero") {
                      TvHeroSpotlightSlot(
                        focusedPreviewFlow = focusedPreviewFlow,
                        heroItems = heroItems,
                        fallbackItem = heroItems.first(),
                        pagerState = pagerState,
                        isReducedMotion = isReducedMotion,
                        onExplore = onItemClick,
                        onFocusItem = onFocusItem,
                      )
                    }
                  }

                  // 2. Explore by Language (9 sliding 2:1 banner cards)
                  item(key = "tv_language_cards", contentType = "tv_language_rail") {
                    val latestItemIndex = if (heroItems.isNotEmpty()) 2 else 1
                    Box(modifier = Modifier.padding(top = if (heroItems.isNotEmpty()) 28.dp else 0.dp)) {
                      LanguageCardsRail(
                        formFactor = DeviceFormFactor.TV,
                        listState = languageRowState,
                        horizontalPadding = 42.dp,
                        itemSpacing = 10.dp,
                        sectionSpacing = 14.dp,
                        isTvMode = true,
                        isReducedMotion = isReducedMotion,
                        testTagPrefix = "tv_languages",
                        onCardClick = { card ->
                          onSelectLanguageFilter(card.latestFilter)
                          coroutineScope.launch {
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

                  // 3. Latest Episodes & Releases (HIN/TAM/TEL default, max 12 items)
                  if (homepageLatest.isNotEmpty()) {
                    val isGrid = AnimeSectionTab.LATEST in controls.gridModeTabs
                    if (isGrid) {
                      item(key = "tv_latest_header", contentType = "tv_section_header") {
                        Row(
                          modifier =
                            Modifier.fillMaxWidth()
                              .padding(start = 42.dp, end = 42.dp, top = 28.dp),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically,
                        ) {
                          Column {
                            Text(
                              text =
                                "${stringResource(R.string.section_latest_combined_sub)}  •  ${homepageLatest.size} titles",
                              style = MaterialTheme.typography.labelLarge,
                              color = TextSecondary,
                            )
                            Text(
                              text = stringResource(R.string.section_latest_combined),
                              style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
                              color = TextPrimary,
                            )
                          }
                          Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                          ) {
                            TvLanguageTogglePill(
                              selectedFilter = controls.latestLanguageFilter,
                              onSelectFilter = onSelectLanguageFilter,
                            )
                            TvViewModeTogglePill(
                              isGridMode = true,
                              onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.LATEST, it) },
                            )
                            TvSeeAllChip(
                              isReducedMotion = isReducedMotion,
                              onClick = { onSelectTab(AnimeSectionTab.LATEST) },
                              testTag = "tv_see_all_latest",
                            )
                          }
                        }
                      }
                      inlinePosterGridRows(
                        sectionKey = "tv_latest",
                        items = homepageLatest,
                        columns = tvGridColumns,
                        cardWidth = 176.dp,
                        itemSpacing = 18.dp,
                        horizontalPadding = 42.dp,
                        firstRowTopPadding = 14.dp,
                        rowKeyPrefix = { it.uid },
                      ) { _, item, cellModifier ->
                        TvFocusablePosterCard(
                          item = item,
                          rank = null,
                          isSaved = item.id in controls.savedIds,
                          onFocus = { onFocusItem(item) },
                          onClick = { onItemClick(item) },
                          onToggleSave = { onToggleSave(item) },
                          modifier = cellModifier,
                          cardWidth = null,
                          isReducedMotion = isReducedMotion,
                        )
                      }
                    } else {
                      item(key = "tv_latest_rail", contentType = "tv_rail") {
                        Column(
                          modifier = Modifier.padding(top = 28.dp),
                          verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                          Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 42.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                          ) {
                            Column {
                              Text(
                                text =
                                  "${stringResource(R.string.section_latest_combined_sub)}  •  ${homepageLatest.size} titles",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary,
                              )
                              Text(
                                text = stringResource(R.string.section_latest_combined),
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
                                color = TextPrimary,
                              )
                            }
                            Row(
                              horizontalArrangement = Arrangement.spacedBy(10.dp),
                              verticalAlignment = Alignment.CenterVertically,
                            ) {
                              TvLanguageTogglePill(
                                selectedFilter = controls.latestLanguageFilter,
                                onSelectFilter = onSelectLanguageFilter,
                              )
                              TvViewModeTogglePill(
                                isGridMode = false,
                                onToggleGridMode = {
                                  onToggleSectionGrid(AnimeSectionTab.LATEST, it)
                                },
                              )
                              TvSeeAllChip(
                                isReducedMotion = isReducedMotion,
                                onClick = { onSelectTab(AnimeSectionTab.LATEST) },
                                testTag = "tv_see_all_latest",
                              )
                            }
                          }
                          TvPosterLazyRow(
                            items = homepageLatest,
                            savedIds = controls.savedIds,
                            showRank = false,
                            listState = latestRowState,
                            isReducedMotion = isReducedMotion,
                            onFocusItem = onFocusItem,
                            onItemClick = onItemClick,
                            onToggleSave = onToggleSave,
                          )
                        }
                      }
                    }
                  }

                  // 4. Childhood Shows (max 12 items)
                  if (homepageChildhood.isNotEmpty()) {
                    tvHomeSection(
                      sectionKey = "tv_childhood",
                      titleRes = R.string.section_childhood_shows,
                      kickerRes = R.string.section_childhood_shows_sub,
                      items = homepageChildhood,
                      savedIds = controls.savedIds,
                      showRank = true,
                      isGridMode = AnimeSectionTab.CHILDHOOD in controls.gridModeTabs,
                      tvGridColumns = tvGridColumns,
                      listState = childhoodRowState,
                      isReducedMotion = isReducedMotion,
                      onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.CHILDHOOD, it) },
                      onSeeAll = { onSelectTab(AnimeSectionTab.CHILDHOOD) },
                      onFocusItem = onFocusItem,
                      onItemClick = onItemClick,
                      onToggleSave = onToggleSave,
                    )
                  }

                  // 5. Most Popular TV Series (max 12 items)
                  if (homepageSeries.isNotEmpty()) {
                    tvHomeSection(
                      sectionKey = "tv_series",
                      titleRes = R.string.section_popular_series,
                      kickerRes = R.string.section_popular_series_sub,
                      items = homepageSeries,
                      savedIds = controls.savedIds,
                      showRank = false,
                      isGridMode = AnimeSectionTab.SERIES in controls.gridModeTabs,
                      tvGridColumns = tvGridColumns,
                      listState = seriesRowState,
                      isReducedMotion = isReducedMotion,
                      onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.SERIES, it) },
                      onSeeAll = { onSelectTab(AnimeSectionTab.SERIES) },
                      onFocusItem = onFocusItem,
                      onItemClick = onItemClick,
                      onToggleSave = onToggleSave,
                    )
                  }

                  // 6. Blockbuster Anime Movies (max 12 items)
                  if (homepageMovies.isNotEmpty()) {
                    tvHomeSection(
                      sectionKey = "tv_movies",
                      titleRes = R.string.section_popular_movies,
                      kickerRes = R.string.section_popular_movies_sub,
                      items = homepageMovies,
                      savedIds = controls.savedIds,
                      showRank = false,
                      isGridMode = AnimeSectionTab.MOVIES in controls.gridModeTabs,
                      tvGridColumns = tvGridColumns,
                      listState = moviesRowState,
                      isReducedMotion = isReducedMotion,
                      onToggleGridMode = { onToggleSectionGrid(AnimeSectionTab.MOVIES, it) },
                      onSeeAll = { onSelectTab(AnimeSectionTab.MOVIES) },
                      onFocusItem = onFocusItem,
                      onItemClick = onItemClick,
                      onToggleSave = onToggleSave,
                    )
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
                horizontalPadding = 42.dp,
                bottomPadding = 36.dp,
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
              Box(modifier = Modifier.fillMaxSize()) {
                AnimePosterGrid(
                  items = items,
                  savedIds = controls.savedIds,
                  onItemClick = onItemClick,
                  onToggleSave = onToggleSave,
                  minCellWidth = 176.dp,
                  horizontalPadding = 42.dp,
                  itemSpacing = 18.dp,
                  verticalSpacing = 18.dp,
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

@Composable
private fun TvHeroSpotlightSlot(
  focusedPreviewFlow: StateFlow<AnimeItem?>,
  heroItems: List<AnimeItem>,
  fallbackItem: AnimeItem,
  pagerState: androidx.compose.foundation.pager.PagerState,
  isReducedMotion: Boolean,
  onExplore: (AnimeItem) -> Unit,
  onFocusItem: (AnimeItem) -> Unit,
) {
  val focusedPreview by focusedPreviewFlow.collectAsStateWithLifecycle()
  val spotlight = focusedPreview ?: heroItems.firstOrNull() ?: fallbackItem
  TvHeroSpotlightShowcase(
    spotlight = spotlight,
    heroItems = heroItems,
    pagerState = pagerState,
    isReducedMotion = isReducedMotion,
    onExplore = onExplore,
    onFocusItem = onFocusItem,
  )
}

private fun LazyListScope.tvHomeSection(
  sectionKey: String,
  titleRes: Int,
  kickerRes: Int,
  items: List<AnimeItem>,
  savedIds: Set<String>,
  showRank: Boolean,
  isGridMode: Boolean,
  tvGridColumns: Int,
  listState: LazyListState,
  isReducedMotion: Boolean,
  onToggleGridMode: (Boolean) -> Unit,
  onSeeAll: () -> Unit,
  onFocusItem: (AnimeItem) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
) {
  if (isGridMode) {
    item(key = "${sectionKey}_header", contentType = "tv_section_header") {
      val title = stringResource(titleRes)
      val kicker = "${stringResource(kickerRes)}  •  ${items.size} titles"
      TvSectionHeader(
        title = title,
        kicker = kicker,
        isGridMode = true,
        isReducedMotion = isReducedMotion,
        onToggleGridMode = onToggleGridMode,
        onSeeAll = onSeeAll,
        modifier = Modifier.padding(top = 28.dp),
      )
    }
    inlinePosterGridRows(
      sectionKey = sectionKey,
      items = items,
      columns = tvGridColumns,
      cardWidth = 176.dp,
      itemSpacing = 18.dp,
      horizontalPadding = 42.dp,
      firstRowTopPadding = 14.dp,
      rowKeyPrefix = { it.uid },
    ) { flatIndex, item, cellModifier ->
      TvFocusablePosterCard(
        item = item,
        rank = if (showRank) flatIndex + 1 else null,
        isSaved = item.id in savedIds,
        onFocus = { onFocusItem(item) },
        onClick = { onItemClick(item) },
        onToggleSave = { onToggleSave(item) },
        modifier = cellModifier,
        cardWidth = null,
        isReducedMotion = isReducedMotion,
      )
    }
  } else {
    item(key = "${sectionKey}_rail", contentType = "tv_rail") {
      val title = stringResource(titleRes)
      val kicker = "${stringResource(kickerRes)}  •  ${items.size} titles"
      TvSectionRail(
        title = title,
        kicker = kicker,
        items = items,
        savedIds = savedIds,
        showRank = showRank,
        isGridMode = false,
        listState = listState,
        isReducedMotion = isReducedMotion,
        onToggleGridMode = onToggleGridMode,
        onSeeAll = onSeeAll,
        onFocusItem = onFocusItem,
        onItemClick = onItemClick,
        onToggleSave = onToggleSave,
        modifier = Modifier.padding(top = 28.dp),
      )
    }
  }
}

@Composable
private fun TvSideRailIcon(
  label: String,
  selected: Boolean,
  isReducedMotion: Boolean,
  onClick: () -> Unit,
  testTag: String,
  @DrawableRes iconRes: Int? = null,
  iconVector: ImageVector? = null,
) {
  var isFocused by remember { mutableStateOf(false) }
  val scale by
  animateFloatAsState(
    targetValue = if (isFocused) 1.08f else 1f,
    animationSpec = if (isReducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
    label = "tv_rail_icon_scale",
  )

  val contentTint =
    when {
      isFocused -> ObsidianBg
      selected -> TextPrimary
      else -> TextOffWhite
    }

  Surface(
    modifier =
      Modifier.fillMaxWidth()
        .height(56.dp)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .onFocusChanged { isFocused = it.isFocused }
        .focusable()
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    color =
      when {
        isFocused -> TextPrimary
        selected -> ToonVioletPrimary
        else -> ObsidianCard
      },
    border =
      BorderStroke(
        width = if (isFocused) 2.dp else 1.dp,
        color = if (isFocused) ToonVioletPrimary else BorderSubtle,
      ),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      if (iconRes != null) {
        Icon(
          painter = painterResource(id = iconRes),
          contentDescription = label,
          tint = contentTint,
          modifier = Modifier.size(21.dp),
        )
      } else if (iconVector != null) {
        Icon(
          imageVector = iconVector,
          contentDescription = label,
          tint = contentTint,
          modifier = Modifier.size(21.dp),
        )
      }
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = label,
        style =
          MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
          ),
        color = contentTint,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun TvFocusableChip(
  label: String,
  selected: Boolean,
  isReducedMotion: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  var isFocused by remember { mutableStateOf(false) }
  val scale by
  animateFloatAsState(
    targetValue = if (isFocused) 1.07f else 1f,
    animationSpec = if (isReducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
    label = "tv_chip_scale",
  )

  Surface(
    modifier =
      Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
        .onFocusChanged { isFocused = it.isFocused }
        .focusable()
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    color =
      when {
        isFocused -> TextPrimary
        selected -> ToonVioletPrimary
        else -> ObsidianCard
      },
    border =
      BorderStroke(
        width = if (isFocused) 2.dp else 1.dp,
        color = if (isFocused) ToonVioletPrimary else BorderSubtle,
      ),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
      color =
        when {
          isFocused -> ObsidianBg
          selected -> TextPrimary
          else -> TextOffWhite
        },
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
    )
  }
}

@Composable
private fun TvSeeAllChip(
  isReducedMotion: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  var isFocused by remember { mutableStateOf(false) }
  val scale by
  animateFloatAsState(
    targetValue = if (isFocused) 1.07f else 1f,
    animationSpec = if (isReducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
    label = "tv_see_all_scale",
  )

  Surface(
    modifier =
      Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
        .onFocusChanged { isFocused = it.isFocused }
        .focusable()
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    color = if (isFocused) TextPrimary else ObsidianCard,
    border =
      BorderStroke(
        width = if (isFocused) 2.dp else 1.dp,
        color = if (isFocused) ToonVioletPrimary else BorderSubtle,
      ),
  ) {
    Row(
      modifier = Modifier.padding(start = 11.dp, end = 10.dp, top = 7.dp, bottom = 7.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Text(
        text = stringResource(R.string.action_see_all),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = if (isFocused) ObsidianBg else TextOffWhite,
      )
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        tint = if (isFocused) ObsidianBg else TextOffWhite,
        modifier = Modifier.size(14.dp),
      )
    }
  }
}

@Composable
private fun TvLanguageTogglePill(
  selectedFilter: LatestLanguageFilter,
  onSelectFilter: (LatestLanguageFilter) -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = ObsidianSurface,
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Row(
      modifier = Modifier.padding(3.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      LatestLanguageFilter.entries.forEach { filter ->
        val selected = filter == selectedFilter
        var isFocused by remember { mutableStateOf(false) }
        Surface(
          modifier =
            Modifier.height(36.dp)
              .onFocusChanged { isFocused = it.isFocused }
              .focusable()
              .clip(RoundedCornerShape(9.dp))
              .clickable { onSelectFilter(filter) }
              .testTag("tv_lang_${filter.name.lowercase()}"),
          shape = RoundedCornerShape(9.dp),
          color =
            when {
              isFocused -> TextPrimary
              selected -> ToonVioletPrimary
              else -> Color.Transparent
            },
          border = if (isFocused) BorderStroke(2.dp, ToonVioletPrimary) else null,
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
          ) {
            Text(
              text = filter.label,
              style =
                MaterialTheme.typography.labelLarge.copy(
                  fontWeight = if (selected || isFocused) FontWeight.Bold else FontWeight.SemiBold,
                ),
              color =
                when {
                  isFocused -> ObsidianBg
                  selected -> TextPrimary
                  else -> TextOffWhite
                },
              maxLines = 1,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TvViewModeTogglePill(
  isGridMode: Boolean,
  onToggleGridMode: (Boolean) -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = ObsidianSurface,
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Row(
      modifier = Modifier.padding(3.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      var gridFocused by remember { mutableStateOf(false) }
      Surface(
        modifier =
          Modifier.size(36.dp)
            .onFocusChanged { gridFocused = it.isFocused }
            .focusable()
            .clip(RoundedCornerShape(9.dp))
            .clickable { onToggleGridMode(true) },
        shape = RoundedCornerShape(9.dp),
        color =
          when {
            gridFocused -> TextPrimary
            isGridMode -> ToonVioletPrimary
            else -> Color.Transparent
          },
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Rounded.GridView,
            contentDescription = "Grid view",
            tint =
              when {
                gridFocused -> ObsidianBg
                isGridMode -> TextPrimary
                else -> TextOffWhite
              },
            modifier = Modifier.size(18.dp),
          )
        }
      }

      var scrollFocused by remember { mutableStateOf(false) }
      Surface(
        modifier =
          Modifier.size(36.dp)
            .onFocusChanged { scrollFocused = it.isFocused }
            .focusable()
            .clip(RoundedCornerShape(9.dp))
            .clickable { onToggleGridMode(false) },
        shape = RoundedCornerShape(9.dp),
        color =
          when {
            scrollFocused -> TextPrimary
            !isGridMode -> ToonVioletPrimary
            else -> Color.Transparent
          },
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Rounded.ViewCarousel,
            contentDescription = "Scroll view",
            tint =
              when {
                scrollFocused -> ObsidianBg
                !isGridMode -> TextPrimary
                else -> TextOffWhite
              },
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun TvHeroSpotlightShowcase(
  spotlight: AnimeItem,
  heroItems: List<AnimeItem>,
  pagerState: androidx.compose.foundation.pager.PagerState,
  isReducedMotion: Boolean,
  onExplore: (AnimeItem) -> Unit,
  onFocusItem: (AnimeItem) -> Unit,
) {
  val context = LocalContext.current
  val currentHeroIndex by remember(heroItems, pagerState) {
    derivedStateOf { pagerState.currentPage % heroItems.size }
  }
  val tvBannerGradient =
    remember {
      Brush.horizontalGradient(
        0f to ObsidianBg.copy(alpha = 0.97f),
        0.54f to ObsidianBg.copy(alpha = 0.86f),
        0.78f to ObsidianBg.copy(alpha = 0.66f),
        1f to ObsidianBg.copy(alpha = 0.48f),
      )
    }
  Surface(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 42.dp).height(310.dp),
    shape = RoundedCornerShape(26.dp),
    color = ObsidianCard,
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Crossfade(
        targetState = spotlight.backdropUrl.ifBlank { spotlight.posterUrl },
        animationSpec = tween(if (isReducedMotion) 0 else 220),
        label = "tv_hero_img",
      ) { url ->
        val request =
          remember(url) {
            ImageRequest.Builder(context)
              .data(url)
              .crossfade(false)
              .size(1024, 576)
              .precision(Precision.INEXACT)
              .build()
          }
        AsyncImage(
          model = request,
          contentDescription = spotlight.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }

      Box(modifier = Modifier.fillMaxSize().background(tvBannerGradient))

      Row(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        Column(
          modifier = Modifier.weight(0.52f),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            spotlight.rating?.let { StarRatingPill(rating = it) }
            spotlight.year?.let { MetadataPill(text = it) }
            MetadataPill(text = spotlight.typeLabel)
          }
          Text(
            text = spotlight.title,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          if (spotlight.overview.isNotBlank()) {
            Text(
              text = spotlight.overview,
              style = MaterialTheme.typography.bodyLarge,
              color = TextSecondary,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            TvFocusableChip(
              label = "Watch / Explore",
              selected = true,
              isReducedMotion = isReducedMotion,
              onClick = { onExplore(spotlight) },
              testTag = "tv_hero_explore",
            )
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
            cardWidth = 152.dp,
            overlapFactor = 26f,
          )
        }
      }
    }
  }
}

@Composable
private fun TvSectionHeader(
  title: String,
  kicker: String,
  isGridMode: Boolean,
  isReducedMotion: Boolean,
  onToggleGridMode: (Boolean) -> Unit,
  onSeeAll: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 42.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column {
      Text(
        text = kicker,
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondary,
      )
      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
        color = TextPrimary,
      )
    }
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TvViewModeTogglePill(
        isGridMode = isGridMode,
        onToggleGridMode = onToggleGridMode,
      )
      TvSeeAllChip(
        isReducedMotion = isReducedMotion,
        onClick = onSeeAll,
        testTag = "tv_see_all_${title.lowercase().replace(" ", "_")}",
      )
    }
  }
}

@Composable
private fun TvSectionRail(
  title: String,
  kicker: String,
  items: List<AnimeItem>,
  savedIds: Set<String>,
  showRank: Boolean,
  isGridMode: Boolean,
  listState: LazyListState,
  isReducedMotion: Boolean,
  onToggleGridMode: (Boolean) -> Unit,
  onSeeAll: () -> Unit,
  onFocusItem: (AnimeItem) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
    TvSectionHeader(
      title = title,
      kicker = kicker,
      isGridMode = isGridMode,
      isReducedMotion = isReducedMotion,
      onToggleGridMode = onToggleGridMode,
      onSeeAll = onSeeAll,
    )

    TvPosterLazyRow(
      items = items,
      savedIds = savedIds,
      showRank = showRank,
      listState = listState,
      isReducedMotion = isReducedMotion,
      onFocusItem = onFocusItem,
      onItemClick = onItemClick,
      onToggleSave = onToggleSave,
    )
  }
}

@Composable
private fun TvPosterLazyRow(
  items: List<AnimeItem>,
  savedIds: Set<String>,
  showRank: Boolean,
  listState: LazyListState,
  isReducedMotion: Boolean,
  onFocusItem: (AnimeItem) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
) {
  LazyRow(
    state = listState,
    modifier =
      Modifier.fillMaxWidth()
        .smoothMouseWheelScroll(listState, Orientation.Horizontal, isReducedMotion),
    contentPadding = PaddingValues(horizontal = 42.dp),
    horizontalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    itemsIndexed(
      items = items,
      key = { _, it -> it.uid },
      contentType = { _, _ -> "tv_poster_card" },
    ) { index, item ->
      TvFocusablePosterCard(
        item = item,
        rank = if (showRank) index + 1 else null,
        isSaved = item.id in savedIds,
        onFocus = { onFocusItem(item) },
        onClick = { onItemClick(item) },
        onToggleSave = { onToggleSave(item) },
        isReducedMotion = isReducedMotion,
      )
    }
  }
}

private val TvCardShape = RoundedCornerShape(16.dp)
private val TvBadgeShape = RoundedCornerShape(8.dp)
private val TvFooterBtnShape = RoundedCornerShape(9.dp)
private val TvCardOverlayGradient =
  Brush.verticalGradient(
    0f to Color.Black.copy(alpha = 0.24f),
    0.38f to Color.Transparent,
    0.68f to ObsidianBg.copy(alpha = 0.78f),
    0.81f to ObsidianCard.copy(alpha = 0.86f),
    0.92f to ObsidianBg.copy(alpha = 0.93f),
    1f to ObsidianBg.copy(alpha = 0.97f),
  )

@Composable
private fun TvFocusablePosterCard(
  item: AnimeItem,
  rank: Int?,
  isSaved: Boolean,
  onFocus: () -> Unit,
  onClick: () -> Unit,
  onToggleSave: () -> Unit,
  modifier: Modifier = Modifier,
  cardWidth: Dp? = 176.dp,
  isReducedMotion: Boolean = false,
) {
  val context = LocalContext.current
  val imageUrl = item.posterUrl.ifBlank { item.backdropUrl }
  val imageRequest =
    remember(imageUrl) {
      ImageRequest.Builder(context)
        .data(imageUrl)
        .crossfade(false)
        .size(380, 480)
        .precision(Precision.INEXACT)
        .build()
    }
  var isFocused by remember { mutableStateOf(false) }
  val scale by
  animateFloatAsState(
    targetValue = if (isFocused) 1.06f else 1f,
    animationSpec = if (isReducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
    label = "tv_card_scale",
  )

  Box(
    modifier =
      modifier
        .then(if (cardWidth != null) Modifier.width(cardWidth) else Modifier.fillMaxWidth())
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .onFocusChanged { state ->
          isFocused = state.isFocused
          if (state.isFocused) onFocus()
        }
        .focusable()
        .clip(TvCardShape)
        .background(ObsidianCard)
        .border(
          width = if (isFocused) 2.5.dp else 1.dp,
          color = if (isFocused) ToonVioletPrimary else BorderSubtle,
          shape = TvCardShape,
        )
        .clickable(onClick = onClick)
        .testTag("tv_card_${item.id}"),
  ) {
    val posterPlaceholder = painterResource(id = R.drawable.poster_placeholder)
    AsyncImage(
      model = imageRequest,
      contentDescription = item.title,
      placeholder = posterPlaceholder,
      error = posterPlaceholder,
      fallback = posterPlaceholder,
      contentScale = ContentScale.Crop,
      modifier = Modifier.matchParentSize(),
    )
    Box(modifier = Modifier.matchParentSize().background(TvCardOverlayGradient))

    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.79f),
      ) {
        // Top-right Type pill
        Row(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .padding(8.dp)
              .clip(TvBadgeShape)
              .background(ObsidianGlass)
              .border(0.8.dp, BorderSubtle, TvBadgeShape)
              .padding(horizontal = 7.dp, vertical = 3.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.LiveTv,
            contentDescription = null,
            tint = TextOffWhite,
            modifier = Modifier.size(11.dp),
          )
          Text(
            text = item.typeLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = TextOffWhite,
          )
        }

        // Top-left Rank / Episode pill (ratings reserved exclusively for hero cards)
        val topLeftText =
          when {
            rank != null -> "#$rank"
            item.episodeLabel != null -> item.episodeLabel
            else -> null
          }
        if (topLeftText != null) {
          val isEpBadge = rank == null && item.episodeLabel != null
          val badgeColor = if (isEpBadge) IndigoBadgeDark else ObsidianGlass
          val badgeBorderColor =
            if (isEpBadge) ToonVioletPrimary.copy(alpha = 0.45f) else BorderSubtle
          Box(
            modifier =
              Modifier.align(Alignment.TopStart)
                .padding(8.dp)
                .clip(TvBadgeShape)
                .background(badgeColor)
                .border(0.8.dp, badgeBorderColor, TvBadgeShape)
                .padding(horizontal = 7.dp, vertical = 3.dp),
          ) {
            Text(
              text = topLeftText,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary,
            )
          }
        }

        val isLatestEpisodeCard = item.sectionKey == "latest" || item.sectionKey == "global"
        Column(
          modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 10.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          val metaLine =
            remember(item.seasonLabel, item.episodeLabel, item.year) {
              listOfNotNull(
                item.seasonLabel,
                item.episodeLabel,
                if (item.seasonLabel == null && item.episodeLabel == null) item.year else null,
              )
                .joinToString("  •  ")
            }
          if (metaLine.isNotBlank()) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              if (isLatestEpisodeCard) {
                BlinkingGreenDot()
              }
              Text(
                text = metaLine,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
          Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      HorizontalDivider(
        modifier = Modifier.padding(horizontal = 10.dp),
        thickness = 1.dp,
        color = Color.White.copy(alpha = 0.14f),
      )

      // Unified Watch + Bookmark footer bar over subtle gradient
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier =
            Modifier.weight(1f)
              .height(31.dp)
              .clip(TvFooterBtnShape)
              .background(ObsidianElevated.copy(alpha = 0.94f))
              .border(1.dp, BorderBright.copy(alpha = 0.55f), TvFooterBtnShape)
              .clickable(onClick = onClick),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = TextOffWhite,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = stringResource(R.string.action_watch),
            style = MaterialTheme.typography.labelMedium,
            color = TextOffWhite,
          )
        }
        Box(
          contentAlignment = Alignment.Center,
          modifier =
            Modifier.size(31.dp)
              .clip(TvFooterBtnShape)
              .background(
                if (isSaved) EmeraldLive.copy(alpha = 0.18f)
                else ObsidianElevated.copy(alpha = 0.94f)
              )
              .border(
                1.dp,
                if (isSaved) EmeraldLive.copy(alpha = 0.72f)
                else BorderBright.copy(alpha = 0.55f),
                TvFooterBtnShape,
              )
              .clickable(onClick = onToggleSave),
        ) {
          Icon(
            imageVector =
              if (isSaved) Icons.Rounded.Check else Icons.Rounded.BookmarkBorder,
            contentDescription = stringResource(R.string.cd_favorite),
            tint = if (isSaved) EmeraldLive else TextOffWhite,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}