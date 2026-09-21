package com.minnolter.habitrack.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/** A minimal, name-and-color view of a habit — enough to reorder by, without the cost of a full jelly card. */
data class ReorderEntry(val habitId: Long, val name: String, val colorHex: String)

private val ROW_HEIGHT = 64.dp

/**
 * The Section 24 reorder UI: rows are deliberately simplified (name + accent
 * dot, no jelly canvas) since continuously animating N jelly fills while the
 * user is mid-drag would be exactly the kind of unnecessary GPU/CPU cost
 * Section 35 warns against, for a view the user is in only briefly.
 *
 * Every row exposes two ways to move: a long-press-and-drag handle (the
 * spec's literal "drag cards up and down"), and up/down icon buttons for
 * anyone who can't perform a drag gesture — a screen reader user, someone
 * using switch access, or anyone who'd simply rather tap twice (Section 34:
 * the app must remain usable without relying on gesture-only interactions).
 * Both act on the same underlying order, so they're always in sync.
 *
 * The reordered list is held entirely in local Compose state; [onOrderChanged]
 * fires on every move so the caller (`HomeScreen`) always has the latest
 * order on hand for its Save/Cancel actions, but nothing is persisted here —
 * this composable never talks to a ViewModel or repository.
 */
@Composable
fun ReorderableHabitList(
    items: List<ReorderEntry>,
    onOrderChanged: (List<ReorderEntry>) -> Unit,
    modifier: Modifier = Modifier
) {
    var orderedItems by remember(items) { mutableStateOf(items) }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { ROW_HEIGHT.toPx() }

    fun moveItem(from: Int, to: Int) {
        if (from == to || to !in orderedItems.indices) return
        orderedItems = orderedItems.toMutableList().apply { add(to, removeAt(from)) }
        onOrderChanged(orderedItems)
    }

    val latestMoveItem = rememberUpdatedState(::moveItem)

    LazyColumn(modifier = modifier) {
        itemsIndexed(items = orderedItems, key = { _, entry -> entry.habitId }) { index, entry ->
            val isDragging = entry.habitId == draggingId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ROW_HEIGHT)
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffsetPx else 0f
                        shadowElevation = if (isDragging) 12f else 0f
                    }
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { latestMoveItem.value(index, index - 1) }, enabled = index > 0) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move ${entry.name} up")
                }
                IconButton(
                    onClick = { latestMoveItem.value(index, index + 1) },
                    enabled = index < orderedItems.lastIndex
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move ${entry.name} down")
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(entry.colorHex)))
                )

                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Drag to reorder ${entry.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(entry.habitId, orderedItems.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingId = entry.habitId
                                    dragOffsetPx = 0f
                                },
                                onDragEnd = {
                                    draggingId = null
                                    dragOffsetPx = 0f
                                },
                                onDragCancel = {
                                    draggingId = null
                                    dragOffsetPx = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetPx += dragAmount.y

                                    val currentIndex = orderedItems.indexOfFirst { it.habitId == entry.habitId }
                                    val steps = (dragOffsetPx / rowHeightPx).roundToInt()
                                    if (steps != 0) {
                                        val targetIndex = (currentIndex + steps)
                                            .coerceIn(0, orderedItems.lastIndex)
                                        if (targetIndex != currentIndex) {
                                            latestMoveItem.value(currentIndex, targetIndex)
                                            dragOffsetPx -= steps * rowHeightPx
                                        }
                                    }
                                }
                            )
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}
