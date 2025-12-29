package com.machiav3lli.fdroid.ui.components.privacy

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.ExpandablePrivacyBlock
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
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
    ExpandablePrivacyBlock(
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