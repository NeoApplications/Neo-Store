package com.machiav3lli.fdroid.ui.compose.pages.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceSlider(
    modifier: Modifier = Modifier,
    title: String,
    steps: Int = 20,
    valueRange: ClosedFloatingPointRange<Float> = 10f..200f,
    value: () -> Float,
    onChange: (Float) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = value(),
            valueRange = valueRange,
            steps = steps,
            onValueChange = onChange
        )
    }
}