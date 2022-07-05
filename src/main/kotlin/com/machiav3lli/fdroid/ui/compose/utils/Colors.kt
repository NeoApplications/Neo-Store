package com.machiav3lli.fdroid.ui.compose.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

@Stable
@Composable
fun Color.compositeOverBackground(
    alpha: Float,
    background: Color = MaterialTheme.colorScheme.background
) = this.copy(alpha).compositeOver(background)