package domilopment.apkextractor.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A fading edge drawn by a linear gradient from start to end
 * @param start the begin of the fading edge from it is drawn drawn
 * @param end the end of the fading edge to it is drawn
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingEdge(start: Offset, end: Offset, visible: Boolean, size: Dp) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .composed {
        require(size > 0.dp) { "Invalid fade width: Width must be greater than 0" }

        val fade by animateDpAsState(
            targetValue = if (visible) 0.dp else size,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "FadingEdge"
        )

        val width = remember(start, end) {
            abs(end.x - start.x)
        }

        val height = remember(start, end) {
            abs(end.y - start.y)
        }

        drawWithContent {
            drawContent()

            val contentWidth = when {
                width > this.size.width -> this.size.width
                width.isNaN() -> 0f
                else -> width
            }
            val contentHeight = when {
                height > this.size.height -> this.size.height
                height.isNaN() -> 0f
                else -> height
            }

            val fraction = when {
                contentWidth > 0 && contentHeight <= 0f -> fade.toPx() / contentWidth
                contentWidth <= 0f && contentHeight > 0 -> fade.toPx() / contentHeight
                contentWidth <= 0f && contentHeight <= 0f -> 0f
                contentWidth > 0f && contentHeight > 0f -> fade.toPx() / sqrt(contentWidth * contentWidth + contentHeight * contentHeight)
                else -> error("FadingEdge float fraction is not in range")
            }

            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.Transparent, fraction to Color.Black, start = start, end = end
                ), blendMode = BlendMode.DstIn
            )
        }
    }

/**
 * Draws a fading edge at the top of the content
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingTop(visible: Boolean, size: Dp = 32.dp) = fadingEdge(
    start = Offset.Zero, end = Offset(0f, Float.POSITIVE_INFINITY), visible = visible, size = size
)

/**
 * Draws a fading edge at the bottom of the content
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingBottom(visible: Boolean, size: Dp = 32.dp) = fadingEdge(
    start = Offset(0f, Float.POSITIVE_INFINITY), end = Offset.Zero, visible = visible, size = size
)

/**
 * Draws a fading edge at the start of the content
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingStart(visible: Boolean, size: Dp = 32.dp) = fadingEdge(
    start = Offset.Zero, end = Offset(Float.POSITIVE_INFINITY, 0f), visible = visible, size = size
)

/**
 * Draws a fading edge at the end of the content
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingEnd(visible: Boolean, size: Dp = 32.dp) = fadingEdge(
    start = Offset(Float.POSITIVE_INFINITY, 0f), end = Offset.Zero, visible = visible, size = size
)