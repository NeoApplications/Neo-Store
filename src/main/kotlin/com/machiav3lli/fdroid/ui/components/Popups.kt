package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay

@Composable
fun Tooltip(
    text: String,
    openPopup: MutableState<Boolean>,
) {
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, 100),
    ) {
        LaunchedEffect(key1 = Unit) {
            delay(3000)
            openPopup.value = false
        }

        Box {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}