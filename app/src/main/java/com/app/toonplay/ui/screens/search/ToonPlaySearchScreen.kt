package com.app.toonplay.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.DeviceFormFactor
import com.app.toonplay.data.model.SearchFilter
import com.app.toonplay.ui.components.AnimePosterGrid
import com.app.toonplay.ui.components.FeedErrorView
import com.app.toonplay.ui.components.GlassSearchBar
import com.app.toonplay.ui.components.SearchPosterSkeletonCard
import com.app.toonplay.ui.components.calculateCompleteGridColumns
import com.app.toonplay.ui.components.rememberIsReducedMotion
import com.app.toonplay.ui.components.rememberSkeletonBrush
import com.app.toonplay.ui.state.SearchScreenUiState
import com.app.toonplay.ui.theme.BorderBright
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.TextOffWhite
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonIndigoLight
import com.app.toonplay.ui.theme.ToonVioletPrimary
import com.app.toonplay.ui.theme.ToonVioletSoft

@Immutable
private data class SearchDeviceLayoutSpec(
  val cardWidth: Dp,
  val horizontalPadding: Dp,
  val itemSpacing: Dp,
  val verticalSpacing: Dp,
  val headerTopPadding: Dp,
  val bringIntoViewMargin: Dp,
  val enableHoverLift: Boolean,
  val searchBarHeight: Dp,
)

@Composable
private fun rememberSearchLayoutSpec(formFactor: DeviceFormFactor): SearchDeviceLayoutSpec {
  return remember(formFactor) {
    when (formFactor) {
      DeviceFormFactor.MOBILE ->
        SearchDeviceLayoutSpec(
          cardWidth = 158.dp,
          horizontalPadding = 16.dp,
          itemSpacing = 12.dp,
          verticalSpacing = 14.dp,
          headerTopPadding = 6.dp,
          bringIntoViewMargin = 16.dp,
          enableHoverLift = false,
          searchBarHeight = 48.dp,
        )
      DeviceFormFactor.TABLET ->
        SearchDeviceLayoutSpec(
          cardWidth = 158.dp,
          horizontalPadding = 24.dp,
          itemSpacing = 14.dp,
          verticalSpacing = 14.dp,
          headerTopPadding = 8.dp,
          bringIntoViewMargin = 24.dp,
          enableHoverLift = false,
          searchBarHeight = 48.dp,
        )
      DeviceFormFactor.BIG_TABLET ->
        SearchDeviceLayoutSpec(
          cardWidth = 158.dp,
          horizontalPadding = 24.dp,
          itemSpacing = 14.dp,
          verticalSpacing = 14.dp,
          headerTopPadding = 8.dp,
          bringIntoViewMargin = 24.dp,
          enableHoverLift = false,
          searchBarHeight = 48.dp,
        )
      DeviceFormFactor.DESKTOP ->
        SearchDeviceLayoutSpec(
          cardWidth = 162.dp,
          horizontalPadding = 24.dp,
          itemSpacing = 16.dp,
          verticalSpacing = 16.dp,
          headerTopPadding = 10.dp,
          bringIntoViewMargin = 24.dp,
          enableHoverLift = true,
          searchBarHeight = 48.dp,
        )
      DeviceFormFactor.TV ->
        SearchDeviceLayoutSpec(
          cardWidth = 176.dp,
          horizontalPadding = 42.dp,
          itemSpacing = 18.dp,
          verticalSpacing = 18.dp,
          headerTopPadding = 20.dp,
          bringIntoViewMargin = 42.dp,
          enableHoverLift = true,
          searchBarHeight = 52.dp,
        )
    }
  }
}

