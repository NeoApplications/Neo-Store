package com.looker.droidify.ui.compose.pages.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceSwitch(
    modifier: Modifier = Modifier,
    switchTitle: String,
    switchDescription: String,
    checkedState: () -> Boolean,
    onCheck: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = switchTitle, style = MaterialTheme.typography.titleSmall)
            Text(text = switchDescription, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checkedState(), onCheckedChange = onCheck)
    }
}