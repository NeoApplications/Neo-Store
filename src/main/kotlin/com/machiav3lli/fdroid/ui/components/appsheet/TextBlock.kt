package com.machiav3lli.fdroid.ui.components.appsheet

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.utils.addIfElse
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun HtmlTextBlock(
    modifier: Modifier = Modifier,
    shortText: String,
    longText: String = "",
    onUriClick: (String) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var isExpanded by rememberSaveable { mutableStateOf(false) }
        MarkdownText(
            modifier = Modifier
                .padding(12.dp)
                .addIfElse(
                    isExpanded,
                    { fillMaxWidth() },
                    { align(Alignment.CenterHorizontally) }
                ),
            markdown = if (isExpanded) longText
            else shortText,
            linkColor = MaterialTheme.colorScheme.secondary,
            isTextSelectable = true,
            onLinkClicked = onUriClick
        )
        if (longText.isNotEmpty()) {
            FilledTonalButton(onClick = { isExpanded = !isExpanded }) {
                Text(text = stringResource(id = if (isExpanded) R.string.show_less else R.string.show_more))
            }
        }
    }
}
