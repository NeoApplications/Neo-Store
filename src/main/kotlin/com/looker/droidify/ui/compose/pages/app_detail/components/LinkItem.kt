package com.looker.droidify.ui.compose.pages.app_detail.components

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.entity.LinkType
import com.looker.droidify.ui.compose.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkItem(
    modifier: Modifier = Modifier,
    linkType: LinkType,
    onClick: (Uri?) -> Unit = {},
    onLongClick: (Uri?) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(linkType.link) },
                onLongClick = { onLongClick(linkType.link) })
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = linkType.iconResId),
            contentDescription = linkType.title
        )

        Column(
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = linkType.title,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = linkType.link.toString(),
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun LinkItemPreview() {
    AppTheme(blackTheme = false) {
        LinkItem(
            linkType = LinkType(R.drawable.ic_email, "R.string.author_email")
        )
    }
}