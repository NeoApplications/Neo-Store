/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import coil.annotation.ExperimentalCoilApi
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.theme.OmegaTheme
import com.saggitt.omega.theme.kaushanScript
import com.saggitt.omega.ui.component.ContributorRow
import com.saggitt.omega.ui.component.ItemLink
import com.saggitt.omega.ui.component.NavigationActionPreference
import com.saggitt.omega.ui.component.PreferenceGroup

@ExperimentalCoilApi
class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OmegaAppTheme {
                CreateMainScreen()
            }
        }
    }
}

@ExperimentalCoilApi
@Preview
@Composable
fun CreateMainScreen() {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16f))

            ) {
                ResourcesCompat.getDrawable(
                    LocalContext.current.resources,
                    R.mipmap.ic_launcher,
                    LocalContext.current.theme
                )?.let { drawable ->
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.requiredSize(72.dp)
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.derived_app_name),
                fontFamily = kaushanScript,
                fontWeight = FontWeight.Normal,
                fontSize = 30.sp,
                color = MaterialTheme.colors.primary
            )

            Text(
                text = stringResource(id = R.string.app_version) + ": "
                        + BuildConfig.VERSION_NAME + " ( Build " + BuildConfig.VERSION_CODE + " )",
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = OmegaTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            links.map { link ->
                ItemLink(
                    iconResId = link.iconResId,
                    label = stringResource(id = link.labelResId),
                    modifier = Modifier.weight(1f),
                    url = link.url
                )
            }
        }

        Spacer(modifier = Modifier.requiredHeight(8.dp))

        PreferenceGroup(heading = stringResource(id = R.string.about_team)) {
            contributors.forEach {
                ContributorRow(
                    contributorName = stringResource(it.name),
                    contributorRole = stringResource(it.descriptionRes),
                    photoUrl = it.photoUrl,
                    url = it.webpage
                )
            }
        }

        Spacer(modifier = Modifier.requiredHeight(8.dp))

        ContributorRow(
            contributorName = stringResource(id = R.string.contributor2),
            contributorRole = stringResource(id = R.string.contributor_role),
            photoUrl = "https://avatars.githubusercontent.com/u/69337602",
            url = "https://github.com/nonaybay"
        )

        NavigationActionPreference(
            label = stringResource(id = R.string.category__about_licenses),
            subtitle = stringResource(id = R.string.license_gpl),
            destination = subRoute(name = AboutRoutes.LICENSE),
            startWidget = {
                Image(
                    painter = painterResource(id = R.drawable.ic_copyright),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)
                        .background(MaterialTheme.colors.onBackground.copy(alpha = 0.12F))
                )
            }
        )

        NavigationActionPreference(
            label = stringResource(id = R.string.title__about_changelog),
            destination = subRoute(name = AboutRoutes.CHANGELOG),
            startWidget = {
                Image(
                    painter = painterResource(id = R.drawable.ic_list),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)
                        .background(MaterialTheme.colors.onBackground.copy(alpha = 0.12F))
                )
            }
        )

        NavigationActionPreference(
            label = stringResource(id = R.string.about_open_source),
            destination = subRoute(name = AboutRoutes.OPEN_SOURCE),
            startWidget = {
                Image(
                    painter = painterResource(id = R.drawable.ic_copyright),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)
                        .background(MaterialTheme.colors.onBackground.copy(alpha = 0.12F))
                )
            }
        )
    }
}

private data class Link(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int,
    val url: String
)

private data class TeamMember(
    @StringRes val name: Int,
    @StringRes val descriptionRes: Int,
    val photoUrl: String,
    val webpage: String
)

private val links = listOf(
    Link(
        iconResId = R.drawable.ic_github,
        labelResId = R.string.about_source,
        url = "https://github.com/otakuhqz/Omega"
    ),
    Link(
        iconResId = R.drawable.ic_group,
        labelResId = R.string.about_channel,
        url = "https://t.me/omegalauncher"
    ),
    Link(
        iconResId = R.drawable.ic_community,
        labelResId = R.string.about_community,
        url = "https://t.me/omegalauncher_group"
    )
)

private val contributors = listOf(
    TeamMember(
        name = R.string.author,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/6044050",
        webpage = "https://github.com/otakuhqz"
    ),
    TeamMember(
        name = R.string.contributor1,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/40302595",
        webpage = "https://github.com/machiav3lli"
    )
)

val LocalRoute = compositionLocalOf { "" }

@Composable
fun subRoute(name: String) = "${LocalRoute.current}$name/"

object AboutRoutes {
    const val LICENSE = "license"
    const val OPEN_SOURCE = "libraries"
    const val CHANGELOG = "changelog"
}