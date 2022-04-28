package com.looker.droidify.ui.compose.pages.app_detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Description(
    modifier: Modifier = Modifier,
    description: AnnotatedString
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var isExpanded by remember { mutableStateOf(false) }
        Surface(
            modifier = modifier.animateContentSize(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colors.primary.copy(0.3f)
                .compositeOver(MaterialTheme.colors.background)
        ) {
            val maxLines by animateIntAsState(
                targetValue = if (isExpanded) Int.MAX_VALUE else 12,
                animationSpec = tween(durationMillis = 1000)
            )
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(),
                text = description,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
        FilledTonalButton(onClick = { isExpanded = !isExpanded }) {
            Text(text = "Show More")
        }
    }
}