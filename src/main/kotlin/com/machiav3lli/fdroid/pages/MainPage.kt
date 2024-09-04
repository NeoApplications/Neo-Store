package com.machiav3lli.fdroid.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.POPUP_LONG
import com.machiav3lli.fdroid.POPUP_NONE
import com.machiav3lli.fdroid.POPUP_SHORT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.LatestSyncs
import com.machiav3lli.fdroid.service.worker.SyncRequest
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.ui.components.Tooltip
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.NavRoute
import com.machiav3lli.fdroid.ui.navigation.PagerNavBar
import com.machiav3lli.fdroid.ui.navigation.SlidePager
import com.machiav3lli.fdroid.utility.getLocaleDateString
import kotlinx.collections.immutable.persistentListOf

@OptIn(
    ExperimentalFoundationApi::class,
)
@Composable
fun MainPage(navController: NavHostController, pageIndex: Int) {
    val context = LocalContext.current
    val mActivity = context as NeoActivity

    val successfulSyncs by MainApplication.db.getRepositoryDao().latestUpdatesFlow()
        .collectAsState(initial = LatestSyncs(0L, 0L))

    val showPopup = remember { mutableIntStateOf(POPUP_NONE) }

    val pages = persistentListOf(
        NavItem.Latest,
        NavItem.Explore,
        NavItem.Search,
        NavItem.Installed,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })

    BackHandler {
        if (mActivity.isAppSheetOpen)
            mActivity.navigateProduct("")
        else
            mActivity.moveTaskToBack(true)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = { PagerNavBar(pageItems = pages, pagerState = pagerState) },
        topBar = {
            TopBar(title = stringResource(id = R.string.application_name)) {
                TopBarAction(
                    icon = Phosphor.ArrowsClockwise,
                    description = stringResource(id = R.string.sync_repositories),
                    onLongClick = {
                        showPopup.intValue = POPUP_LONG
                    },
                    onClick = {
                        if (System.currentTimeMillis() - Preferences[Preferences.Key.LastManualSyncTime] >= 10_000L) {
                            Preferences[Preferences.Key.LastManualSyncTime] =
                                System.currentTimeMillis()
                            SyncWorker.enqueueAll(SyncRequest.MANUAL)
                        } else {
                            showPopup.intValue = POPUP_SHORT
                        }
                    }
                )
                TopBarAction(
                    icon = Phosphor.GearSix,
                    description = stringResource(id = R.string.settings)
                ) {
                    navController.navigate(NavRoute.Prefs())
                }

                if (showPopup.intValue != POPUP_NONE) {
                    Tooltip(
                        when (showPopup.intValue) {
                            POPUP_LONG -> stringResource(
                                id = R.string.last_successful_sync,
                                context.getLocaleDateString(successfulSyncs.latest),
                                context.getLocaleDateString(successfulSyncs.latestAll),
                            )

                            else       -> stringResource(id = R.string.wait_to_sync)
                        },
                        showPopup
                    )
                }
            }
        }
    ) { paddingValues ->
        SlidePager(
            modifier = Modifier
                .padding(paddingValues)
                .blockBorder()
                .fillMaxSize(),
            pagerState = pagerState,
            pageItems = pages,
        )
    }
}