package com.machiav3lli.fdroid.ui.components.appsheet

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.utils.addIfElse

@Composable
fun HtmlTextBlock(
    modifier: Modifier = Modifier,
    shortText: String,
    longText: String = "",
    onUriClick: (String) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var isExpanded by rememberSaveable { mutableStateOf(false) }
        val text = remember(isExpanded, shortText, longText) {
            htmlToAnnotatedString(
                if (isExpanded) longText else shortText,
                linkInteractionListener = { link ->
                    if (link is LinkAnnotation.Url) onUriClick(link.url)
                }
            )
        }
        Text(
            text,
            modifier = Modifier
                .animateContentSize()
                .padding(12.dp)
                .addIfElse(
                    isExpanded,
                    { fillMaxWidth() },
                    { align(Alignment.CenterHorizontally) }
                )
        )
        if (longText.isNotEmpty()) {
            FilledTonalButton(onClick = { isExpanded = !isExpanded }) {
                Text(text = stringResource(id = if (isExpanded) R.string.show_less else R.string.show_more))
            }
        }
    }
}
