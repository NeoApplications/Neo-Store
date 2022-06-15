package com.looker.droidify.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.looker.droidify.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableBlock(
    modifier: Modifier = Modifier,
    heading: String? = null,
    positive: Boolean = true,
    preExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
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
                Column(Modifier.padding(bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ExpandableBlockHeader(
    heading: String? = null,
    positive: Boolean
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
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = heading
                )
            }
        }
    }
}