package com.looker.droidify.ui.compose.pages.app_detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import de.charlex.compose.HtmlText

/*
* Annotate String in this way https://stackoverflow.com/questions/65567412/jetpack-compose-text-hyperlink-some-section-of-the-text/69549929#69549929
* TODO REMOVE usage of HtmlText
* */
@Composable
fun HtmlTextBlock(
    modifier: Modifier = Modifier,
    description: String
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        var isExpanded by remember { mutableStateOf(false) }
        Surface(
            modifier = modifier.animateContentSize(),
            shape = MaterialTheme.shapes.large,
            color = Color.Transparent
        ) {
            val maxLines by animateIntAsState(
                targetValue = if (isExpanded) Int.MAX_VALUE else 12,
                animationSpec = tween(durationMillis = 200)
            )
            HtmlText(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(),
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (description.length >= 500) {
            FilledTonalButton(onClick = { isExpanded = !isExpanded }) {
                Text(text = stringResource(id = if (isExpanded) R.string.show_less else R.string.show_more))
            }
        }
    }
}