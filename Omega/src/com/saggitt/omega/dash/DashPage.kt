package com.saggitt.omega.dash

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.ActionDashItem
import com.saggitt.omega.compose.components.ControlDashItem
import com.saggitt.omega.compose.components.MusicBar
import com.saggitt.omega.dash.actionprovider.AudioPlayer
import kotlin.math.roundToInt

// TODO add better support for horizontal
@Composable
fun DashPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val activeDashProviders = prefs.dashProvidersItems.getAll()

    val actionItems = DashEditAdapter.getDashActionProviders(context).filter {
        it.javaClass.name in activeDashProviders && it.name != AudioPlayer::class.java.name
    }
    val controlItems = DashEditAdapter.getDashControlProviders(context).filter {
        it.javaClass.name in activeDashProviders
    }
    val musicManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val lineSize = prefs.dashLineSize.onGetValue().roundToInt()

    val displayItems = actionItems + controlItems
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(lineSize),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        if (activeDashProviders.contains(AudioPlayer::class.java.name)) item(
            span = { GridItemSpan(lineSize) }) { // TODO abstract DashProviders to Constants
            MusicBar(
                ratio = lineSize.toFloat(),
                audioManager = musicManager,
            )
        }
        itemsIndexed(
            items = displayItems,
            span = { _, item ->
                when (item) {
                    is DashControlProvider -> GridItemSpan(2)
                    else -> GridItemSpan(1)
                }
            },
            key = { _: Int, item: DashProvider -> item.javaClass.name }) { _, item ->
            when (item) {
                is DashControlProvider -> {
                    val enabled = remember {
                        mutableStateOf(item.state)
                    }
                    ControlDashItem(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        icon = painterResource(id = item.icon),
                        description = item.name,
                        ratio = 2.15f,
                        isExtendable = item.extendable,
                        enabled = enabled.value,
                        onClick = {
                            item.state = !enabled.value
                            enabled.value = !enabled.value
                        }
                    )
                }
                is DashActionProvider -> ActionDashItem(
                    icon = painterResource(id = item.icon),
                    description = item.name,
                    onClick = { item.runAction(context) }
                )
            }
        }
    }
}