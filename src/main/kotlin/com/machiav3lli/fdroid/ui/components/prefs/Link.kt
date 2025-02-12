package com.machiav3lli.fdroid.ui.components.prefs

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.machiav3lli.fdroid.data.entity.LinkRef

@Composable
fun LinkPreference(
    link: LinkRef,
    modifier: Modifier = Modifier,
    index: Int = 1,
    groupSize: Int = 1,
) {
    val context = LocalContext.current

    BasePreference(
        modifier = modifier,
        titleId = link.titleId,
        index = index,
        groupSize = groupSize,
        onClick = {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(link.url)
                )
            )
        }
    )
}