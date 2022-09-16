package com.machiav3lli.fdroid.ui.compose.components.prefs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    heading: String? = null,
    content: @Composable () -> Unit
) {
    PreferenceGroupHeading(heading)
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.primary
    ) {
        Surface(color = Color.Transparent) {
            Column(modifier = modifier) {
                content()
            }
        }
    }
}

@Composable
fun PreferenceGroupHeading(
    heading: String? = null
) = if (heading != null) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
} else Spacer(modifier = Modifier.requiredHeight(8.dp))
