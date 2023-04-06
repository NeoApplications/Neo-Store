package com.machiav3lli.fdroid.ui.components.appsheet

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.machiav3lli.fdroid.R
import de.charlex.compose.HtmlText

@Composable
fun HtmlTextBlock(
    modifier: Modifier = Modifier,
    description: String,
    isExpandable: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var isExpanded by remember { mutableStateOf(false) }
        Surface(
            modifier = modifier.animateContentSize(),
            shape = MaterialTheme.shapes.large,
            color = Color.Transparent
        ) {
            val maxLines by animateIntAsState(
                targetValue = if (isExpanded || !isExpandable) Int.MAX_VALUE else 12,
                animationSpec = tween(durationMillis = 200)
            )
            SelectionContainer {
                HtmlText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .animateContentSize(),
                    text = description,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (description.length >= 290 && isExpandable) {
            FilledTonalButton(onClick = { isExpanded = !isExpanded }) {
                Text(text = stringResource(id = if (isExpanded) R.string.show_less else R.string.show_more))
            }
        }
    }
}
