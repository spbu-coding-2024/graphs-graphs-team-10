package view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import viewmodel.VertexViewModel

@Composable
fun vertexView(
    viewModel: VertexViewModel,
    modifier: Modifier = Modifier,
) {
    var isHovered by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        if (isHovered) Color.Magenta else Color.Black,
        label = "border_color_animation",
    )

    val pulseScale by animateFloatAsState(
        targetValue = if (isTapped) 1.2f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.5f,
                stiffness = 300f,
            ),
        label = "tap_pulse",
    )

    LaunchedEffect(isTapped) {
        if (isTapped) {
            delay(150)
            isTapped = false
        }
    }

    Box(
        modifier =
            modifier
                .offset(viewModel.x, viewModel.y),
    ) {
        Box(
            modifier =
                Modifier
                    .size(viewModel.radius * 2)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        color = viewModel.color,
                        shape = CircleShape,
                    )
                    .border(
                        width = viewModel.radius / 5,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .pointerInput(viewModel) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Move -> {
                                        val isInside =
                                            event.changes.fastAll { change ->
                                                val position = change.position
                                                position.x >= 0f &&
                                                        position.y >= 0f &&
                                                        position.x <= size.width &&
                                                        position.y <= size.height
                                            }
                                        if (isInside != isHovered) {
                                            isHovered = isInside
                                        }
                                    }
                                    PointerEventType.Exit -> {
                                        isHovered = false
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                    .pointerInput(viewModel) {
                        detectTapGestures {
                            viewModel.onClick()
                            isTapped = true
                        }
                    }
                    .pointerInput(viewModel) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            viewModel.onDrag(dragAmount)
                            isHovered = false
                        }
                    },
            contentAlignment = Alignment.Center,
        ) {
        }

        if (viewModel.valueVisible) {
            Text(
                text = viewModel.value.toString(),
                color = Color.Black,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .zIndex(1f),
            )
        }
    }
}
