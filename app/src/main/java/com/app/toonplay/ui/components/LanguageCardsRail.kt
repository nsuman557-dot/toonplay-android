package com.app.toonplay.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.app.toonplay.R
import com.app.toonplay.data.model.DeviceFormFactor
import com.app.toonplay.data.model.LatestLanguageFilter
import com.app.toonplay.ui.theme.ObsidianBg
import com.app.toonplay.ui.theme.TextPrimary
import com.app.toonplay.ui.theme.TextSecondary
import com.app.toonplay.ui.theme.ToonVioletPrimary

/** Exact native 1774 x 887 px (2:1) aspect ratio shared across all 9 language cards. */
const val LANGUAGE_CARD_ASPECT_RATIO: Float = 1774f / 887f

@Immutable
data class LanguageCardSpec(
    val id: String,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val drawableRes: Int,
    val latestFilter: LatestLanguageFilter,
)

/**
 * Ordered language banner cards for the homepage sliding rail:
 * Hindi, Telugu, Tamil, Malayalam, Kannada, Bengali, English, Japanese, Korean.
 */
val ToonPlayLanguageCards: List<LanguageCardSpec> =
    listOf(
        LanguageCardSpec(
            id = "hindi",
            labelRes = R.string.lang_card_hindi,
            drawableRes = R.drawable.lang_card_hindi,
            latestFilter = LatestLanguageFilter.HIN_TAM_TEL,
        ),
        LanguageCardSpec(
            id = "telugu",
            labelRes = R.string.lang_card_telugu,
            drawableRes = R.drawable.lang_card_telugu,
            latestFilter = LatestLanguageFilter.HIN_TAM_TEL,
        ),
        LanguageCardSpec(
            id = "tamil",
            labelRes = R.string.lang_card_tamil,
            drawableRes = R.drawable.lang_card_tamil,
            latestFilter = LatestLanguageFilter.HIN_TAM_TEL,
        ),
        LanguageCardSpec(
            id = "malayalam",
            labelRes = R.string.lang_card_malayalam,
            drawableRes = R.drawable.lang_card_malayalam,
            latestFilter = LatestLanguageFilter.HIN_TAM_TEL,
        ),
        LanguageCardSpec(
            id = "kannada",
            labelRes = R.string.lang_card_kannada,
            drawableRes = R.drawable.lang_card_kannada,
            latestFilter = LatestLanguageFilter.HIN_TAM_TEL,
        ),
        LanguageCardSpec(
            id = "bengali",
            labelRes = R.string.lang_card_bengali,
            drawableRes = R.drawable.lang_card_bengali,
            latestFilter = LatestLanguageFilter.HIN_TAM_TEL,
        ),
        LanguageCardSpec(
            id = "english",
            labelRes = R.string.lang_card_english,
            drawableRes = R.drawable.lang_card_english,
            latestFilter = LatestLanguageFilter.JAP_ENG,
        ),
        LanguageCardSpec(
            id = "japanese",
            labelRes = R.string.lang_card_japanese,
            drawableRes = R.drawable.lang_card_japanese,
            latestFilter = LatestLanguageFilter.JAP_ENG,
        ),
        LanguageCardSpec(
            id = "korean",
            labelRes = R.string.lang_card_korean,
            drawableRes = R.drawable.lang_card_korean,
            latestFilter = LatestLanguageFilter.JAP_ENG,
        ),
    )

/**
 * Computes responsive card width per form factor & available viewport width while preserving
 * the exact 2:1 (1774 x 887 px) aspect ratio of every language card.
 */
fun calculateResponsiveLanguageCardWidth(
    formFactor: DeviceFormFactor,
    availableWidth: Dp,
): Dp {
    val safeWidth = availableWidth.value.coerceAtLeast(240f)
    val computed =
        when (formFactor) {
            DeviceFormFactor.MOBILE -> (safeWidth * 0.46f).coerceIn(160f, 206f)
            DeviceFormFactor.TABLET -> (safeWidth * 0.28f).coerceIn(184f, 232f)
            DeviceFormFactor.BIG_TABLET -> (safeWidth * 0.22f).coerceIn(196f, 248f)
            DeviceFormFactor.DESKTOP -> (safeWidth * 0.20f).coerceIn(208f, 264f)
            DeviceFormFactor.TV -> (safeWidth * 0.21f).coerceIn(216f, 272f)
        }
    return computed.dp
}

