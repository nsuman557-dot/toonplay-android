package com.app.toonplay.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.toonplay.data.model.DeviceFormFactor
import com.app.toonplay.ui.theme.BorderBright
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianElevated
import com.app.toonplay.ui.theme.ObsidianGlass
import com.app.toonplay.ui.theme.ObsidianSurface

@Composable
private fun rememberSkeletonBrush(): Brush {
  val transition = rememberInfiniteTransition(label = "home_skeleton_shimmer")
  val shift by
  transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = 1250, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
      ),
    label = "shimmer_shift",
  )
  return Brush.linearGradient(
    colors =
      listOf(
        ObsidianCard,
        ObsidianElevated,
        Color(0xFF232740),
        ObsidianElevated,
        ObsidianCard,
      ),
    start = Offset(shift - 420f, 0f),
    end = Offset(shift, 360f),
  )
}

@Composable
private fun SkeletonBox(
  brush: Brush,
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(10.dp),
) {
  Box(
    modifier = modifier.clip(shape).background(brush).border(0.6.dp, BorderSubtle.copy(alpha = 0.5f), shape)
  )
}

@Composable
fun HomeSkeletonLoader(
  formFactor: DeviceFormFactor,
  modifier: Modifier = Modifier,
) {
  val brush = rememberSkeletonBrush()
  Box(
    modifier = modifier.fillMaxSize().background(ObsidianBg).testTag("home_skeleton_loader"),
  ) {
    when (formFactor) {
      DeviceFormFactor.MOBILE -> MobileHomeSkeleton(brush = brush)
      DeviceFormFactor.TABLET -> TabletHomeSkeleton(brush = brush)
      DeviceFormFactor.BIG_TABLET -> BigTabletHomeSkeleton(brush = brush)
      DeviceFormFactor.DESKTOP -> DesktopHomeSkeleton(brush = brush)
      DeviceFormFactor.TV -> TvHomeSkeleton(brush = brush)
    }
  }
}

