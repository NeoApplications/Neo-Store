package com.looker.droidify.ui.compose.pages.app_detail.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.ui.compose.theme.AppTheme

@Composable
fun LinkItem(
    modifier: Modifier = Modifier,
    linkType: LinkType,
    onClick: (String) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(linkType.text) }
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = linkType.iconResId),
            contentDescription = stringResource(id = linkType.titleResId)
        )

        Column(
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = stringResource(id = linkType.titleResId),
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = linkType.text,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class LinkType(
    @DrawableRes val iconResId: Int, @StringRes val titleResId: Int, val text: String = ""
)

@Preview
@Composable
fun LinkItemPreview() {
    AppTheme(blackTheme = false) {
        LinkItem(
            linkType = LinkType(R.drawable.ic_email, R.string.author_email, "Looker")
        )
    }
}