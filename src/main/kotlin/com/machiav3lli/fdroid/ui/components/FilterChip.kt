package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectChip(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean = false,
    onClick: () -> Unit = {}
) {
    FilterChip(
        modifier = modifier,
        label = { Text(text = text) },
        selected = checked,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipsSwitch(
    firstTextId: Int,
    firstIcon: ImageVector,
    secondTextId: Int,
    secondIcon: ImageVector,
    firstSelected: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.medium
            )
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
    ) {
        val (firstSelected, selectFirst) = remember { mutableStateOf(firstSelected) }
        FilterChip(
            modifier = Modifier.weight(1f),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = Color.Transparent,
                borderWidth = 0.dp
            ),
            selected = firstSelected,
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            },
            leadingIcon = {
                Icon(
                    imageVector = firstIcon,
                    contentDescription = stringResource(id = firstTextId)
                )
            },
            label = {
                Row(
                    Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = firstTextId),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        )
        FilterChip(
            modifier = Modifier.weight(1f),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = Color.Transparent,
                borderWidth = 0.dp
            ),
            selected = !firstSelected,
            onClick = {
                onCheckedChange(false)
                selectFirst(false)
            },
            label = {
                Row(
                    Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = secondTextId),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            trailingIcon = {
                Icon(
                    imageVector = secondIcon,
                    contentDescription = stringResource(id = secondTextId)
                )
            }
        )
    }
}
