package com.looker.droidify.ui.compose.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.looker.droidify.R
import com.looker.droidify.ui.compose.theme.LocalShapes

@OptIn(ExperimentalCoilApi::class)
@Composable
fun NetworkImage(
    modifier: Modifier = Modifier,
    data: Uri?,
    contentScale: ContentScale = ContentScale.Crop,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shape: CornerBasedShape = RoundedCornerShape(LocalShapes.current.medium)
) {
    Box(modifier) {
        val painter = rememberImagePainter(data = data) {
            placeholder(R.drawable.ic_application_default)
            error(R.drawable.ic_application_default)
        }

        Image(
            painter = painter,
            contentDescription = "This is Album Art",
            contentScale = contentScale,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
        )

        if (painter.state is ImagePainter.State.Loading) {
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor)
            )
        }
    }
}