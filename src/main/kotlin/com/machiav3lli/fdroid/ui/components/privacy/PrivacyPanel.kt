package com.machiav3lli.fdroid.ui.components.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.ExpandableBlock
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SlidersHorizontal

@Composable
fun PrivacyCard(
    modifier: Modifier = Modifier,
    heading: String? = null,
    actionText: String = "",
    actionIcon: ImageVector = Phosphor.SlidersHorizontal,
    preExpanded: Boolean = false,
    onAction: () -> Unit = { },
    content: @Composable ColumnScope.() -> Unit,
) {
    ExpandableBlock(
        modifier = modifier.padding(horizontal = 8.dp),
        heading = heading,
        preExpanded = preExpanded
    ) {
        content()
        if (actionText.isNotEmpty()) Row(
            modifier = Modifier.padding(
                top = 8.dp,
                start = 8.dp,
                end = 8.dp,
            ),
        ) {
            ActionButton(
                modifier = Modifier.weight(1f),
                text = actionText,
                icon = actionIcon,
                positive = true,
                onClick = onAction
            )
        }
    }
}

@Composable
fun PrivacyItemHeader(
    heading: String? = null,
    icon: ImageVector? = null,
    withIcon: Boolean = true,
) {
    var spacerHeight = 0
    if (heading == null) spacerHeight += 8
    Spacer(modifier = Modifier.requiredHeight(spacerHeight.dp))
    if (heading != null) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = heading
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = heading,
                style = MaterialTheme.typography.titleSmall,
            )
            if (withIcon) Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Phosphor.CaretDown,
                contentDescription = heading
            )
        }
    }
}