@Composable
fun ToonPlaySearchScreen(
  formFactor: DeviceFormFactor,
  searchState: SearchScreenUiState,
  savedIds: Set<String>,
  onQueryChange: (String) -> Unit,
  onFilterChange: (SearchFilter) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onLoadMore: () -> Unit,
  onRetrySearch: () -> Unit,
  onRetryTrending: () -> Unit,
  onDismissPartialWarning: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onClose)

  val spec = rememberSearchLayoutSpec(formFactor)
  val isReducedMotion = rememberIsReducedMotion()
  val gridState = rememberLazyGridState()
  val trimmedQuery = searchState.query.trim()
  val isQueryEmpty = searchState.isQueryEmpty

  LaunchedEffect(searchState.selectedFilter, trimmedQuery) {
    if (gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0) {
      gridState.scrollToItem(0)
    }
  }

  val navBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val headerAmbientBrush =
    remember {
      Brush.verticalGradient(
        0.0f to ToonVioletSoft.copy(alpha = 0.42f),
        0.55f to ObsidianBg.copy(alpha = 0.92f),
        1.0f to ObsidianBg,
      )
    }

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxSize()
        .background(ObsidianBg)
        .testTag("toonplay_search_screen"),
  ) {
    val useInlineHeaderRow = this.maxWidth >= 700.dp

    Column(modifier = Modifier.fillMaxSize()) {
      // Atmospheric Top Search & Filter Header
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .background(headerAmbientBrush)
            .statusBarsPadding()
            .padding(
              start = spec.horizontalPadding,
              end = spec.horizontalPadding,
              top = spec.headerTopPadding,
              bottom = 12.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          SearchBackButton(
            onClick = onClose,
            isReducedMotion = isReducedMotion,
          )

          GlassSearchBar(
            query = searchState.query,
            onQueryChange = onQueryChange,
            onClose = null,
            autoFocus = formFactor != DeviceFormFactor.TV,
            height = spec.searchBarHeight,
            modifier = Modifier.weight(1f),
          )
        }

        val resultsCount = searchState.displayedItems.size
        val headingTitle =
          if (isQueryEmpty) {
            stringResource(R.string.search_trending_heading)
          } else {
            stringResource(R.string.search_results_heading)
          }
        val headingSubtitle =
          when {
            isQueryEmpty -> {
              val sourceLabel =
                when (searchState.selectedFilter) {
                  SearchFilter.ALL -> stringResource(R.string.search_trending_subtitle)
                  SearchFilter.MOVIES -> stringResource(R.string.search_trending_movies_subtitle)
                  SearchFilter.SERIES -> stringResource(R.string.search_trending_series_subtitle)
                }
              if (searchState.isTrendingLoading) {
                sourceLabel
              } else {
                val countLabel =
                  if (resultsCount == 1) {
                    stringResource(R.string.search_titles_count_singular, resultsCount)
                  } else {
                    stringResource(R.string.search_titles_count_plural, resultsCount)
                  }
                "$sourceLabel  •  $countLabel"
              }
            }
            searchState.isSearching -> stringResource(R.string.search_searching_subtitle, trimmedQuery)
            else -> {
              val countLabel =
                if (resultsCount == 1) {
                  stringResource(R.string.search_results_count_singular, resultsCount)
                } else {
                  stringResource(R.string.search_results_count_plural, resultsCount)
                }
              "\"$trimmedQuery\"  •  $countLabel"
            }
          }

        if (useInlineHeaderRow) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            SearchSectionTitleBlock(
              title = headingTitle,
              subtitle = headingSubtitle,
              isTrending = isQueryEmpty,
              modifier = Modifier.weight(1f).padding(end = 16.dp),
            )
            SearchFilterChipRow(
              selectedFilter = searchState.selectedFilter,
              onSelectFilter = onFilterChange,
              isReducedMotion = isReducedMotion,
            )
          }
        } else {
          SearchFilterChipRow(
            selectedFilter = searchState.selectedFilter,
            onSelectFilter = onFilterChange,
            isReducedMotion = isReducedMotion,
          )
          SearchSectionTitleBlock(
            title = headingTitle,
            subtitle = headingSubtitle,
            isTrending = isQueryEmpty,
          )
        }
      }

      HorizontalDivider(
        color = BorderSubtle.copy(alpha = 0.7f),
        thickness = 1.dp,
      )

      // Results / Loading / Error / Empty Area
      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        val isShowingFullSkeleton =
          (isQueryEmpty && searchState.isTrendingLoading) ||
                  (!isQueryEmpty && searchState.isSearching)
        val fullErrorMessage =
          when {
            isQueryEmpty &&
                    !searchState.trendingErrorMessage.isNullOrBlank() &&
                    searchState.displayedItems.isEmpty() -> searchState.trendingErrorMessage
            !isQueryEmpty &&
                    !searchState.errorMessage.isNullOrBlank() &&
                    searchState.displayedItems.isEmpty() -> searchState.errorMessage
            else -> null
          }

        when {
          isShowingFullSkeleton -> {
            SearchGridSkeleton(spec = spec, bottomPadding = navBottomInset + 28.dp)
          }
          fullErrorMessage != null -> {
            FeedErrorView(
              message = fullErrorMessage,
              onRetry = if (isQueryEmpty) onRetryTrending else onRetrySearch,
            )
          }
          else -> {
            Column(modifier = Modifier.fillMaxSize()) {
              AnimatedVisibility(
                visible = !isQueryEmpty && !searchState.partialFailureWarning.isNullOrBlank(),
              ) {
                PartialSourceBanner(
                  message = stringResource(R.string.search_partial_warning),
                  horizontalPadding = spec.horizontalPadding,
                  onRetry = onRetrySearch,
                  onDismiss = onDismissPartialWarning,
                )
              }

              val emptyTitle = stringResource(R.string.empty_search_title)
              val emptySubtitle =
                if (isQueryEmpty) {
                  stringResource(R.string.search_empty_trending_subtitle)
                } else {
                  stringResource(R.string.search_empty_query_subtitle, trimmedQuery)
                }
              val emptyActionLabel =
                when {
                  searchState.selectedFilter != SearchFilter.ALL ->
                    stringResource(R.string.search_show_all_action)
                  !isQueryEmpty -> stringResource(R.string.search_clear_action)
                  else -> null
                }
              val onEmptyAction: (() -> Unit)? =
                when {
                  searchState.selectedFilter != SearchFilter.ALL ->
                    ({ onFilterChange(SearchFilter.ALL) })
                  !isQueryEmpty -> ({ onQueryChange("") })
                  else -> null
                }

              AnimePosterGrid(
                items = searchState.displayedItems,
                savedIds = savedIds,
                onItemClick = onItemClick,
                onToggleSave = onToggleSave,
                minCellWidth = spec.cardWidth,
                horizontalPadding = spec.horizontalPadding,
                itemSpacing = spec.itemSpacing,
                verticalSpacing = spec.verticalSpacing,
                topPadding = 14.dp,
                bottomPadding = navBottomInset + 28.dp,
                bringIntoViewMargin = spec.bringIntoViewMargin,
                enableHoverLift = spec.enableHoverLift,
                showYearOnCards = false,
                emptyTitle = emptyTitle,
                emptySubtitle = emptySubtitle,
                emptyActionLabel = emptyActionLabel,
                onEmptyAction = onEmptyAction,
                gridState = gridState,
                isLoadingMore = !isQueryEmpty && searchState.isLoadingMore,
                hasMore = !isQueryEmpty && searchState.hasMore,
                onLoadMore = if (!isQueryEmpty) onLoadMore else null,
                paginationError = if (!isQueryEmpty) searchState.paginationError else null,
                onRetryLoadMore = if (!isQueryEmpty) onLoadMore else null,
                modifier = Modifier.weight(1f).fillMaxWidth(),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PartialSourceBanner(
  message: String,
  horizontalPadding: Dp,
  onRetry: () -> Unit,
  onDismiss: () -> Unit,
) {
  Surface(
    color = ObsidianCard,
    border = BorderStroke(1.dp, ToonVioletPrimary.copy(alpha = 0.45f)),
    shape = RoundedCornerShape(12.dp),
    modifier =
      Modifier.fillMaxWidth()
        .padding(start = horizontalPadding, end = horizontalPadding, top = 12.dp)
        .testTag("search_partial_failure_banner"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Info,
        contentDescription = null,
        tint = ToonIndigoLight,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      TextButton(
        onClick = onRetry,
        modifier = Modifier.minimumInteractiveComponentSize(),
      ) {
        Icon(
          imageVector = Icons.Rounded.Refresh,
          contentDescription = null,
          tint = ToonIndigoLight,
          modifier = Modifier.size(15.dp),
        )
        Text(
          text = stringResource(R.string.action_retry),
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = ToonIndigoLight,
          modifier = Modifier.padding(start = 4.dp),
        )
      }
      IconButton(
        onClick = onDismiss,
        modifier = Modifier.minimumInteractiveComponentSize(),
      ) {
        Icon(
          imageVector = Icons.Rounded.Close,
          contentDescription = stringResource(R.string.action_close),
          tint = TextSecondary,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
private fun SearchBackButton(
  onClick: () -> Unit,
  isReducedMotion: Boolean,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val isHovered by interactionSource.collectIsHoveredAsState()
  val elevated = isFocused || isHovered
  val scale by
  animateFloatAsState(
    targetValue = if (isFocused) 1.06f else 1f,
    animationSpec = if (isReducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
    label = "search_back_scale",
  )
  val borderColor by
  animateColorAsState(
    targetValue = if (elevated) ToonVioletPrimary else BorderSubtle,
    label = "search_back_border",
  )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    modifier =
      Modifier.minimumInteractiveComponentSize()
        .size(44.dp)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .hoverable(interactionSource)
        .testTag("search_back_button"),
    shape = CircleShape,
    color = if (isFocused) ToonVioletPrimary else ObsidianCard,
    border = BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
        contentDescription = stringResource(R.string.cd_back),
        tint = TextPrimary,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

@Composable
private fun SearchSectionTitleBlock(
  title: String,
  subtitle: String,
  isTrending: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector =
          if (isTrending) Icons.AutoMirrored.Rounded.TrendingUp else Icons.Rounded.Search,
        contentDescription = null,
        tint = ToonIndigoLight,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag("search_section_heading"),
      )
    }
    Text(
      text = subtitle,
      style = MaterialTheme.typography.labelMedium,
      color = TextSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.testTag("search_section_subtitle"),
    )
  }
}

@Composable
private fun SearchFilterChipRow(
  selectedFilter: SearchFilter,
  onSelectFilter: (SearchFilter) -> Unit,
  isReducedMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()).testTag("search_filter_row"),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    SearchFilter.entries.forEach { filter ->
      val (label, icon) =
        when (filter) {
          SearchFilter.ALL -> stringResource(R.string.search_filter_all) to Icons.Rounded.Apps
          SearchFilter.MOVIES -> stringResource(R.string.search_filter_movies) to Icons.Rounded.Movie
          SearchFilter.SERIES -> stringResource(R.string.search_filter_series) to Icons.Rounded.Tv
        }
      SearchFilterPill(
        label = label,
        icon = icon,
        selected = selectedFilter == filter,
        isReducedMotion = isReducedMotion,
        onClick = { onSelectFilter(filter) },
        testTag = "search_filter_${filter.name.lowercase()}",
      )
    }
  }
}

@Composable
private fun SearchFilterPill(
  label: String,
  icon: ImageVector,
  selected: Boolean,
  isReducedMotion: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val isHovered by interactionSource.collectIsHoveredAsState()
  val scale by
  animateFloatAsState(
    targetValue = if (isFocused) 1.05f else 1f,
    animationSpec = if (isReducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
    label = "search_filter_scale",
  )
  val containerColor by
  animateColorAsState(
    targetValue =
      when {
        selected -> ToonVioletPrimary
        isFocused || isHovered -> ObsidianElevated
        else -> ObsidianCard
      },
    label = "search_filter_bg",
  )
  val borderColor by
  animateColorAsState(
    targetValue =
      when {
        isFocused -> TextPrimary
        selected -> ToonIndigoLight.copy(alpha = 0.65f)
        isHovered -> BorderBright
        else -> BorderSubtle
      },
    label = "search_filter_border",
  )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(50),
    color = containerColor,
    border = BorderStroke(if (isFocused) 1.8.dp else 1.dp, borderColor),
    modifier =
      Modifier.minimumInteractiveComponentSize()
        .height(40.dp)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .hoverable(interactionSource)
        .testTag(testTag),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) TextPrimary else TextSecondary,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = label,
        style =
          MaterialTheme.typography.labelLarge.copy(
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
          ),
        color = if (selected) TextPrimary else TextOffWhite,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun SearchGridSkeleton(
  spec: SearchDeviceLayoutSpec,
  bottomPadding: Dp,
) {
  val brush = rememberSkeletonBrush()

  BoxWithConstraints(modifier = Modifier.fillMaxSize().testTag("search_loading_skeleton")) {
    val contentWidth = (this.maxWidth - spec.horizontalPadding * 2).coerceAtLeast(0.dp)
    val columns =
      remember(contentWidth, spec.cardWidth, spec.itemSpacing) {
        calculateCompleteGridColumns(
          availableWidth = contentWidth,
          cardWidth = spec.cardWidth,
          spacing = spec.itemSpacing,
          minColumns = 1,
        )
      }

    LazyVerticalGrid(
      columns = GridCells.Fixed(columns),
      userScrollEnabled = false,
      contentPadding =
        PaddingValues(
          start = spec.horizontalPadding,
          end = spec.horizontalPadding,
          top = 14.dp,
          bottom = bottomPadding,
        ),
      horizontalArrangement = Arrangement.spacedBy(spec.itemSpacing),
      verticalArrangement = Arrangement.spacedBy(spec.verticalSpacing),
      modifier = Modifier.fillMaxSize(),
    ) {
      items(
        count = (columns * 3).coerceAtLeast(6),
        key = { index -> "search_skeleton_$index" },
        contentType = { "search_skeleton_card" },
      ) {
        if (columns == 1) {
          Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SearchPosterSkeletonCard(
              brush = brush,
              modifier = Modifier.widthIn(max = spec.cardWidth * 1.35f).fillMaxWidth(),
            )
          }
        } else {
          SearchPosterSkeletonCard(
            brush = brush,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}