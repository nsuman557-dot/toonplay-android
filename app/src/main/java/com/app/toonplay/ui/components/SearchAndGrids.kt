package com.app.toonplay.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.ui.theme.BorderBright
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.TextMuted
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonVioletPrimary

@Composable
fun GlassSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  onClose: (() -> Unit)? = null,
  autoFocus: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(autoFocus) {
    if (autoFocus) {
      runCatching { focusRequester.requestFocus() }
    }
  }

  Surface(
    modifier = modifier.height(48.dp),
    shape = RoundedCornerShape(50),
    color = ObsidianCard,
    border = BorderStroke(1.dp, if (query.isNotEmpty()) BorderBright else BorderSubtle),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Search,
        contentDescription = null,
        tint = if (query.isNotEmpty()) ToonVioletPrimary else TextSecondary,
        modifier = Modifier.size(20.dp),
      )

      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
        if (query.isEmpty()) {
          Text(
            text = stringResource(R.string.search_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            maxLines = 1,
          )
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
          cursorBrush = SolidColor(ToonVioletPrimary),
          modifier =
            Modifier.fillMaxWidth()
              .focusRequester(focusRequester)
              .testTag("search_input_field"),
        )
      }

      if (query.isNotEmpty() || onClose != null) {
        IconButton(
          onClick = {
            if (query.isNotEmpty()) {
              onQueryChange("")
            } else {
              onClose?.invoke()
            }
          },
          modifier = Modifier.size(32.dp).testTag("search_clear_or_close"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.action_clear),
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
          )
        }
      }
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
  bottomPadding: Dp = 24.dp,
  enableHoverLift: Boolean = false,
  emptyTitle: String = stringResource(R.string.empty_search_title),
  emptySubtitle: String = stringResource(R.string.empty_search_sub),
  gridState: LazyGridState = rememberLazyGridState(),
) {
  if (items.isEmpty()) {
    EmptyCollectionView(
      title = emptyTitle,
      subtitle = emptySubtitle,
      icon = Icons.Rounded.SearchOff,
      modifier = modifier,
    )
    return
  }

  val isReducedMotion = rememberIsReducedMotion()
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

    ProvideSmoothBringIntoView(edgeMargin = 20.dp, isReducedMotion = isReducedMotion) {
      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        contentPadding =
          PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            top = 8.dp,
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
            )
          }
        }
      }
    }
  }
}

@Composable
fun FullScreenSearchSheet(
  query: String,
  results: List<AnimeItem>,
  allItems: List<AnimeItem>,
  savedIds: Set<String>,
  onQueryChange: (String) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val displayItems = if (query.isBlank()) allItems.take(24) else results

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(ObsidianBg)
        .statusBarsPadding()
        .padding(top = 10.dp)
        .testTag("full_search_overlay"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      GlassSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onClose = onClose,
        autoFocus = true,
        modifier = Modifier.weight(1f),
      )
      Surface(
        onClick = onClose,
        shape = RoundedCornerShape(50),
        color = ObsidianElevated,
        border = BorderStroke(1.dp, BorderSubtle),
      ) {
        Text(
          text = stringResource(R.string.action_close),
          style = MaterialTheme.typography.labelLarge,
          color = TextPrimary,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text =
        if (query.isBlank()) "Quick Browse (${allItems.size} titles)"
        else "Results (${results.size})",
      style = MaterialTheme.typography.labelLarge,
      color = TextSecondary,
      modifier = Modifier.padding(horizontal = 18.dp),
    )

    Spacer(modifier = Modifier.height(8.dp))

    AnimePosterGrid(
      items = displayItems,
      savedIds = savedIds,
      onItemClick = onItemClick,
      onToggleSave = onToggleSave,
      minCellWidth = 116.dp,
      bottomPadding = 96.dp,
    )
  }
}