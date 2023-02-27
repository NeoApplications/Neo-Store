package com.machiav3lli.fdroid.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableBlock(
    modifier: Modifier = Modifier,
    heading: String? = null,
    positive: Boolean = true,
    preExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(preExpanded) }
    val tonalElevation by animateDpAsState(
        targetValue = if (expanded) 8.dp
        else 0.dp
    )

    Surface(
        modifier = Modifier.animateContentSize(),
        shape = MaterialTheme.shapes.large,
        onClick = { expanded = !expanded },
        tonalElevation = tonalElevation
    ) {
        Column(modifier = modifier) {
            ExpandableBlockHeader(heading, positive)
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(bottom = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ExpandableBlockHeader(
    heading: String? = null,
    positive: Boolean,
) {
    var spacerHeight = 0
    if (heading == null) spacerHeight += 8
    Spacer(modifier = Modifier.requiredHeight(spacerHeight.dp))
    if (heading != null) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleSmall,
                )
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Phosphor.CaretDown,
                    contentDescription = heading
                )
            }
        }
    }
}