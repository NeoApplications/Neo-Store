package com.looker.droidify.ui.compose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    text: String,
    initSelected: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (selected, select) = remember { mutableStateOf(initSelected) }
        Text(text = text)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = selected,
            onCheckedChange = {
                select(it)
                onCheckedChanged(it)
            })
    }
}
