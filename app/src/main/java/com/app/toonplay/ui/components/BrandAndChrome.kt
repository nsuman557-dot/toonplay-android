package com.app.toonplay.ui.components

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.toonplay.R
import com.app.toonplay.data.model.AnimeSectionTab
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.ui.theme.AmberGold
import com.app.toonplay.ui.theme.AmberSoft
import com.app.toonplay.ui.theme.BorderBright
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.GlassCircleSurface
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.ObsidianPill
import com.app.toonplay.ui.theme.ObsidianSurface
import com.app.toonplay.ui.theme.TextMuted
import com.app.toonplay.ui.theme.TextOffWhite
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonIndigoAccent
import com.app.toonplay.ui.theme.ToonVioletPrimary

@Immutable
data class ToonPlayNavItem(
  val tab: AnimeSectionTab,
  @DrawableRes val iconRes: Int,
  @StringRes val labelRes: Int,
)

val ToonPlayNavItems: List<ToonPlayNavItem> =
  listOf(
    ToonPlayNavItem(AnimeSectionTab.HOME, R.drawable.ic_nav_home, R.string.nav_home),
    ToonPlayNavItem(AnimeSectionTab.ANIME, R.drawable.ic_nav_anime, R.string.nav_anime),
    ToonPlayNavItem(AnimeSectionTab.CARTOON, R.drawable.ic_nav_cartoon, R.string.nav_cartoon),
    ToonPlayNavItem(AnimeSectionTab.MOVIES, R.drawable.ic_nav_movies, R.string.nav_movies),
    ToonPlayNavItem(AnimeSectionTab.PROFILE, R.drawable.ic_nav_profile, R.string.nav_profile),
  )

@Composable
fun ToonPlayWordmark(
  modifier: Modifier = Modifier,
  compact: Boolean = false,
  height: Dp = if (compact) 23.dp else 28.dp,
) {
  Image(
    painter = painterResource(id = R.drawable.toonplay_logo),
    contentDescription = stringResource(R.string.app_name),
    contentScale = ContentScale.Fit,
    modifier = modifier.height(height).testTag("toonplay_wordmark_logo"),
  )
}

@Composable
fun GlassCircleIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "",
  tint: Color = TextPrimary,
  containerColor: Color = Color(0x3D1E233A),
  shape: Shape = CircleShape,
  size: Dp = 42.dp,
) {
  Surface(
    onClick = onClick,
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .size(size)
        .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
    shape = shape,
    color = containerColor,
    border =
      BorderStroke(
        width = 1.dp,
        brush =
          Brush.linearGradient(
            listOf(
              Color.White.copy(alpha = 0.55f),
              Color.White.copy(alpha = 0.14f),
              Color.White.copy(alpha = 0.30f),
            )
          ),
      ),
    shadowElevation = 8.dp,
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.linearGradient(
              listOf(
                Color.White.copy(alpha = 0.26f),
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = 0.14f),
              )
            )
          ),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

private val AmbientBackdropScrim =
  Brush.verticalGradient(
    0.0f to ObsidianBg.copy(alpha = 0.62f),
    0.42f to ObsidianSurface.copy(alpha = 0.40f),
    0.76f to ObsidianBg.copy(alpha = 0.90f),
    1.0f to ObsidianBg,
  )

@Composable
fun AmbientHeroBackdrop(
  imageUrl: String?,
  modifier: Modifier = Modifier,
  height: Dp = 460.dp,
) {
  val context = LocalContext.current
  Box(modifier = modifier.fillMaxWidth().height(height)) {
    Crossfade(
      targetState = imageUrl.orEmpty(),
      animationSpec = tween(durationMillis = 280),
      label = "hero_ambient_crossfade",
    ) { url ->
      if (url.isNotBlank()) {
        // Request a tiny thumbnail so hardware bilinear filtering provides a silky-smooth
        // ambient backdrop blur without any expensive GPU RenderEffect blur pass.
        val request =
          remember(url) {
            ImageRequest.Builder(context)
              .data(url)
              .crossfade(false)
              .size(64, 48)
              .build()
          }
        AsyncImage(
          model = request,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize().alpha(0.36f),
        )
      }
    }

    Box(modifier = Modifier.fillMaxSize().background(AmbientBackdropScrim))
  }
}

@Composable
fun MetadataPill(
  text: String,
  modifier: Modifier = Modifier,
  containerColor: Color = ObsidianPill,
  contentColor: Color = TextOffWhite,
  borderColor: Color = BorderSubtle,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      color = contentColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
    )
  }
}

@Composable
fun StarRatingPill(
  rating: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = AmberSoft,
    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.45f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Star,
        contentDescription = null,
        tint = AmberGold,
        modifier = Modifier.size(13.dp),
      )
      Text(
        text = rating,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = AmberGold,
      )
    }
  }
}

