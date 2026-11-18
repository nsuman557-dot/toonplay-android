package com.app.toonplay.ui.components

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.ui.theme.AmberGold
import com.app.toonplay.ui.theme.BorderBright
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.EmeraldLive
import com.app.toonplay.ui.theme.ToonVioletPrimary
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.ObsidianGlass
import com.app.toonplay.ui.theme.ObsidianSurface
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailModal(
  item: AnimeItem,
  isSaved: Boolean,
  relatedItems: List<AnimeItem>,
  onToggleSave: () -> Unit,
  onSelectRelated: (AnimeItem) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onDismiss)
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val relatedRowState = rememberLazyListState()
  val isReducedMotion = rememberIsReducedMotion()

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(ObsidianBg.copy(alpha = 0.85f))
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 680.dp)
          .clickable(enabled = false) {}
          .testTag("anime_detail_modal"),
      color = ObsidianBg,
    ) {
      ProvideSmoothBringIntoView(edgeMargin = 20.dp, isReducedMotion = isReducedMotion) {
        Column(
          modifier =
            Modifier.fillMaxSize()
              .smoothMouseWheelScroll(
                scrollableState = scrollState,
                orientation = Orientation.Vertical,
                isReducedMotion = isReducedMotion,
              )
              .verticalScroll(scrollState)
        ) {
          // Full-bleed Backdrop Header with floating controls
          val heroImageUrl = item.backdropUrl.ifBlank { item.posterUrl }
          val heroImageRequest =
            remember(heroImageUrl) {
              ImageRequest.Builder(context)
                .data(heroImageUrl)
                .crossfade(false)
                .size(960, 620)
                .build()
            }
          val modalBackdropScrim =
            remember {
              Brush.verticalGradient(
                0.0f to ObsidianBg.copy(alpha = 0.45f),
                0.5f to Color.Transparent,
                1.0f to ObsidianBg,
              )
            }
          Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10.5f)) {
            AsyncImage(
              model = heroImageRequest,
              contentDescription = item.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )

            Box(modifier = Modifier.fillMaxSize().background(modalBackdropScrim))

            Row(
              modifier =
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              GlassCircleIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                onClick = onDismiss,
                testTag = "detail_back_button",
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCircleIconButton(
                  icon = Icons.Rounded.Share,
                  contentDescription = stringResource(R.string.cd_share),
                  onClick = {
                    val sendIntent =
                      Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                          Intent.EXTRA_TEXT,
                          "Check out ${item.title} on ToonPlay!",
                        )
                      }
                    context.startActivity(Intent.createChooser(sendIntent, item.title))
                  },
                  testTag = "detail_share_button",
                )
                GlassCircleIconButton(
                  icon = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                  contentDescription = stringResource(R.string.cd_favorite),
                  onClick = onToggleSave,
                  tint = if (isSaved) ToonVioletPrimary else TextPrimary,
                  testTag = "detail_save_icon_button",
                )
              }
            }

            // Floating Accent Bookmark Button anchored at bottom edge
            Surface(
              onClick = onToggleSave,
              modifier =
                Modifier.align(Alignment.BottomCenter)
                  .offset(y = 22.dp)
                  .size(52.dp)
                  .minimumInteractiveComponentSize()
                  .testTag("detail_center_action"),
              shape = CircleShape,
              color = ObsidianElevated,
              border =
                BorderStroke(
                  1.5.dp,
                  if (isSaved) EmeraldLive.copy(alpha = 0.78f) else BorderBright,
                ),
              shadowElevation = 10.dp,
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector =
                    if (isSaved) Icons.Rounded.Check else Icons.Rounded.BookmarkBorder,
                  contentDescription = stringResource(R.string.cd_favorite),
                  tint = if (isSaved) EmeraldLive else TextPrimary,
                  modifier = Modifier.size(24.dp),
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(34.dp))

          Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            if (!item.year.isNullOrBlank()) {
              Text(
                text = item.year,
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
              )
            }

            Text(
              text = item.title,
              style = MaterialTheme.typography.displaySmall,
              color = TextPrimary,
            )

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              MetadataPill(text = item.typeLabel)
              item.seasonLabel?.let { MetadataPill(text = it) }
              item.episodeLabel?.let {
                MetadataPill(
                  text = it,
                  containerColor = ToonVioletPrimary.copy(alpha = 0.2f),
                  borderColor = ToonVioletPrimary.copy(alpha = 0.5f),
                )
              }
              item.anilistId?.let { MetadataPill(text = "AniList #$it") }
            }

            if (!item.rating.isNullOrBlank()) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Star,
                  contentDescription = null,
                  tint = AmberGold,
                  modifier = Modifier.size(22.dp),
                )
                Text(
                  text = "${item.rating}/10",
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary,
                )
                Text(
                  text = "Community Score",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextSecondary,
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Button(
                onClick = onToggleSave,
                modifier = Modifier.weight(1f).height(48.dp).testTag("detail_save_cta"),
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) EmeraldLive.copy(alpha = 0.16f) else ToonVioletPrimary
                  ),
                border =
                  if (isSaved) BorderStroke(1.dp, EmeraldLive.copy(alpha = 0.65f)) else null,
              ) {
                Icon(
                  imageVector = if (isSaved) Icons.Rounded.Check else Icons.Rounded.BookmarkBorder,
                  contentDescription = null,
                  tint = if (isSaved) EmeraldLive else TextPrimary,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text =
                    stringResource(if (isSaved) R.string.action_saved else R.string.action_save),
                  color = if (isSaved) EmeraldLive else TextPrimary,
                  style = MaterialTheme.typography.labelLarge,
                )
              }
              OutlinedButton(
                onClick = {
                  val sendIntent =
                    Intent(Intent.ACTION_SEND).apply {
                      type = "text/plain"
                      putExtra(Intent.EXTRA_TEXT, "Check out ${item.title} on ToonPlay!")
                    }
                  context.startActivity(Intent.createChooser(sendIntent, item.title))
                },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderSubtle),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Share,
                  contentDescription = null,
                  tint = TextPrimary,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = stringResource(R.string.action_share),
                  color = TextPrimary,
                  style = MaterialTheme.typography.labelLarge,
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = stringResource(R.string.overview_heading),
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary,
            )
            Text(
              text = item.overview.ifBlank { stringResource(R.string.no_overview) },
              style = MaterialTheme.typography.bodyLarge,
              color = TextSecondary,
            )

            if (relatedItems.isNotEmpty()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = stringResource(R.string.related_titles),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
              )
              LazyRow(
                state = relatedRowState,
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier =
                  Modifier.smoothMouseWheelScroll(
                    scrollableState = relatedRowState,
                    orientation = Orientation.Horizontal,
                    isReducedMotion = isReducedMotion,
                  ),
              ) {
                items(
                  items = relatedItems,
                  key = { it.uid },
                  contentType = { "related_card" },
                ) { rel ->
                  AnimePosterCard(
                    item = rel,
                    onClick = { onSelectRelated(rel) },
                    width = 148.dp,
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(40.dp))
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailInspectorPane(
  item: AnimeItem,
  isSaved: Boolean,
  onToggleSave: () -> Unit,
  onExpandFull: () -> Unit,
  onClose: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val isReducedMotion = rememberIsReducedMotion()

  Surface(
    modifier = modifier.fillMaxSize(),
    color = ObsidianSurface,
    shape = RoundedCornerShape(24.dp),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .smoothMouseWheelScroll(
            scrollableState = scrollState,
            orientation = Orientation.Vertical,
            isReducedMotion = isReducedMotion,
          )
          .verticalScroll(scrollState)
    ) {
      val inspectorUrl = item.backdropUrl.ifBlank { item.posterUrl }
      val inspectorImageRequest =
        remember(inspectorUrl) {
          ImageRequest.Builder(context)
            .data(inspectorUrl)
            .crossfade(false)
            .size(720, 420)
            .build()
        }
      val inspectorScrim =
        remember {
          Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.6f to ObsidianSurface.copy(alpha = 0.4f),
            1.0f to ObsidianSurface,
          )
        }
      Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
        AsyncImage(
          model = inspectorImageRequest,
          contentDescription = item.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(modifier = Modifier.fillMaxSize().background(inspectorScrim))
        if (onClose != null) {
          GlassCircleIconButton(
            icon = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.action_close),
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            size = 38.dp,
          )
        }
        item.rating?.let { rating ->
          StarRatingPill(
            rating = rating,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
          )
        }
      }

      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = item.title,
          style = MaterialTheme.typography.headlineMedium,
          color = TextPrimary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          MetadataPill(text = item.typeLabel)
          item.year?.let { MetadataPill(text = it) }
          item.seasonLabel?.let { MetadataPill(text = it) }
          item.episodeLabel?.let {
            MetadataPill(
              text = it,
              containerColor = ToonVioletPrimary.copy(alpha = 0.2f),
              borderColor = ToonVioletPrimary.copy(alpha = 0.5f),
            )
          }
        }

        Text(
          text = item.overview.ifBlank { stringResource(R.string.no_overview) },
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          maxLines = 6,
          overflow = TextOverflow.Ellipsis,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onToggleSave,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = if (isSaved) EmeraldLive.copy(alpha = 0.16f) else ToonVioletPrimary
              ),
            border =
              if (isSaved) BorderStroke(1.dp, EmeraldLive.copy(alpha = 0.65f)) else null,
          ) {
            Icon(
              imageVector = if (isSaved) Icons.Rounded.Check else Icons.Rounded.BookmarkBorder,
              contentDescription = null,
              tint = if (isSaved) EmeraldLive else TextPrimary,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(if (isSaved) R.string.action_saved else R.string.action_save),
              color = if (isSaved) EmeraldLive else TextPrimary,
              style = MaterialTheme.typography.labelLarge,
            )
          }
          OutlinedButton(
            onClick = onExpandFull,
            modifier = Modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
          ) {
            Text(
              text = stringResource(R.string.action_explore),
              color = TextPrimary,
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    }
  }
}
