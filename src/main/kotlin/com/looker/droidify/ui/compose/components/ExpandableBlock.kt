package com.looker.droidify.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Surface(
        modifier = Modifier.animateContentSize(),
        shape = MaterialTheme.shapes.large,
        onClick = { expanded = !expanded },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = modifier) {
            ExpandableBlockHeader(heading, positive)
            AnimatedVisibility(
                visible = expanded
            ) {
                Column {
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = heading,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = heading
                )
            }
        }
    }
}
