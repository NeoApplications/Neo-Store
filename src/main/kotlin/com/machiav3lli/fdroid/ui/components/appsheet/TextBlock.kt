package com.machiav3lli.fdroid.ui.components.appsheet

import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.saveable.rememberSaveable
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
    shortText: String,
    longText: String = "",
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var isExpanded by rememberSaveable { mutableStateOf(false) }
        Surface(
            modifier = modifier.animateContentSize(),
            shape = MaterialTheme.shapes.large,
            color = Color.Transparent
        ) {
            SelectionContainer {
                HtmlText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .animateContentSize(),
                    text = if (isExpanded) longText
                    else shortText,
                    color = MaterialTheme.colorScheme.onBackground,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (longText.isNotEmpty()) {
            FilledTonalButton(onClick = { isExpanded = !isExpanded }) {
                Text(text = stringResource(id = if (isExpanded) R.string.show_less else R.string.show_more))
            }
        }
    }
}
