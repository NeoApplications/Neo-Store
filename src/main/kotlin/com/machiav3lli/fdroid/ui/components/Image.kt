package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.machiav3lli.fdroid.R

@Composable
fun NetworkImage(
    modifier: Modifier = Modifier,
    data: String?,
    contentScale: ContentScale = ContentScale.Crop,
    isScreenshot: Boolean = false,
    shape: Shape = MaterialTheme.shapes.medium
) {
    AsyncImage(
        modifier = modifier.clip(shape),
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = contentScale,
        error = painterResource(
            id = if (isScreenshot) R.drawable.ic_screenshot_placeholder
            else R.drawable.ic_placeholder
        ),
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
        scale = (scale * zoomChange).coerceAtLeast(1f)
        offset = if (scale == 1f) Offset.Zero
        else offset + offsetChange * scale
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
        contentScale = ContentScale.Fit,
        shape = RectangleShape
    )
}