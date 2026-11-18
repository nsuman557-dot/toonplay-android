package com.app.toonplay.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeItem
import com.app.toonplay.ui.theme.AmberGold
import com.app.toonplay.ui.theme.BorderBright
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.EmeraldLive
import com.app.toonplay.ui.theme.IndigoBadgeDark
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.ObsidianGlass
import com.app.toonplay.ui.theme.TextOffWhite
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonVioletPrimary
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private val PosterCardShape = RoundedCornerShape(16.dp)
private val PosterBadgeShape = RoundedCornerShape(8.dp)
private val FooterButtonShape = RoundedCornerShape(9.dp)
private val HeroCardShape = RoundedCornerShape(24.dp)
private val HeroBadgeShape = RoundedCornerShape(12.dp)

private val PosterCardOverlayGradient =
  Brush.verticalGradient(
    0.0f to Color.Black.copy(alpha = 0.24f),
    0.38f to Color.Transparent,
    0.68f to ObsidianBg.copy(alpha = 0.78f),
    0.81f to ObsidianCard.copy(alpha = 0.86f),
    0.92f to ObsidianBg.copy(alpha = 0.93f),
    1.0f to ObsidianBg.copy(alpha = 0.97f),
  )

private val HeroCardOverlayGradient =
  Brush.verticalGradient(
    0.0f to Color.Transparent,
    0.65f to Color.Transparent,
    1.0f to ObsidianBg.copy(alpha = 0.72f),
  )

/**
 * Unified ToonPlay card item used across Latest Episodes, Childhood Shows, Popular TV Series,
 * and Blockbuster Anime Movies (clean dark/slate surface, separation line after title, Watch + Bookmark footer).
 */
