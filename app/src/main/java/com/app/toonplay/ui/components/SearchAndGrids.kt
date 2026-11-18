package com.app.toonplay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.ui.theme.BorderBright
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.TextMuted
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonVioletPrimary
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun GlassSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  onClose: (() -> Unit)? = null,
  autoFocus: Boolean = false,
  height: Dp = 48.dp,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current
  var isFocused by remember { mutableStateOf(false) }

  LaunchedEffect(autoFocus) {
    if (autoFocus) {
      runCatching { focusRequester.requestFocus() }
    }
  }

  val borderColor by
  animateColorAsState(
    targetValue =
      when {
        isFocused -> ToonVioletPrimary
        query.isNotEmpty() -> BorderBright
        else -> BorderSubtle
      },
    label = "search_bar_border",
  )

  Surface(
    modifier = modifier.height(height),
    shape = RoundedCornerShape(50),
    color = ObsidianCard,
    border = BorderStroke(if (isFocused) 1.5.dp else 1.dp, borderColor),
    shadowElevation = if (isFocused) 6.dp else 0.dp,
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Search,
        contentDescription = null,
        tint = if (isFocused || query.isNotEmpty()) ToonVioletPrimary else TextSecondary,
        modifier = Modifier.size(20.dp),
      )

      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
        if (query.isEmpty()) {
          Text(
            text = stringResource(R.string.search_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
          cursorBrush = SolidColor(ToonVioletPrimary),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions =
            KeyboardActions(
              onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus()
              }
            ),
          modifier =
            Modifier.fillMaxWidth()
              .focusRequester(focusRequester)
              .onFocusChanged { isFocused = it.isFocused }
              .testTag("search_input_field"),
        )
      }

      AnimatedVisibility(
        visible = query.isNotEmpty() || onClose != null,
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit = fadeOut() + scaleOut(targetScale = 0.85f),
      ) {
        Surface(
          onClick = {
            if (query.isNotEmpty()) {
              onQueryChange("")
              runCatching { focusRequester.requestFocus() }
            } else {
              onClose?.invoke()
            }
          },
          shape = CircleShape,
          color = if (query.isNotEmpty()) ObsidianElevated else ObsidianCard,
          border = if (query.isNotEmpty()) BorderStroke(1.dp, BorderSubtle) else null,
          modifier = Modifier.size(32.dp).testTag("search_clear_or_close"),
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
              imageVector = Icons.Rounded.Close,
              contentDescription = stringResource(R.string.action_clear),
              tint = if (query.isNotEmpty()) TextPrimary else TextSecondary,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
fun SearchBarTrigger(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "search_bar_trigger",
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isFocused by interactionSource.collectIsFocusedAsState()
  val highlighted = isHovered || isFocused
  val borderColor by
  animateColorAsState(
    targetValue = if (highlighted) ToonVioletPrimary else BorderSubtle,
    label = "search_trigger_border",
  )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    modifier =
      modifier
        .height(48.dp)
        .hoverable(interactionSource)
        .testTag(testTag),
    shape = RoundedCornerShape(50),
    color = ObsidianCard,
    border = BorderStroke(if (isFocused) 1.5.dp else 1.dp, borderColor),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Search,
        contentDescription = stringResource(R.string.cd_search),
        tint = if (highlighted) ToonVioletPrimary else TextSecondary,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = stringResource(R.string.search_placeholder),
        style = MaterialTheme.typography.bodyMedium,
        color = TextMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
fun AnimePosterGrid(
  items: List<AnimeItem>,
  savedIds: Set<String>,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  modifier: Modifier = Modifier,
  minCellWidth: Dp = 158.dp,
  horizontalPadding: Dp = 16.dp,
  itemSpacing: Dp = 12.dp,
  verticalSpacing: Dp = 14.dp,
  topPadding: Dp = 8.dp,
  bottomPadding: Dp = 24.dp,
  bringIntoViewMargin: Dp = 20.dp,
  enableHoverLift: Boolean = false,
  showYearOnCards: Boolean = true,
  emptyTitle: String = stringResource(R.string.empty_search_title),
  emptySubtitle: String = stringResource(R.string.empty_search_sub),
  emptyActionLabel: String? = null,
  onEmptyAction: (() -> Unit)? = null,
  gridState: LazyGridState = rememberLazyGridState(),
  isLoadingMore: Boolean = false,
  hasMore: Boolean = false,
  onLoadMore: (() -> Unit)? = null,
  paginationError: String? = null,
  onRetryLoadMore: (() -> Unit)? = null,
) {
  if (items.isEmpty() && !isLoadingMore) {
    Column(
      modifier = modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      EmptyCollectionView(
        title = emptyTitle,
        subtitle = emptySubtitle,
        icon = Icons.Rounded.SearchOff,
      )
      if (!emptyActionLabel.isNullOrBlank() && onEmptyAction != null) {
        Button(
          onClick = onEmptyAction,
          colors = ButtonDefaults.buttonColors(containerColor = ToonVioletPrimary),
          shape = RoundedCornerShape(50),
          modifier = Modifier.minimumInteractiveComponentSize().testTag("empty_grid_action_button"),
        ) {
          Text(
            text = emptyActionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
    return
  }

  val isReducedMotion = rememberIsReducedMotion()
  val skeletonBrush = if (isLoadingMore) rememberSkeletonBrush() else null

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val availableWidth = this.maxWidth
    val contentWidth = (availableWidth - horizontalPadding * 2).coerceAtLeast(0.dp)
    val columns =
      remember(contentWidth, minCellWidth, itemSpacing) {
        calculateCompleteGridColumns(
          availableWidth = contentWidth,
          cardWidth = minCellWidth,
          spacing = itemSpacing,
          minColumns = 1,
        )
      }

    if (onLoadMore != null) {
      LaunchedEffect(gridState, items.size, columns, hasMore, isLoadingMore, paginationError) {
        if (!hasMore || isLoadingMore || paginationError != null || items.isEmpty()) {
          return@LaunchedEffect
        }
        snapshotFlow {
          val layoutInfo = gridState.layoutInfo
          val totalCount = layoutInfo.totalItemsCount
          val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
          totalCount > 0 && lastVisible >= (totalCount - columns.coerceAtLeast(1) - 1)
        }
          .distinctUntilChanged()
          .collect { shouldTrigger ->
            if (shouldTrigger) {
              onLoadMore()
            }
          }
      }
    }

    ProvideSmoothBringIntoView(edgeMargin = bringIntoViewMargin, isReducedMotion = isReducedMotion) {
      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        contentPadding =
          PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            top = topPadding,
            bottom = bottomPadding,
          ),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        modifier =
          Modifier.fillMaxSize()
            .smoothMouseWheelScroll(
              scrollableState = gridState,
              orientation = Orientation.Vertical,
              isReducedMotion = isReducedMotion,
            ),
      ) {
        items(items = items, key = { it.uid }, contentType = { "poster_grid_card" }) { item ->
          if (columns == 1) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
              AnimePosterCard(
                item = item,
                onClick = { onItemClick(item) },
                width = null,
                isSaved = item.id in savedIds,
                onToggleSave = { onToggleSave(item) },
                enableHoverLift = enableHoverLift,
                showYear = showYearOnCards,
                modifier = Modifier.widthIn(max = minCellWidth * 1.35f).fillMaxWidth(),
              )
            }
          } else {
            AnimePosterCard(
              item = item,
              onClick = { onItemClick(item) },
              width = null,
              isSaved = item.id in savedIds,
              onToggleSave = { onToggleSave(item) },
              enableHoverLift = enableHoverLift,
              showYear = showYearOnCards,
            )
          }
        }

        if (isLoadingMore && skeletonBrush != null) {
          items(
            count = columns.coerceAtLeast(1),
            key = { index -> "pagination_skeleton_$index" },
            contentType = { "pagination_skeleton" },
          ) {
            if (columns == 1) {
              Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SearchPosterSkeletonCard(
                  brush = skeletonBrush,
                  modifier =
                    Modifier.widthIn(max = minCellWidth * 1.35f)
                      .fillMaxWidth()
                      .testTag("search_pagination_skeleton_card"),
                )
              }
            } else {
              SearchPosterSkeletonCard(
                brush = skeletonBrush,
                modifier = Modifier.fillMaxWidth().testTag("search_pagination_skeleton_card"),
              )
            }
          }
        } else if (!paginationError.isNullOrBlank() && onRetryLoadMore != null) {
          item(
            key = "pagination_error_footer",
            span = { GridItemSpan(maxLineSpan) },
            contentType = "pagination_error",
          ) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = ObsidianCard,
              border = BorderStroke(1.dp, BorderSubtle),
              modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag("search_pagination_error"),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = paginationError,
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                OutlinedButton(
                  onClick = onRetryLoadMore,
                  shape = RoundedCornerShape(50),
                  border = BorderStroke(1.dp, ToonVioletPrimary.copy(alpha = 0.7f)),
                  modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.size(6.dp))
                  Text(
                    text = stringResource(R.string.action_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
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