package com.looker.droidify.ui.compose.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.looker.droidify.R
import com.looker.droidify.ui.compose.theme.LocalShapes

@Composable
fun NetworkImage(
    modifier: Modifier = Modifier,
    data: String?,
    contentScale: ContentScale = ContentScale.Crop,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(LocalShapes.current.medium)
) {
    SubcomposeAsyncImage(
        modifier = modifier.clip(shape),
        model = data,
        contentDescription = null,
        contentScale = contentScale,
        loading = {
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor)
            )
        },
        error = {
            SubcomposeAsyncImageContent(painter = painterResource(id = R.drawable.ic_placeholder))
        }
    )
}

@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    data: String?,
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange * scale
    }
    NetworkImage(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = state),
        data = data,
        shape = RectangleShape
    )
}