@Composable
fun LanguageCardsRail(
    formFactor: DeviceFormFactor,
    onCardClick: (LanguageCardSpec) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    horizontalPadding: Dp = 16.dp,
    itemSpacing: Dp = 6.dp,
    sectionSpacing: Dp = 12.dp,
    showScrollControls: Boolean = false,
    enableHoverLift: Boolean = false,
    isTvMode: Boolean = formFactor == DeviceFormFactor.TV,
    isReducedMotion: Boolean = rememberIsReducedMotion(),
    testTagPrefix: String = "home_languages",
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cardWidth =
            remember(formFactor, maxWidth) {
                calculateResponsiveLanguageCardWidth(
                    formFactor = formFactor,
                    availableWidth = this.maxWidth,
                )
            }
        val stepDistance = remember(cardWidth, itemSpacing) { cardWidth + itemSpacing }

        Column(verticalArrangement = Arrangement.spacedBy(sectionSpacing)) {
            if (isTvMode) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.section_languages_sub_count, ToonPlayLanguageCards.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                        )
                        Text(
                            text = stringResource(R.string.section_languages_title),
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
                            color = TextPrimary,
                        )
                    }
                }
            } else {
                SectionHeaderRow(
                    title = stringResource(R.string.section_languages_title),
                    kicker = stringResource(R.string.section_languages_sub_count, ToonPlayLanguageCards.size),
                    trailingLeadingControls =
                        if (showScrollControls) {
                            {
                                HorizontalRailScrollControls(
                                    listState = listState,
                                    stepDistance = stepDistance,
                                    isReducedMotion = isReducedMotion,
                                    testTagPrefix = testTagPrefix,
                                )
                            }
                        } else {
                            null
                        },
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                )
            }

            LazyRow(
                state = listState,
                contentPadding =
                    PaddingValues(
                        horizontal = horizontalPadding,
                        vertical = if (isTvMode || enableHoverLift) 4.dp else 0.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                modifier =
                    Modifier.fillMaxWidth()
                        .smoothMouseWheelScroll(
                            scrollableState = listState,
                            orientation = Orientation.Horizontal,
                            isReducedMotion = isReducedMotion,
                        )
                        .testTag("${testTagPrefix}_rail"),
            ) {
                items(
                    items = ToonPlayLanguageCards,
                    key = { it.id },
                    contentType = { "language_banner_card" },
                ) { card ->
                    LanguageBannerCardItem(
                        card = card,
                        cardWidth = cardWidth,
                        isTvMode = isTvMode,
                        enableHoverLift = enableHoverLift,
                        isReducedMotion = isReducedMotion,
                        onClick = { onCardClick(card) },
                    )
                }
            }
        }
    }
}

private val LanguageCardFocusShape = RoundedCornerShape(18.dp)

@Composable
private fun LanguageBannerCardItem(
    card: LanguageCardSpec,
    cardWidth: Dp,
    isTvMode: Boolean,
    enableHoverLift: Boolean,
    isReducedMotion: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val label = stringResource(card.labelRes)
    val imageRequest =
        remember(card.drawableRes) {
            ImageRequest.Builder(context)
                .data(card.drawableRes)
                .crossfade(false)
                .size(960, 480)
                .precision(Precision.INEXACT)
                .build()
        }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isInteractionFocused by interactionSource.collectIsFocusedAsState()
    var isTvFocused by remember { mutableStateOf(false) }
    val isFocusedOrHovered =
        isTvFocused || isInteractionFocused || (enableHoverLift && isHovered)

    val scale by
    animateFloatAsState(
        targetValue =
            when {
                isReducedMotion -> 1f
                isTvMode && isFocusedOrHovered -> 1.06f
                enableHoverLift && isFocusedOrHovered -> 1.035f
                else -> 1f
            },
        animationSpec =
            if (isReducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
        label = "language_card_scale",
    )

    val focusHaloColor by
    animateColorAsState(
        targetValue =
            when {
                isTvMode && (isTvFocused || isInteractionFocused) -> ToonVioletPrimary.copy(alpha = 0.28f)
                enableHoverLift && isFocusedOrHovered -> ToonVioletPrimary.copy(alpha = 0.16f)
                else -> Color.Transparent
            },
        animationSpec = if (isReducedMotion) snap() else spring(),
        label = "language_card_halo",
    )

    val baseModifier =
        Modifier.width(cardWidth)
            .aspectRatio(LANGUAGE_CARD_ASPECT_RATIO)
            .minimumInteractiveComponentSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isTvMode) {
                    Modifier.onFocusChanged { isTvFocused = it.isFocused }.focusable()
                } else if (enableHoverLift) {
                    Modifier.hoverable(interactionSource)
                } else {
                    Modifier
                }
            )
            .clip(LanguageCardFocusShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .testTag("language_card_${card.id}")

    Box(
        modifier =
            baseModifier.then(
                if (focusHaloColor != Color.Transparent) {
                    Modifier.background(
                        brush =
                            Brush.radialGradient(
                                colors = listOf(focusHaloColor, ObsidianBg.copy(alpha = 0f)),
                            ),
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )

        if (isTvMode && (isTvFocused || isInteractionFocused)) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(2.dp)
                        .border(
                            border = BorderStroke(2.dp, TextPrimary.copy(alpha = 0.9f)),
                            shape = LanguageCardFocusShape,
                        ),
            )
        }
    }
}