@Composable
private fun MobileHomeSkeleton(brush: Brush) {
  val bottomBarClearance =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 96.dp

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      userScrollEnabled = false,
      contentPadding = PaddingValues(bottom = bottomBarClearance),
      verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
      item {
        Column(
          modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 64.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          HeroCarouselSkeleton(brush = brush, cardWidth = 198.dp)
          Spacer(modifier = Modifier.height(16.dp))
          Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 138.dp),
            contentAlignment = Alignment.TopCenter,
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              SkeletonBox(brush = brush, modifier = Modifier.width(108.dp).height(14.dp), shape = CircleShape)
              SkeletonBox(brush = brush, modifier = Modifier.width(230.dp).height(28.dp), shape = RoundedCornerShape(8.dp))
              SkeletonBox(brush = brush, modifier = Modifier.width(58.dp).height(24.dp), shape = CircleShape)
              SkeletonBox(brush = brush, modifier = Modifier.fillMaxWidth(0.88f).height(13.dp))
              SkeletonBox(brush = brush, modifier = Modifier.fillMaxWidth(0.66f).height(13.dp))
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          DotsSkeletonRow(brush = brush)
        }
      }

      item {
        SectionRowSkeleton(
          brush = brush,
          cardWidth = 158.dp,
          horizontalPadding = 16.dp,
          itemSpacing = 12.dp,
          showLatestControls = true,
          showHeaderActions = false,
        )
      }
      item {
        SectionRowSkeleton(
          brush = brush,
          cardWidth = 158.dp,
          horizontalPadding = 16.dp,
          itemSpacing = 12.dp,
          showLatestControls = false,
          showHeaderActions = true,
        )
      }
    }

    // Mobile Top Header Skeleton matching MobileTopHeader
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .align(Alignment.TopCenter)
          .statusBarsPadding()
          .padding(start = 16.dp, end = 16.dp, top = 3.dp, bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      SkeletonBox(brush = brush, modifier = Modifier.width(96.dp).height(23.dp), shape = RoundedCornerShape(8.dp))
      SkeletonBox(brush = brush, modifier = Modifier.size(42.dp), shape = CircleShape)
    }

    // Bottom Floating Bar Skeleton matching MobileFloatingBottomBar (5 items)
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        shape = RoundedCornerShape(32.dp),
        color = ObsidianGlass,
        border = BorderStroke(1.dp, BorderBright.copy(alpha = 0.45f)),
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          repeat(ToonPlayNavItems.size) {
            Column(
              modifier = Modifier.weight(1f).padding(vertical = 7.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              SkeletonBox(brush = brush, modifier = Modifier.size(22.dp), shape = CircleShape)
              SkeletonBox(brush = brush, modifier = Modifier.width(34.dp).height(9.dp), shape = CircleShape)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun NavigationRailSkeleton(brush: Brush) {
  Column(
    modifier =
      Modifier.width(80.dp)
        .fillMaxHeight()
        .background(ObsidianSurface)
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(top = 18.dp, bottom = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(22.dp),
  ) {
    repeat(ToonPlayNavItems.size) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
      ) {
        SkeletonBox(brush = brush, modifier = Modifier.size(24.dp), shape = CircleShape)
        SkeletonBox(brush = brush, modifier = Modifier.width(36.dp).height(8.dp), shape = CircleShape)
      }
    }
  }
}

@Composable
private fun TabletHomeSkeleton(brush: Brush) {
  Row(modifier = Modifier.fillMaxSize()) {
    NavigationRailSkeleton(brush = brush)
    Column(modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding()) {
      Row(
        modifier =
          Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SkeletonBox(brush = brush, modifier = Modifier.width(114.dp).height(26.dp), shape = RoundedCornerShape(8.dp))
        SkeletonBox(brush = brush, modifier = Modifier.size(42.dp), shape = CircleShape)
      }
      LazyColumn(
        userScrollEnabled = false,
        contentPadding = PaddingValues(top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
      ) {
        item {
          BannerShowcaseSkeleton(
            brush = brush,
            cardWidth = 166.dp,
            horizontalPadding = 24.dp,
            height = 340.dp,
            carouselOnLeft = true,
            showDotsUnderCarousel = true,
          )
        }
        item {
          SectionRowSkeleton(
            brush = brush,
            cardWidth = 164.dp,
            horizontalPadding = 24.dp,
            itemSpacing = 14.dp,
            showLatestControls = true,
            showHeaderActions = false,
          )
        }
        item {
          SectionRowSkeleton(
            brush = brush,
            cardWidth = 160.dp,
            horizontalPadding = 24.dp,
            itemSpacing = 14.dp,
            showLatestControls = false,
            showHeaderActions = true,
          )
        }
      }
    }
  }
}

@Composable
private fun BigTabletHomeSkeleton(brush: Brush) {
  Row(modifier = Modifier.fillMaxSize()) {
    NavigationRailSkeleton(brush = brush)
    Column(modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding()) {
      Row(
        modifier =
          Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SkeletonBox(brush = brush, modifier = Modifier.width(118.dp).height(27.dp), shape = RoundedCornerShape(8.dp))
        Spacer(modifier = Modifier.weight(0.15f))
        SkeletonBox(brush = brush, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(50))
      }
      LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
        userScrollEnabled = false,
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
      ) {
        item {
          BannerShowcaseSkeleton(
            brush = brush,
            cardWidth = 172.dp,
            horizontalPadding = 0.dp,
            height = 340.dp,
            carouselOnLeft = false,
            showDotsUnderCarousel = true,
          )
        }
        item {
          SectionRowSkeleton(
            brush = brush,
            cardWidth = 164.dp,
            horizontalPadding = 0.dp,
            itemSpacing = 14.dp,
            showLatestControls = true,
            showHeaderActions = false,
          )
        }
        item {
          SectionRowSkeleton(
            brush = brush,
            cardWidth = 160.dp,
            horizontalPadding = 0.dp,
            itemSpacing = 14.dp,
            showLatestControls = false,
            showHeaderActions = true,
          )
        }
      }
    }
  }
}

@Composable
private fun DesktopHomeSkeleton(brush: Brush) {
  Row(modifier = Modifier.fillMaxSize()) {
    Surface(
      color = ObsidianSurface,
      border = BorderStroke(1.dp, BorderSubtle),
      modifier = Modifier.width(224.dp).fillMaxHeight(),
    ) {
      Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
          SkeletonBox(brush = brush, modifier = Modifier.width(116.dp).height(26.dp), shape = RoundedCornerShape(8.dp))
        }
        HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
        repeat(ToonPlayNavItems.size) {
          SkeletonBox(
            brush = brush,
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(12.dp),
          )
        }
        Spacer(modifier = Modifier.weight(1f))
      }
    }
    Column(modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding()) {
      Row(
        modifier =
          Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SkeletonBox(brush = brush, modifier = Modifier.width(84.dp).height(26.dp))
        Spacer(modifier = Modifier.width(8.dp))
        SkeletonBox(brush = brush, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(50))
        SkeletonBox(brush = brush, modifier = Modifier.width(94.dp).height(38.dp), shape = RoundedCornerShape(12.dp))
      }
      LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
        userScrollEnabled = false,
        contentPadding = PaddingValues(top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
      ) {
        item {
          BannerShowcaseSkeleton(
            brush = brush,
            cardWidth = 166.dp,
            horizontalPadding = 0.dp,
            height = 340.dp,
            carouselOnLeft = false,
            showDotsNextToButton = true,
            leftWeight = 0.52f,
          )
        }
        item {
          SectionRowSkeleton(
            brush = brush,
            cardWidth = 168.dp,
            horizontalPadding = 0.dp,
            itemSpacing = 16.dp,
            showLatestControls = true,
            showHeaderActions = false,
          )
        }
        item {
          SectionRowSkeleton(
            brush = brush,
            cardWidth = 164.dp,
            horizontalPadding = 0.dp,
            itemSpacing = 16.dp,
            showLatestControls = false,
            showHeaderActions = true,
          )
        }
      }
    }
  }
}

