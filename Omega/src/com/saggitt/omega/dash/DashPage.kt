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
import androidx.compose.ui.unit.dp
import com.android.launcher3.Utilities
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.compose.components.ActionDashItem
import com.saggitt.omega.compose.components.ControlDashItem
import com.saggitt.omega.compose.components.MusicBar

// TODO add better support for horizontal
@Composable
fun DashPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val allActionItems = DashEditAdapter.getDashActionProviders(context)
    val allControlItems = DashEditAdapter.getDashControlProviders(context)
    val activeDashProviders = prefs.desktopDashProviders.getAll()
    val musicManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val lineSize = prefs.desktopDashLineSize.toInt()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(lineSize),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        if (activeDashProviders.contains("2")) item(span = { GridItemSpan(lineSize) }) { // TODO abstract DashProviders to Constants
            MusicBar(
                ratio = lineSize.toFloat(),
                audioManager = musicManager,
            )
        }
        itemsIndexed(items = activeDashProviders.mapNotNull { itemId ->
            allControlItems.find { it.itemId.toString() == itemId }
                ?: allActionItems.find { it.itemId.toString() == itemId && it.itemId != 2 }
        }, span = { _, item ->
            when (item) {
                is DashControlProvider -> GridItemSpan(2)
                else -> GridItemSpan(1)
            }
        }, key = { _: Int, item: DashProvider -> item.itemId }) { _, item ->
            when (item) {
                is DashControlProvider -> {
                    val (enabled, enable) = remember {
                        mutableStateOf(item.state)
                    }
                    ControlDashItem(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        icon = rememberDrawablePainter(drawable = item.icon),
                        description = item.name,
                        ratio = 2.15f,
                        isExtendable = item.extendable,
                        enabled = enabled,
                        onClick = {
                            item.state = !enabled
                            enable(!enabled)
                        }
                    )
                }
                is DashActionProvider -> ActionDashItem(
                    icon = rememberDrawablePainter(drawable = item.icon),
                    description = item.name,
                    onClick = { item.runAction(context) }
                )
            }
        }
    }
}