@Composable
fun SeeAllArrowButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  label: String = stringResource(R.string.action_see_all),
) {
  Surface(
    onClick = onClick,
    modifier = modifier.height(32.dp).testTag("see_all_button"),
    shape = RoundedCornerShape(10.dp),
    color = ObsidianCard,
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Row(
      modifier = Modifier.padding(start = 11.dp, end = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = TextOffWhite,
        maxLines = 1,
      )
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        tint = TextOffWhite,
        modifier = Modifier.size(14.dp),
      )
    }
  }
}

@Composable
fun ViewModeTogglePill(
  isGridMode: Boolean,
  onToggleGridMode: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.height(32.dp).testTag("view_mode_toggle_pill"),
    shape = RoundedCornerShape(10.dp),
    color = ObsidianSurface,
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Row(
      modifier = Modifier.padding(3.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        onClick = { onToggleGridMode(true) },
        shape = RoundedCornerShape(7.dp),
        color = if (isGridMode) ToonVioletPrimary else Color.Transparent,
        modifier = Modifier.size(26.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Rounded.GridView,
            contentDescription = "Grid view",
            tint = if (isGridMode) TextPrimary else TextOffWhite,
            modifier = Modifier.size(15.dp),
          )
        }
      }
      Surface(
        onClick = { onToggleGridMode(false) },
        shape = RoundedCornerShape(7.dp),
        color = if (!isGridMode) ToonVioletPrimary else Color.Transparent,
        modifier = Modifier.size(26.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Rounded.ViewCarousel,
            contentDescription = "Scroll view",
            tint = if (!isGridMode) TextPrimary else TextOffWhite,
            modifier = Modifier.size(15.dp),
          )
        }
      }
    }
  }
}

@Composable
fun SectionHeaderRow(
  title: String,
  kicker: String? = null,
  itemCount: Int? = null,
  onSeeAll: (() -> Unit)? = null,
  isGridMode: Boolean? = null,
  onToggleGridMode: ((Boolean) -> Unit)? = null,
  trailingLeadingControls: (@Composable () -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      if (!kicker.isNullOrBlank()) {
        val kickerText =
          if (itemCount != null) "$kicker  •  $itemCount titles" else kicker
        Text(
          text = kickerText,
          style = MaterialTheme.typography.labelMedium,
          color = TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
      }
      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    if (onSeeAll != null || (isGridMode != null && onToggleGridMode != null) || trailingLeadingControls != null) {
      Spacer(modifier = Modifier.width(8.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        trailingLeadingControls?.invoke()
        // Grid/Scroll toggle on LEFT, See All on RIGHT
        if (isGridMode != null && onToggleGridMode != null) {
          ViewModeTogglePill(isGridMode = isGridMode, onToggleGridMode = onToggleGridMode)
        }
        if (onSeeAll != null) {
          SeeAllArrowButton(onClick = onSeeAll)
        }
      }
    }
  }
}

@Composable
fun LatestControlsBar(
  selectedFilter: LatestLanguageFilter,
  onSelectFilter: (LatestLanguageFilter) -> Unit,
  onSeeAll: () -> Unit,
  isGridMode: Boolean,
  onToggleGridMode: (Boolean) -> Unit,
  trailingLeadingControls: (@Composable () -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.fillMaxWidth(),
  ) {
    // Far Left: Compact Segmented Language / Source Pill (JAP/ENG | HIN/TAM/TEL)
    Surface(
      modifier = Modifier.height(32.dp),
      shape = RoundedCornerShape(10.dp),
      color = ObsidianSurface,
      border = BorderStroke(1.dp, BorderSubtle),
    ) {
      Row(
        modifier = Modifier.padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LatestLanguageFilter.entries.forEach { filter ->
          val selected = filter == selectedFilter
          Surface(
            onClick = { onSelectFilter(filter) },
            shape = RoundedCornerShape(7.dp),
            color = if (selected) ToonVioletPrimary else Color.Transparent,
            modifier = Modifier.height(26.dp).testTag("lang_filter_${filter.name.lowercase()}"),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(horizontal = 8.dp),
            ) {
              Text(
                text = filter.label,
                style =
                  MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                  ),
                color = if (selected) TextPrimary else TextOffWhite,
                maxLines = 1,
              )
            }
          }
        }
      }
    }

    // Far Right: Scroll chevrons (when enabled), Grid/Scroll toggle on LEFT, See All on RIGHT
    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      trailingLeadingControls?.invoke()
      ViewModeTogglePill(isGridMode = isGridMode, onToggleGridMode = onToggleGridMode)
      SeeAllArrowButton(onClick = onSeeAll)
    }
  }
}