@Composable
private fun TvHomeSkeleton(brush: Brush) {
  Row(modifier = Modifier.fillMaxSize()) {
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
        repeat(ToonPlayNavItems.size) {
          SkeletonBox(
            brush = brush,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
          )
        }
        Spacer(modifier = Modifier.weight(1f))
        SkeletonBox(
          brush = brush,
          modifier = Modifier.fillMaxWidth().height(56.dp),
          shape = RoundedCornerShape(16.dp),
        )
      }
    }
    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 42.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SkeletonBox(brush = brush, modifier = Modifier.width(134.dp).height(30.dp), shape = RoundedCornerShape(8.dp))
      }
      LazyColumn(
        userScrollEnabled = false,
        contentPadding = PaddingValues(top = 14.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
      ) {
        item {
          BannerShowcaseSkeleton(
            brush = brush,
            cardWidth = 152.dp,
            horizontalPadding = 42.dp,
            height = 310.dp,
            carouselOnLeft = false,
            showDotsNextToButton = true,
            leftWeight = 0.52f,
          )
        }
        item {
          SectionRowSkeleton(
            brush = brush,
            cardWidth = 176.dp,
            horizontalPadding = 42.dp,
            itemSpacing = 18.dp,
            sectionSpacing = 14.dp,
            showLatestControls = false,
            showHeaderActions = false,
            inlineTvLatestControls = true,
          )
        }
        item {
          SectionRowSkeleton(
            brush = brush,
            cardWidth = 176.dp,
            horizontalPadding = 42.dp,
            itemSpacing = 18.dp,
            sectionSpacing = 14.dp,
            showLatestControls = false,
            showHeaderActions = true,
          )
        }
      }
    }
  }
}

@Composable
private fun DotsSkeletonRow(brush: Brush) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    SkeletonBox(brush = brush, modifier = Modifier.width(22.dp).height(6.dp), shape = CircleShape)
    repeat(5) {
      SkeletonBox(brush = brush, modifier = Modifier.size(6.dp), shape = CircleShape)
    }
  }
}

@Composable
private fun HeroCarouselSkeleton(brush: Brush, cardWidth: Dp) {
  BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val viewportWidth = this.maxWidth
    val effectiveWidth = minOf(cardWidth, (viewportWidth * 0.56f).coerceAtLeast(110.dp))
    val sideWidth = effectiveWidth * 0.80f
    val maxSideOffset = ((viewportWidth - sideWidth) / 2f - 4.dp).coerceAtLeast(12.dp)
    val sideOffset = minOf(effectiveWidth * 0.62f, maxSideOffset)

    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      SkeletonBox(
        brush = brush,
        modifier = Modifier.padding(end = sideOffset * 2f).width(sideWidth).aspectRatio(0.68f),
        shape = RoundedCornerShape(24.dp),
      )
      SkeletonBox(
        brush = brush,
        modifier = Modifier.padding(start = sideOffset * 2f).width(sideWidth).aspectRatio(0.68f),
        shape = RoundedCornerShape(24.dp),
      )
      SkeletonBox(
        brush = brush,
        modifier = Modifier.width(effectiveWidth).aspectRatio(0.68f),
        shape = RoundedCornerShape(24.dp),
      )
    }
  }
}

