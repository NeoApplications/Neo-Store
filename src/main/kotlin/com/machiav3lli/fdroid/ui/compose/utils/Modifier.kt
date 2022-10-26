package com.machiav3lli.fdroid.ui.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: @Composable Modifier.() -> Modifier
): Modifier =
    if (condition) factory() else this
