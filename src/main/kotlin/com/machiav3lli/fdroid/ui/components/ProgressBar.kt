package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

data object DelayedLinearProgressBarDefaults {
    const val SHORT_DELAY_MS: Long = 200L
}

@Composable
fun DelayedLinearProgressBar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Long = DelayedLinearProgressBarDefaults.SHORT_DELAY_MS,
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMillis)
            show = true
        } else {
            show = false
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = show,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}