@Composable
fun ToonPlaySplashScreen(modifier: Modifier = Modifier) {
  var started by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) { started = true }

  val iconScale by
  animateFloatAsState(
    targetValue = if (started) 1f else 0.78f,
    animationSpec =
      spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
      ),
    label = "splash_icon_scale",
  )
  val contentAlpha by
  animateFloatAsState(
    targetValue = if (started) 1f else 0f,
    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
    label = "splash_alpha",
  )
  val arcSweepProgress by
  animateFloatAsState(
    targetValue = if (started) 0.84f else 0.08f,
    animationSpec = tween(durationMillis = 920, easing = FastOutSlowInEasing),
    label = "splash_round_fill",
  )

  val pulseTransition = rememberInfiniteTransition(label = "splash_glow_pulse")
  val glowAlpha by
  pulseTransition.animateFloat(
    initialValue = 0.28f,
    targetValue = 0.58f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
    label = "splash_glow_alpha",
  )
  val rotationAngle by
  pulseTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = 2200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
      ),
    label = "splash_round_rotation",
  )

  val splashBgBrush =
    remember {
      Brush.radialGradient(
        colors = listOf(Color(0xFF1F124D), ObsidianBg),
        radius = 900f,
      )
    }
  val ringBrush =
    remember {
      Brush.sweepGradient(
        colors =
          listOf(
            ToonIndigoAccent.copy(alpha = 0.25f),
            ToonVioletPrimary,
            Color(0xFFA799FF),
            ToonIndigoAccent.copy(alpha = 0.25f),
          )
      )
    }

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxSize()
        .background(splashBgBrush)
        .testTag("toonplay_splash_screen"),
    contentAlignment = Alignment.Center,
  ) {
    val availableWidth = this.maxWidth
    val shortestSide = minOf(availableWidth, maxHeight)
    val iconSize = (shortestSide * 0.28f).coerceIn(104.dp, 148.dp)
    val glowSize = iconSize * 1.44f
    val iconCorner = (iconSize * 0.24f).coerceIn(24.dp, 36.dp)
    val progressSize = (shortestSide * 0.135f).coerceIn(48.dp, 68.dp)
    val strokeWidth = (progressSize * 0.105f).coerceIn(4.5.dp, 6.8.dp)
    val verticalGap = (shortestSide * 0.08f).coerceIn(26.dp, 38.dp)

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(verticalGap),
      modifier = Modifier.alpha(contentAlpha),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Box(
          modifier =
            Modifier.size(glowSize)
              .graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
              }
              .alpha(glowAlpha)
              .background(
                Brush.radialGradient(
                  colors =
                    listOf(
                      ToonVioletPrimary.copy(alpha = 0.72f),
                      Color.Transparent,
                    )
                ),
                shape = CircleShape,
              )
        )
        Image(
          painter = painterResource(id = R.drawable.ic_toonplay_app_icon),
          contentDescription = stringResource(R.string.app_name),
          contentScale = ContentScale.Fit,
          modifier =
            Modifier.size(iconSize)
              .graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
              }
              .clip(RoundedCornerShape(iconCorner))
              .testTag("splash_app_icon"),
        )
      }

      // Responsive custom round progress ring (no default spinner style)
      Canvas(
        modifier = Modifier.size(progressSize).testTag("splash_spinner"),
      ) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val arcSize = size.copy(width = size.width - strokePx, height = size.height - strokePx)
        val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)

        drawArc(
          color = ObsidianElevated,
          startAngle = 0f,
          sweepAngle = 360f,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokePx),
        )
        drawArc(
          color = BorderSubtle.copy(alpha = 0.75f),
          startAngle = 0f,
          sweepAngle = 360f,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = 1.dp.toPx()),
        )

        rotate(degrees = rotationAngle - 90f) {
          drawArc(
            brush = ringBrush,
            startAngle = 0f,
            sweepAngle = 360f * arcSweepProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
          )
        }
      }
    }
  }
}

@Composable
fun FeedLoadingView(modifier: Modifier = Modifier) {
  ToonPlaySplashScreen(modifier = modifier)
}

@Composable
fun FeedErrorView(
  message: String,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize().background(ObsidianBg).padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = ObsidianCard,
      border = BorderStroke(1.dp, BorderSubtle),
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.ErrorOutline,
          contentDescription = null,
          tint = ToonVioletPrimary,
          modifier = Modifier.size(44.dp),
        )
        Text(
          text = stringResource(R.string.error_feed_title),
          style = MaterialTheme.typography.titleLarge,
          color = TextPrimary,
          textAlign = TextAlign.Center,
        )
        Text(
          text = message.ifBlank { stringResource(R.string.error_feed_fallback) },
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
          onClick = onRetry,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("retry_feed_button"),
          colors = ButtonDefaults.buttonColors(containerColor = ToonVioletPrimary),
          shape = RoundedCornerShape(50),
        ) {
          Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.action_retry),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}

@Composable
fun EmptyCollectionView(
  title: String,
  subtitle: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Box(
      modifier =
        Modifier.size(64.dp)
          .clip(CircleShape)
          .background(ObsidianCard)
          .border(1.dp, BorderSubtle, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = TextMuted,
        modifier = Modifier.size(30.dp),
      )
    }
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = TextPrimary,
      textAlign = TextAlign.Center,
    )
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      textAlign = TextAlign.Center,
    )
  }
}
