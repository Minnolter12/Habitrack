package com.minnolter.habitrack.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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

data class ReorderEntry(val habitId: Long, val name: String, val colorHex: String)

private val ROW_HEIGHT = 72.dp

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

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
    ) {
        itemsIndexed(items = orderedItems, key = { _, entry -> entry.habitId }) { index, entry ->
            val isDragging = entry.habitId == draggingId
            val cardShape = RoundedCornerShape(18.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ROW_HEIGHT)
                    .padding(vertical = 4.dp)
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffsetPx else 0f
                        shadowElevation = if (isDragging) 12f else 0f
                    }
                    .clip(cardShape)
                    .background(Color(0xFF1B1822))
                    .border(
                        BorderStroke(
                            width = 1.dp,
                            color = if (isDragging) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.15f)
                        ),
                        shape = cardShape
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { latestMoveItem.value(index, index - 1) }, enabled = index > 0) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Move ${entry.name} up",
                        tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.25f)
                    )
                }
                IconButton(
                    onClick = { latestMoveItem.value(index, index + 1) },
                    enabled = index < orderedItems.lastIndex
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Move ${entry.name} down",
                        tint = if (index < orderedItems.lastIndex) Color.White else Color.White.copy(alpha = 0.25f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(entry.colorHex)))
                )

                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x2200E5FF))
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
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragHandle,
                        contentDescription = "Drag to reorder ${entry.name}",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }
    }
}