@Composable
fun AnimePosterCard(
  item: AnimeItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  width: Dp? = 158.dp,
  isSaved: Boolean = false,
  onToggleSave: (() -> Unit)? = null,
  rankBadge: Int? = null,
  showActionFooter: Boolean = true,
  enableHoverLift: Boolean = false,
) {
  val context = LocalContext.current
  val imageUrl = item.posterUrl.ifBlank { item.backdropUrl }
  val imageRequest =
    remember(imageUrl) {
      ImageRequest.Builder(context)
        .data(imageUrl)
        .crossfade(false)
        .size(340, 430)
        .build()
    }

  val baseSizedModifier =
    if (width != null) {
      modifier.width(width)
    } else {
      modifier.fillMaxWidth()
    }

  val cardModifier =
    if (enableHoverLift) {
      val interactionSource = remember { MutableInteractionSource() }
      val isHovered by interactionSource.collectIsHoveredAsState()
      val isFocused by interactionSource.collectIsFocusedAsState()
      val elevated = isHovered || isFocused
      val scale by
      animateFloatAsState(
        targetValue = if (elevated) 1.035f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "poster_hover_scale",
      )
      val borderColor by
      animateColorAsState(
        targetValue = if (elevated) ToonVioletPrimary else BorderSubtle,
        label = "poster_hover_border",
      )
      baseSizedModifier
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .hoverable(interactionSource)
        .clip(PosterCardShape)
        .background(ObsidianCard)
        .border(if (isFocused) 2.dp else 1.dp, borderColor, PosterCardShape)
        .clickable(
          interactionSource = interactionSource,
          indication = LocalIndication.current,
          onClick = onClick,
        )
        .testTag("poster_card_${item.id}")
    } else {
      baseSizedModifier
        .clip(PosterCardShape)
        .background(ObsidianCard)
        .border(1.dp, BorderSubtle, PosterCardShape)
        .clickable(onClick = onClick)
        .testTag("poster_card_${item.id}")
    }

  Box(modifier = cardModifier) {
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

    Box(modifier = Modifier.matchParentSize().background(PosterCardOverlayGradient))

    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.79f),
      ) {
        // Top-right Type pill ([TV] SERIES / MOVIE)
        Row(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .padding(8.dp)
              .clip(PosterBadgeShape)
              .background(ObsidianGlass)
              .border(0.8.dp, BorderSubtle, PosterBadgeShape)
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
            rankBadge != null -> "#$rankBadge"
            item.episodeLabel != null -> item.episodeLabel
            else -> null
          }
        if (topLeftText != null) {
          val isEpBadge = rankBadge == null && item.episodeLabel != null
          val badgeColor = if (isEpBadge) IndigoBadgeDark else ObsidianGlass
          val badgeBorderColor =
            if (isEpBadge) ToonVioletPrimary.copy(alpha = 0.45f) else BorderSubtle
          Box(
            modifier =
              Modifier.align(Alignment.TopStart)
                .padding(8.dp)
                .clip(PosterBadgeShape)
                .background(badgeColor)
                .border(0.8.dp, badgeBorderColor, PosterBadgeShape)
                .padding(horizontal = 7.dp, vertical = 3.dp),
          ) {
            Text(
              text = topLeftText,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary,
            )
          }
        }

        // Bottom Season/Episode/Subtitle + Title
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

      if (showActionFooter) {
        HorizontalDivider(
          modifier = Modifier.padding(horizontal = 10.dp),
          thickness = 1.dp,
          color = Color.White.copy(alpha = 0.14f),
        )
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier =
              Modifier.weight(1f)
                .height(31.dp)
                .clip(FooterButtonShape)
                .background(ObsidianElevated.copy(alpha = 0.94f))
                .border(1.dp, BorderBright.copy(alpha = 0.55f), FooterButtonShape)
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
          if (onToggleSave != null) {
            Box(
              contentAlignment = Alignment.Center,
              modifier =
                Modifier.size(31.dp)
                  .clip(FooterButtonShape)
                  .background(
                    if (isSaved) EmeraldLive.copy(alpha = 0.18f)
                    else ObsidianElevated.copy(alpha = 0.94f)
                  )
                  .border(
                    1.dp,
                    if (isSaved) EmeraldLive.copy(alpha = 0.72f)
                    else BorderBright.copy(alpha = 0.55f),
                    FooterButtonShape,
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
  }
}

/**
 * Circular / bidirectional 3D fanned hero carousel without overlay prev/next arrow buttons:
 * Cards appear on BOTH left and right sides immediately, and swiping or tapping side cards scrolls both ways.
 * Offset math lives purely inside graphicsLayer so horizontal swipes trigger zero recompositions.
 */
@Composable
fun FannedHeroCarousel(
  items: List<AnimeItem>,
  pagerState: PagerState,
  onItemClick: (AnimeItem) -> Unit,
  modifier: Modifier = Modifier,
  cardWidth: Dp = 198.dp,
  overlapFactor: Float = 42f,
) {
  if (items.isEmpty()) return
  val context = LocalContext.current
  val density = LocalDensity.current
  val scope = rememberCoroutineScope()
  val isReducedMotion = rememberIsReducedMotion()
  val smoothSlideEasing = remember { CubicBezierEasing(0.42f, 0.0f, 0.20f, 1.0f) }

  LaunchedEffect(pagerState, items.size, isReducedMotion) {
    if (items.size <= 1 || isReducedMotion) return@LaunchedEffect
    while (true) {
      snapshotFlow { pagerState.isScrollInProgress }.first { !it }
      val interruptedByScroll =
        withTimeoutOrNull(3800) {
          snapshotFlow { pagerState.isScrollInProgress }.first { it }
        } != null
      if (interruptedByScroll || pagerState.isScrollInProgress) continue
      val nextPage =
        if (pagerState.currentPage + 1 < pagerState.pageCount) {
          pagerState.currentPage + 1
        } else {
          items.size * 200
        }
      try {
        pagerState.animateScrollToPage(
          page = nextPage,
          animationSpec = tween(durationMillis = 820, easing = smoothSlideEasing),
        )
      } catch (_: CancellationException) {
        // User interrupted auto-scroll; resume smoothly on next idle interval.
      }
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val viewportWidth = maxWidth
    val effectiveCardWidth =
      remember(viewportWidth, cardWidth) {
        val maxAllowed = (viewportWidth * 0.56f).coerceAtLeast(110.dp)
        if (cardWidth <= maxAllowed) cardWidth else maxAllowed
      }
    val computedSidePadding =
      remember(viewportWidth, effectiveCardWidth) {
        ((viewportWidth - effectiveCardWidth) / 2f).coerceAtLeast(0.dp)
      }
    val viewportWidthPx =
      remember(density, viewportWidth) { with(density) { viewportWidth.toPx() } }
    val cardWidthPx =
      remember(density, effectiveCardWidth) { with(density) { effectiveCardWidth.toPx() } }
    val overlapPx =
      remember(density, overlapFactor, effectiveCardWidth, cardWidth) {
        val ratio = (effectiveCardWidth / cardWidth).coerceIn(0.5f, 1f)
        with(density) { (overlapFactor * ratio).dp.toPx() }
      }
    val safeMarginPx = remember(density) { with(density) { 4.dp.toPx() } }
    val minPeekSpacePx = remember(density) { with(density) { 18.dp.toPx() } }
    val halfViewportPx = viewportWidthPx / 2f
    val safeEdgePx = (halfViewportPx - safeMarginPx).coerceAtLeast(cardWidthPx * 0.52f)
    val halfCard1Px = cardWidthPx * 0.80f / 2f
    val maxDist1Px = (safeEdgePx - halfCard1Px).coerceAtLeast(cardWidthPx * 0.16f)
    val desiredDist1Px = (cardWidthPx - overlapPx).coerceAtLeast(cardWidthPx * 0.32f)
    val dist1Px = desiredDist1Px.coerceAtMost(maxDist1Px)
    val outer1Px = dist1Px + halfCard1Px
    val canShowSecondRing = (safeEdgePx - outer1Px) >= minPeekSpacePx
    val halfCard2Px = cardWidthPx * 0.65f / 2f
    val maxDist2Px = (safeEdgePx - halfCard2Px).coerceAtLeast(dist1Px)
    val dist2Px =
      if (canShowSecondRing) {
        (dist1Px + cardWidthPx * 0.30f).coerceIn(dist1Px, maxDist2Px)
      } else {
        dist1Px
      }
    val cardsPerSide = if (canShowSecondRing) 3 else 2

    HorizontalPager(
      state = pagerState,
      pageSize = PageSize.Fixed(effectiveCardWidth),
      beyondViewportPageCount = cardsPerSide,
      contentPadding = PaddingValues(horizontal = computedSidePadding),
      flingBehavior =
        PagerDefaults.flingBehavior(
          state = pagerState,
          pagerSnapDistance = PagerSnapDistance.atMost(1),
        ),
      key = { idx -> "${items[idx % items.size].uid}_$idx" },
      modifier =
        Modifier.fillMaxWidth()
          .smoothHorizontalPagerWheelScroll(pagerState, isReducedMotion)
          .testTag("hero_fanned_pager"),
    ) { page ->
      val item = items[page % items.size]
      val imageUrl = item.posterUrl.ifBlank { item.backdropUrl }
      val imageRequest =
        remember(imageUrl) {
          ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(false)
            .size(480, 710)
            .build()
        }
      val isCentered by remember(page) { derivedStateOf { page == pagerState.settledPage } }
      val borderWidth by
      animateDpAsState(
        targetValue = if (isCentered) 1.5.dp else 1.dp,
        animationSpec = tween(durationMillis = 280),
        label = "hero_border_width",
      )
      val borderColor by
      animateColorAsState(
        targetValue = if (isCentered) BorderBright else BorderSubtle,
        animationSpec = tween(durationMillis = 280),
        label = "hero_border_color",
      )

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .layout { measurable, constraints ->
              val placeable = measurable.measure(constraints)
              layout(placeable.width, placeable.height) {
                val absOffset =
                  ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    .absoluteValue
                placeable.place(0, 0, zIndex = 100f - absOffset)
              }
            }
            .graphicsLayer {
              val pageOffset =
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
              val absOffset = pageOffset.absoluteValue
              val scale =
                when {
                  absOffset <= 1f -> lerp(1.0f, 0.80f, absOffset)
                  absOffset <= 2f -> lerp(0.80f, 0.65f, absOffset - 1f)
                  else -> (0.65f - (absOffset - 2f) * 0.12f).coerceAtLeast(0.48f)
                }
              val alphaVal =
                if (canShowSecondRing) {
                  when {
                    absOffset <= 1f -> lerp(1.0f, 0.72f, absOffset)
                    absOffset <= 2f -> lerp(0.72f, 0.44f, absOffset - 1f)
                    absOffset <= 2.55f -> lerp(0.44f, 0.0f, (absOffset - 2f) / 0.55f)
                    else -> 0.0f
                  }
                } else {
                  when {
                    absOffset <= 1f -> lerp(1.0f, 0.70f, absOffset)
                    absOffset <= 1.55f -> lerp(0.70f, 0.0f, (absOffset - 1f) / 0.55f)
                    else -> 0.0f
                  }
                }
              val targetCenterDistPx =
                when {
                  absOffset <= 1f -> lerp(0f, dist1Px, absOffset)
                  absOffset <= 2f -> lerp(dist1Px, dist2Px, absOffset - 1f)
                  else -> dist2Px
                }
              val inwardShiftPx = (absOffset * cardWidthPx) - targetCenterDistPx
              scaleX = scale
              scaleY = scale
              alpha = alphaVal
              translationX = pageOffset.sign * inwardShiftPx
              rotationY = pageOffset.coerceIn(-1.25f, 1.25f) * 8f
              cameraDistance = 16f * this.density
            },
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier =
            Modifier.width(effectiveCardWidth)
              .aspectRatio(0.68f)
              .clip(HeroCardShape)
              .background(ObsidianCard)
              .border(
                width = borderWidth,
                color = borderColor,
                shape = HeroCardShape,
              )
              .onFocusChanged { focusState ->
                if (focusState.isFocused && page != pagerState.currentPage) {
                  scope.launch {
                    if (isReducedMotion) {
                      pagerState.scrollToPage(page)
                    } else {
                      pagerState.animateScrollToPage(
                        page = page,
                        animationSpec = tween(durationMillis = 520, easing = smoothSlideEasing),
                      )
                    }
                  }
                }
              }
              .clickable {
                if (page == pagerState.currentPage) {
                  onItemClick(item)
                } else {
                  scope.launch {
                    if (isReducedMotion) {
                      pagerState.scrollToPage(page)
                    } else {
                      pagerState.animateScrollToPage(
                        page = page,
                        animationSpec = tween(durationMillis = 640, easing = smoothSlideEasing),
                      )
                    }
                  }
                }
              }
              .testTag("hero_carousel_card_${page % items.size}"),
        ) {
          val heroPlaceholder = painterResource(id = R.drawable.poster_placeholder)
          AsyncImage(
            model = imageRequest,
            contentDescription = item.title,
            placeholder = heroPlaceholder,
            error = heroPlaceholder,
            fallback = heroPlaceholder,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )

          Box(modifier = Modifier.fillMaxSize().background(HeroCardOverlayGradient))

          if (!item.rating.isNullOrBlank()) {
            Row(
              modifier =
                Modifier.align(Alignment.TopEnd)
                  .padding(12.dp)
                  .clip(HeroBadgeShape)
                  .background(ObsidianGlass)
                  .border(1.dp, BorderSubtle, HeroBadgeShape)
                  .padding(horizontal = 9.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = item.rating,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CarouselDotIndicator(
  count: Int,
  currentIndex: Int,
  modifier: Modifier = Modifier,
) {
  val safeCount = count.coerceAtLeast(1)
  val activeMod = ((currentIndex % safeCount) + safeCount) % safeCount
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(safeCount.coerceAtMost(10)) { index ->
      val active = index == activeMod
      Box(
        modifier =
          Modifier.height(6.dp)
            .width(if (active) 22.dp else 6.dp)
            .clip(CircleShape)
            .background(if (active) ToonVioletPrimary else TextSecondary.copy(alpha = 0.35f))
      )
    }
  }
}

@Composable
fun BlinkingGreenDot(modifier: Modifier = Modifier) {
  val isReducedMotion = rememberIsReducedMotion()
  val blinkAlpha =
    if (isReducedMotion) {
      1.0f
    } else {
      val transition = rememberInfiniteTransition(label = "latest_live_dot")
      val animatedAlpha by
      transition.animateFloat(
        initialValue = 0.22f,
        targetValue = 1.0f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 640, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
          ),
        label = "latest_dot_alpha",
      )
      animatedAlpha
    }
  Canvas(modifier = modifier.size(7.dp)) {
    drawCircle(color = EmeraldLive.copy(alpha = blinkAlpha))
  }
}