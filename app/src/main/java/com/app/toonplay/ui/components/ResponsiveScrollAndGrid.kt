package com.app.toonplay.ui.components

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.toonplay.R
import com.app.toonplay.ui.theme.BorderSubtle
import com.app.toonplay.ui.theme.ObsidianCard
import com.app.toonplay.ui.theme.ObsidianSurface
import com.app.toonplay.ui.theme.TextMuted
import com.app.toonplay.ui.theme.TextOffWhite
import kotlin.math.abs
import kotlin.math.exp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Computes the exact number of complete cards of [cardWidth] (with [spacing] gaps) that fit
 * inside [availableWidth] without clipping or showing fractional cards.
 */

fun calculateCompleteGridColumns(
    availableWidth: Dp,
    cardWidth: Dp,
    spacing: Dp,
    minColumns: Int = 1,
): Int {
    val safeAvailable = availableWidth.value.coerceAtLeast(0f)
    val safeCard = cardWidth.value.coerceAtLeast(1f)
    val safeSpacing = spacing.value.coerceAtLeast(0f)

    // Calculate how many cards fit, including the spacing between cards.
    val visibleCardCount =
        (safeAvailable + safeSpacing) / (safeCard + safeSpacing)

    val wholeColumns = visibleCardCount.toInt()
    val fractionalPart = visibleCardCount - wholeColumns

    // Round up only when the fractional part is 0.6 or greater.
    val columns =
        if (fractionalPart >= 0.6f) wholeColumns + 1
        else wholeColumns

    return columns.coerceAtLeast(minColumns)
}

val LocalIsReducedMotion = staticCompositionLocalOf { false }

val LocalLiveDotAlpha = staticCompositionLocalOf<State<Float>> { mutableFloatStateOf(1f) }

val LocalHeroAutoScrollEnabled = staticCompositionLocalOf<State<Boolean>> { mutableStateOf(true) }

/**
 * Queries Android's system reduced-motion setting (`ANIMATOR_DURATION_SCALE == 0f`) once at root.
 */
@Composable
fun rememberSystemReducedMotion(): Boolean {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}

/**
 * Returns the cached system reduced-motion preference without performing Binder IPC on scroll.
 */
@Composable
fun rememberIsReducedMotion(): Boolean = LocalIsReducedMotion.current

/**
 * Provides smooth spring-based focus scrolling for D-pad, remote, and keyboard navigation while
 * maintaining [edgeMargin] clearance so focus rings and scaled cards do not clip at edges.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProvideSmoothBringIntoView(
    edgeMargin: Dp = 24.dp,
    isReducedMotion: Boolean = rememberIsReducedMotion(),
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val marginPx = remember(density, edgeMargin) { with(density) { edgeMargin.toPx() } }
    val bringIntoViewSpec =
        remember(marginPx, isReducedMotion) {
            object : BringIntoViewSpec {
                override val scrollAnimationSpec: AnimationSpec<Float> =
                    if (isReducedMotion) {
                        snap()
                    } else {
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        )
                    }

                override fun calculateScrollDistance(
                    offset: Float,
                    size: Float,
                    containerSize: Float,
                ): Float {
                    if (containerSize <= 0f || size <= 0f) return 0f
                    val margin = marginPx.coerceAtMost(((containerSize - size) / 2f).coerceAtLeast(0f))
                    val leadingEdge = offset
                    val trailingEdge = offset + size
                    val visibleStart = margin
                    val visibleEnd = (containerSize - margin).coerceAtLeast(visibleStart + size)
                    return when {
                        leadingEdge < visibleStart && trailingEdge > visibleEnd -> 0f
                        leadingEdge < visibleStart -> leadingEdge - visibleStart
                        trailingEdge > visibleEnd -> trailingEdge - visibleEnd
                        else -> 0f
                    }
                }
            }
        }

    CompositionLocalProvider(LocalBringIntoViewSpec provides bringIntoViewSpec, content = content)
}

@Stable
private class SmoothWheelScrollAccumulator(
    private val scrollableState: ScrollableState,
    private val scope: CoroutineScope,
    private val isReducedMotion: Boolean,
) {
    private var remainingDelta = 0f
    private var activeJob: Job? = null

    fun addDelta(deltaPx: Float) {
        if (deltaPx == 0f) return
        if (isReducedMotion) {
            scope.launch { scrollableState.scrollBy(deltaPx) }
            return
        }
        remainingDelta =
            if (remainingDelta != 0f && (remainingDelta > 0f) != (deltaPx > 0f)) {
                deltaPx
            } else {
                remainingDelta + deltaPx
            }

        if (activeJob?.isActive != true) {
            activeJob =
                scope.launch {
                    scrollableState.scroll(MutatePriority.UserInput) {
                        var lastNanos = 0L
                        while (abs(remainingDelta) > 0.5f) {
                            withFrameNanos { frameNanos ->
                                val dtSeconds =
                                    if (lastNanos == 0L) {
                                        1f / 60f
                                    } else {
                                        ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0.004f, 0.05f)
                                    }
                                lastNanos = frameNanos
                                val fraction = (1f - exp(-16f * dtSeconds)).coerceIn(0.14f, 1f)
                                val step = remainingDelta * fraction
                                val consumed = scrollBy(step)
                                remainingDelta -= step
                                if (abs(consumed) < 0.1f && abs(step) >= 1f) {
                                    remainingDelta = 0f
                                }
                            }
                        }
                        if (abs(remainingDelta) > 0.01f && abs(remainingDelta) <= 0.5f) {
                            scrollBy(remainingDelta)
                            remainingDelta = 0f
                        }
                    }
                }
        }
    }
}

/**
 * Adds smooth, non-conflicting mouse-wheel & trackpad scrolling to any [ScrollableState].
 *
 * Horizontal containers only consume horizontal trackpad gestures or `Shift + wheel`, leaving
 * standard vertical wheel events unconsumed so the outer vertical list scrolls smoothly.
 */
