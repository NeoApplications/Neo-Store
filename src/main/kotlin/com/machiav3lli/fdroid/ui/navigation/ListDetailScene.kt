package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene

@OptIn(ExperimentalMaterial3Api::class)
class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>?,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOfNotNull(listEntry, detailEntry)

    override val content: @Composable (() -> Unit) = {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.weight(47.5f)) {
                AnimatedContent(
                    targetState = listEntry,
                    contentKey = { entry -> entry.contentKey },
                ) { entry ->
                    entry.Content()
                }
            }
            if (detailEntry != null) AnimatedContent(
                targetState = detailEntry,
                modifier = Modifier.weight(52.5f),
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                }
            ) { entry ->
                entry.Content()
            }
        }
    }

    companion object {
        internal const val LIST_KEY = "ListDetailScene-List"
        internal const val DETAIL_KEY = "ListDetailScene-Detail"

        fun listPane() = mapOf(LIST_KEY to true)
        fun detailPane() = mapOf(DETAIL_KEY to true)
    }
}