@Composable
private fun BannerShowcaseSkeleton(
  brush: Brush,
  cardWidth: Dp,
  horizontalPadding: Dp,
  height: Dp = 340.dp,
  carouselOnLeft: Boolean = false,
  showDotsUnderCarousel: Boolean = false,
  showDotsNextToButton: Boolean = false,
  leftWeight: Float = 0.50f,
) {
  Surface(
    shape = RoundedCornerShape(26.dp),
    color = ObsidianCard,
    border = BorderStroke(1.dp, BorderSubtle),
    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding).height(height),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      val carouselWeight = (1f - leftWeight).coerceIn(0.4f, 0.6f)
      if (carouselOnLeft) {
        Column(
          modifier = Modifier.weight(carouselWeight),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          HeroCarouselSkeleton(brush = brush, cardWidth = cardWidth)
          if (showDotsUnderCarousel) {
            Spacer(modifier = Modifier.height(10.dp))
            DotsSkeletonRow(brush = brush)
          }
        }
      }

      Column(
        modifier = Modifier.weight(leftWeight),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          SkeletonBox(brush = brush, modifier = Modifier.width(56.dp).height(24.dp), shape = CircleShape)
          SkeletonBox(brush = brush, modifier = Modifier.width(56.dp).height(24.dp), shape = CircleShape)
          SkeletonBox(brush = brush, modifier = Modifier.width(64.dp).height(24.dp), shape = CircleShape)
        }
        SkeletonBox(brush = brush, modifier = Modifier.fillMaxWidth(0.82f).height(30.dp))
        SkeletonBox(brush = brush, modifier = Modifier.fillMaxWidth(0.95f).height(14.dp))
        SkeletonBox(brush = brush, modifier = Modifier.fillMaxWidth(0.72f).height(14.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          SkeletonBox(brush = brush, modifier = Modifier.width(138.dp).height(42.dp), shape = RoundedCornerShape(14.dp))
          if (showDotsNextToButton) {
            DotsSkeletonRow(brush = brush)
          }
        }
      }

      if (!carouselOnLeft) {
        Column(
          modifier = Modifier.weight(carouselWeight),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          HeroCarouselSkeleton(brush = brush, cardWidth = cardWidth)
          if (showDotsUnderCarousel) {
            Spacer(modifier = Modifier.height(10.dp))
            DotsSkeletonRow(brush = brush)
          }
        }
      }
    }
  }
}

@Composable
private fun SectionRowSkeleton(
  brush: Brush,
  cardWidth: Dp,
  horizontalPadding: Dp,
  itemSpacing: Dp = 12.dp,
  sectionSpacing: Dp = 12.dp,
  showLatestControls: Boolean,
  showHeaderActions: Boolean = true,
  inlineTvLatestControls: Boolean = false,
) {
  Column(verticalArrangement = Arrangement.spacedBy(sectionSpacing)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        SkeletonBox(brush = brush, modifier = Modifier.width(128.dp).height(13.dp), shape = CircleShape)
        SkeletonBox(brush = brush, modifier = Modifier.width(216.dp).height(24.dp), shape = RoundedCornerShape(6.dp))
      }
      if (inlineTvLatestControls) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          SkeletonBox(brush = brush, modifier = Modifier.width(84.dp).height(34.dp), shape = RoundedCornerShape(12.dp))
          SkeletonBox(brush = brush, modifier = Modifier.width(106.dp).height(34.dp), shape = RoundedCornerShape(12.dp))
          SkeletonBox(brush = brush, modifier = Modifier.width(78.dp).height(34.dp), shape = RoundedCornerShape(12.dp))
          SkeletonBox(brush = brush, modifier = Modifier.width(76.dp).height(34.dp), shape = RoundedCornerShape(12.dp))
        }
      } else if (showHeaderActions) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          SkeletonBox(brush = brush, modifier = Modifier.width(58.dp).height(32.dp), shape = RoundedCornerShape(10.dp))
          SkeletonBox(brush = brush, modifier = Modifier.width(76.dp).height(32.dp), shape = RoundedCornerShape(10.dp))
        }
      }
    }
    if (showLatestControls) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SkeletonBox(brush = brush, modifier = Modifier.width(166.dp).height(32.dp), shape = RoundedCornerShape(10.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          SkeletonBox(brush = brush, modifier = Modifier.width(58.dp).height(32.dp), shape = RoundedCornerShape(10.dp))
          SkeletonBox(brush = brush, modifier = Modifier.width(76.dp).height(32.dp), shape = RoundedCornerShape(10.dp))
        }
      }
    }
    LazyRow(
      userScrollEnabled = false,
      contentPadding = PaddingValues(horizontal = horizontalPadding),
      horizontalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
      items(6) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = ObsidianCard,
          border = BorderStroke(1.dp, BorderSubtle),
          modifier = Modifier.width(cardWidth),
        ) {
          Column {
            SkeletonBox(
              brush = brush,
              modifier = Modifier.fillMaxWidth().aspectRatio(0.79f),
              shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            )
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
              SkeletonBox(brush = brush, modifier = Modifier.weight(1f).height(31.dp), shape = RoundedCornerShape(9.dp))
              SkeletonBox(brush = brush, modifier = Modifier.size(31.dp), shape = RoundedCornerShape(9.dp))
            }
          }
        }
      }
    }
  }
}