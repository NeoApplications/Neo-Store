package com.machiav3lli.fdroid.ui.components.prefs

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
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.LinkRef

@Composable
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    heading: String? = null,
    content: @Composable () -> Unit
) {
    PreferenceGroupHeading(heading = heading, modifier = titleModifier)
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
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    heading: String? = null,
    keys: List<Preferences.Key<*>>,
    onPrefDialog: (Preferences.Key<*>) -> Unit,
) {
    val size = keys.size

    PreferenceGroup(
        modifier = modifier,
        titleModifier = titleModifier,
        heading = heading
    ) {
        keys.forEachIndexed { index, item ->
            PrefsBuilder(
                item,
                onPrefDialog,
                index,
                size
            )
            if (index < size - 1) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    heading: String? = null,
    links: List<LinkRef>
) {
    val size = links.size

    PreferenceGroup(
        modifier = modifier,
        titleModifier = titleModifier,
        heading = heading
    ) {
        links.forEachIndexed { index, item ->
            LinkPreference(
                link = item,
                index = index,
                groupSize = size,
            )
            if (index < size - 1) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun PreferenceGroupHeading(
    modifier: Modifier = Modifier,
    heading: String? = null
) = if (heading != null) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
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
} else Spacer(modifier = modifier.requiredHeight(8.dp))
