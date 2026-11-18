package com.app.toonplay.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.data.model.HomeFeed
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.EmeraldLive
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.ObsidianSurface
import com.app.toonplay.ui.theme.TextOffWhite
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonIndigoLight
import com.app.toonplay.ui.theme.ToonVioletPrimary
import com.app.toonplay.ui.theme.ToonVioletSoft

@Composable
fun ToonPlayProfileView(
  feed: HomeFeed,
  savedItems: List<AnimeItem>,
  savedIds: Set<String>,
  selectedLanguageFilter: LatestLanguageFilter,
  onSelectLanguageFilter: (LatestLanguageFilter) -> Unit,
  onItemClick: (AnimeItem) -> Unit,
  onToggleSave: (AnimeItem) -> Unit,
  onExploreHome: () -> Unit,
  modifier: Modifier = Modifier,
  horizontalPadding: Dp = 16.dp,
  bottomPadding: Dp = 112.dp,
  enableHoverLift: Boolean = false,
) {
  val listState = rememberLazyListState()
  val isReducedMotion = rememberIsReducedMotion()
  val cardWidth = 160.dp
  val gridSpacing = 12.dp

  BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("profile_screen")) {
    val availableWidth = this.maxWidth
    val contentWidth = (availableWidth - horizontalPadding * 2).coerceAtLeast(0.dp)
    val columns =
      remember(contentWidth) {
        calculateCompleteGridColumns(
          availableWidth = contentWidth,
          cardWidth = cardWidth,
          spacing = gridSpacing,
          minColumns = 1,
        )
      }
    val savedRows = remember(savedItems, columns) { savedItems.chunked(columns) }

    ProvideSmoothBringIntoView(edgeMargin = 20.dp, isReducedMotion = isReducedMotion) {
      LazyColumn(
        state = listState,
        modifier =
          Modifier.fillMaxSize()
            .smoothMouseWheelScroll(
              scrollableState = listState,
              orientation = Orientation.Vertical,
              isReducedMotion = isReducedMotion,
            ),
        contentPadding =
          PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            top = 8.dp,
            bottom = bottomPadding,
          ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        item(key = "profile_header_card", contentType = "profile_header") {
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = ObsidianCard,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .background(
                    Brush.linearGradient(
                      listOf(
                        ToonVioletSoft,
                        ObsidianCard,
                      )
                    )
                  )
                  .padding(18.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
              ) {
                Image(
                  painter = painterResource(id = R.drawable.ic_toonplay_app_icon),
                  contentDescription = stringResource(R.string.app_name),
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)),
                )
                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                  )
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldLive))
                    Text(
                      text = "${feed.allUnique.size} Live Titles  •  ${savedItems.size} Watchlist",
                      style = MaterialTheme.typography.labelMedium,
                      color = TextSecondary,
                    )
                  }
                }
              }
            }
          }
        }

        item(key = "profile_stats_row", contentType = "profile_stats") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            ProfileStatTile(
              icon = Icons.Rounded.Bookmark,
              value = savedItems.size.toString(),
              label = "Watchlist",
              modifier = Modifier.weight(1f),
            )
            ProfileStatTile(
              icon = Icons.Rounded.VideoLibrary,
              value = feed.allUnique.size.toString(),
              label = "Catalog",
              modifier = Modifier.weight(1f),
            )
            ProfileStatTile(
              icon = Icons.Rounded.Translate,
              value = if (selectedLanguageFilter == LatestLanguageFilter.HIN_TAM_TEL) "IND" else "INT",
              label = "Audio",
              modifier = Modifier.weight(1f),
            )
          }
        }

        item(key = "profile_audio_preference", contentType = "profile_audio_pref") {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = ObsidianSurface,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  text = stringResource(R.string.profile_audio_preference),
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = TextPrimary,
                )
                Text(
                  text = "Choose default feed for Latest Episodes",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextSecondary,
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, BorderSubtle),
              ) {
                Row(
                  modifier = Modifier.padding(3.dp),
                  horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                  LatestLanguageFilter.entries.forEach { filter ->
                    val selected = filter == selectedLanguageFilter
                    Surface(
                      onClick = { onSelectLanguageFilter(filter) },
                      shape = RoundedCornerShape(7.dp),
                      color = if (selected) ToonVioletPrimary else Color.Transparent,
                    ) {
                      Text(
                        text = filter.label,
                        style =
                          MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                          ),
                        color = if (selected) TextPrimary else TextOffWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                      )
                    }
                  }
                }
              }
            }
          }
        }

        item(key = "profile_watchlist_heading", contentType = "profile_heading") {
          SectionHeaderRow(
            title = stringResource(R.string.section_saved),
            kicker = stringResource(R.string.section_saved_sub),
            itemCount = savedItems.size,
          )
        }

        if (savedItems.isEmpty()) {
          item(key = "profile_watchlist_empty") {
            Surface(
              shape = RoundedCornerShape(18.dp),
              color = ObsidianCard,
              border = BorderStroke(1.dp, BorderSubtle),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                EmptyCollectionView(
                  title = stringResource(R.string.empty_saved_title),
                  subtitle = stringResource(R.string.empty_saved_sub),
                  icon = Icons.Rounded.Bookmark,
                  modifier = Modifier.padding(vertical = 0.dp),
                )
                Button(
                  onClick = onExploreHome,
                  colors = ButtonDefaults.buttonColors(containerColor = ToonVioletPrimary),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = stringResource(R.string.profile_explore_home),
                    style = MaterialTheme.typography.labelLarge,
                  )
                }
              }
            }
          }
        } else {
          items(
            count = savedRows.size,
            key = { idx ->
              "profile_saved_row_${columns}_${savedRows[rowKeyIdxSafe(idx, savedRows.size)].joinToString("_") { it.uid }}"
            },
            contentType = { "profile_saved_row" },
          ) { rowIdx ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            ) {
              val rowItems = savedRows[rowIdx]
              rowItems.forEach { item ->
                if (columns == 1) {
                  Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    AnimePosterCard(
                      item = item,
                      onClick = { onItemClick(item) },
                      width = null,
                      isSaved = item.id in savedIds,
                      onToggleSave = { onToggleSave(item) },
                      showActionFooter = true,
                      enableHoverLift = enableHoverLift,
                      modifier = Modifier.widthIn(max = cardWidth * 1.35f).fillMaxWidth(),
                    )
                  }
                } else {
                  AnimePosterCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    width = null,
                    isSaved = item.id in savedIds,
                    onToggleSave = { onToggleSave(item) },
                    showActionFooter = true,
                    enableHoverLift = enableHoverLift,
                    modifier = Modifier.weight(1f),
                  )
                }
              }
              repeat(columns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
          }
        }
      }
    }
  }
}

private fun rowKeyIdxSafe(index: Int, size: Int): Int = index.coerceIn(0, (size - 1).coerceAtLeast(0))

@Composable
private fun ProfileStatTile(
  icon: ImageVector,
  value: String,
  label: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = ObsidianElevated,
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = ToonIndigoLight,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
      )
    }
  }
}