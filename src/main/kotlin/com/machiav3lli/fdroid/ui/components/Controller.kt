package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    text: String,
    initSelected: () -> Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (selected, select) = remember { mutableStateOf(initSelected()) }
        Text(
            modifier = Modifier.weight(1f, true),
            text = text,
        )
        Switch(
            checked = selected,
            colors = SwitchDefaults.colors(uncheckedBorderColor = Color.Transparent),
            onCheckedChange = {
                select(it)
                onCheckedChanged(it)
            }
        )
    }
}