@Composable
fun Modifier.smoothMouseWheelScroll(
    scrollableState: ScrollableState,
    orientation: Orientation,
    isReducedMotion: Boolean = rememberIsReducedMotion(),
    stepSize: Dp = 84.dp,
): Modifier {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val stepPx = remember(density, stepSize) { with(density) { stepSize.toPx() } }
    val accumulator =
        remember(scrollableState, scope, isReducedMotion) {
            SmoothWheelScrollAccumulator(
                scrollableState = scrollableState,
                scope = scope,
                isReducedMotion = isReducedMotion,
            )
        }

    return this.pointerInput(scrollableState, orientation, stepPx, isReducedMotion) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type == PointerEventType.Scroll) {
                    val delta = event.changes.fold(Offset.Zero) { acc, change -> acc + change.scrollDelta }
                    val shiftPressed = event.keyboardModifiers.isShiftPressed
                    val axisDelta =
                        when (orientation) {
                            Orientation.Vertical -> {
                                if (!shiftPressed && abs(delta.y) >= abs(delta.x) && delta.y != 0f) delta.y else 0f
                            }
                            Orientation.Horizontal -> {
                                when {
                                    shiftPressed && delta.y != 0f -> delta.y
                                    abs(delta.x) > abs(delta.y) && delta.x != 0f -> delta.x
                                    else -> 0f
                                }
                            }
                        }
                    if (axisDelta != 0f) {
                        val canScroll =
                            if (axisDelta > 0f) scrollableState.canScrollForward
                            else scrollableState.canScrollBackward
                        if (canScroll) {
                            event.changes.forEach { it.consume() }
                            accumulator.addDelta(axisDelta * stepPx)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Allows horizontal trackpad swipes or `Shift + mouse wheel` to advance [PagerState] smoothly
 * without intercepting normal vertical mouse-wheel scrolling of the parent screen.
 */
@Composable
fun Modifier.smoothHorizontalPagerWheelScroll(
    pagerState: PagerState,
    isReducedMotion: Boolean = rememberIsReducedMotion(),
): Modifier {
    val scope = rememberCoroutineScope()
    return this.pointerInput(pagerState, isReducedMotion) {
        var accumulated = 0f
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type == PointerEventType.Scroll && pagerState.pageCount > 1) {
                    val delta = event.changes.fold(Offset.Zero) { acc, change -> acc + change.scrollDelta }
                    val shiftPressed = event.keyboardModifiers.isShiftPressed
                    val horizontalDelta =
                        when {
                            shiftPressed && delta.y != 0f -> delta.y
                            abs(delta.x) > abs(delta.y) && delta.x != 0f -> delta.x
                            else -> 0f
                        }
                    if (horizontalDelta != 0f) {
                        event.changes.forEach { it.consume() }
                        accumulated += horizontalDelta
                        if (abs(accumulated) >= 0.45f && !pagerState.isScrollInProgress) {
                            val step = if (accumulated > 0f) 1 else -1
                            accumulated = 0f
                            val targetPage =
                                (pagerState.currentPage + step).coerceIn(0, pagerState.pageCount - 1)
                            scope.launch {
                                if (isReducedMotion) {
                                    pagerState.scrollToPage(targetPage)
                                } else {
                                    pagerState.animateScrollToPage(
                                        page = targetPage,
                                        animationSpec =
                                            spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow,
                                            ),
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

/**
 * Compact left/right chevron controls for horizontal content rails on Desktop & non-touch devices.
 */
@Composable
fun HorizontalRailScrollControls(
    listState: LazyListState,
    stepDistance: Dp,
    isReducedMotion: Boolean = rememberIsReducedMotion(),
    modifier: Modifier = Modifier,
    testTagPrefix: String = "rail_scroll",
) {
    if (!listState.canScrollBackward && !listState.canScrollForward) return
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val stepPx = remember(density, stepDistance) { with(density) { stepDistance.toPx() } }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RailChevronButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.cd_scroll_left),
            enabled = listState.canScrollBackward,
            testTag = "${testTagPrefix}_left",
            onClick = {
                scope.launch {
                    if (isReducedMotion) {
                        listState.scrollBy(-stepPx)
                    } else {
                        listState.animateScrollBy(
                            value = -stepPx,
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                        )
                    }
                }
            },
        )
        RailChevronButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(R.string.cd_scroll_right),
            enabled = listState.canScrollForward,
            testTag = "${testTagPrefix}_right",
            onClick = {
                scope.launch {
                    if (isReducedMotion) {
                        listState.scrollBy(stepPx)
                    } else {
                        listState.animateScrollBy(
                            value = stepPx,
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun RailChevronButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) ObsidianCard else ObsidianSurface.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.size(32.dp).testTag(testTag),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) TextOffWhite else TextMuted.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Renders an inline grid whose column count matches the exact number of complete [cardWidth]
 * cards that fit inside the available horizontal space (accounting for sidebars, inspector panes,
 * and [horizontalPadding]).
 */
@Composable
fun <T> ResponsiveSectionGrid(
    items: List<T>,
    cardWidth: Dp,
    itemSpacing: Dp,
    horizontalPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
    itemContent: @Composable (index: Int, item: T, cellModifier: Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth().padding(horizontal = horizontalPadding)) {
        val availableWidth = this.maxWidth
        val columns =
            remember(availableWidth, cardWidth, itemSpacing) {
                calculateCompleteGridColumns(
                    availableWidth = maxWidth,
                    cardWidth = cardWidth,
                    spacing = itemSpacing,
                    minColumns = 1,
                )
            }
        val rows = remember(items, columns) { items.chunked(columns) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            rows.forEachIndexed { rowIdx, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                ) {
                    rowItems.forEachIndexed { colIdx, item ->
                        val flatIndex = rowIdx * columns + colIdx
                        if (columns == 1) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                itemContent(
                                    flatIndex,
                                    item,
                                    Modifier.widthIn(max = cardWidth * 1.35f).fillMaxWidth(),
                                )
                            }
                        } else {
                            itemContent(flatIndex, item, Modifier.weight(1f))
                        }
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Emits inline grid rows lazily into an outer [LazyColumn] so each row is composed and recycled
 * on demand as the user scrolls vertically instead of composing all rows in a single frame.
 */
fun <T> LazyListScope.inlinePosterGridRows(
    sectionKey: String,
    items: List<T>,
    columns: Int,
    cardWidth: Dp,
    itemSpacing: Dp,
    horizontalPadding: Dp,
    firstRowTopPadding: Dp,
    rowKeyPrefix: (T) -> String,
    itemContent: @Composable (index: Int, item: T, cellModifier: Modifier) -> Unit,
) {
    val safeColumns = columns.coerceAtLeast(1)
    val rowCount = (items.size + safeColumns - 1) / safeColumns
    items(
        count = rowCount,
        key = { rowIdx ->
            val start = rowIdx * safeColumns
            val end = minOf(start + safeColumns, items.size)
            val rowIds =
                buildString {
                    for (i in start until end) {
                        if (i > start) append('_')
                        append(rowKeyPrefix(items[i]))
                    }
                }
            "${sectionKey}_grid_r${safeColumns}_${rowIdx}_$rowIds"
        },
        contentType = { "inline_grid_row" },
    ) { rowIdx ->
        val start = rowIdx * safeColumns
        val end = minOf(start + safeColumns, items.size)
        val topPad = if (rowIdx == 0) firstRowTopPadding else itemSpacing
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = topPad),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            for (idx in start until end) {
                val item = items[idx]
                if (safeColumns == 1) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        itemContent(
                            idx,
                            item,
                            Modifier.widthIn(max = cardWidth * 1.35f).fillMaxWidth(),
                        )
                    }
                } else {
                    itemContent(idx, item, Modifier.weight(1f))
                }
            }
            repeat(safeColumns - (end - start)) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}