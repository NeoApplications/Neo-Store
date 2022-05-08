/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.compose.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.ContributorRow
import com.saggitt.omega.compose.components.ItemLink
import com.saggitt.omega.compose.components.PreferenceGroup
import com.saggitt.omega.compose.components.PreferenceItem
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.theme.kaushanScript

@ExperimentalCoilApi
@Composable
fun AboutMainScreen(navController: NavController) {

    val context = LocalContext.current
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
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
                        modifier = Modifier.requiredSize(84.dp)
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.derived_app_name),
                fontFamily = kaushanScript,
                fontWeight = FontWeight.Normal,
                fontSize = 30.sp,
                color = Color(Utilities.getOmegaPrefs(context).accentColor)
            )

            Text(
                text = stringResource(id = R.string.app_version) + ": "
                        + BuildConfig.VERSION_NAME + " ( Build " + BuildConfig.VERSION_CODE + " )",
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.app_id) + ": " + BuildConfig.APPLICATION_ID,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary
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
        PreferenceGroup(heading = stringResource(id = R.string.about_translators_group)) {
            ContributorRow(
                contributorName = stringResource(id = R.string.contributor2),
                contributorRole = stringResource(id = R.string.contributor_role),
                photoUrl = "https://avatars.githubusercontent.com/u/69337602",
                url = "https://github.com/nonaybay"
            )

            PreferenceItem(
                title = {
                    Text(
                        text = stringResource(id = R.string.about_translators),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                modifier = Modifier
                    .clickable {
                        navController.navigate(
                            Routes.Translators.route
                        )

                    },
                startWidget = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_language),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12F))
                    )
                }
            )
        }

        Spacer(modifier = Modifier.requiredHeight(8.dp))

        PreferenceGroup(heading = stringResource(id = R.string.category__about_licenses)) {
            PreferenceItem(
                title = {
                    Text(
                        text = stringResource(id = R.string.category__about_licenses),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                modifier = Modifier
                    .clickable {
                        navController.navigate(
                            Routes.License.route
                        )

                    },
                startWidget = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_copyright),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12F))
                    )
                }
            )

            PreferenceItem(
                title = {
                    Text(
                        text = stringResource(id = R.string.title__about_changelog),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                modifier = Modifier
                    .clickable {
                        navController.navigate(
                            Routes.Changelog.route
                        )

                    },
                startWidget = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_list),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12F))
                    )
                }
            )
        }
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
        url = "https://github.com/NeoApplications/Neo-Launcher"
    ),
    Link(
        iconResId = R.drawable.ic_channel,
        labelResId = R.string.about_channel,
        url = "https://t.me/neo_applications"
    ),
    Link(
        iconResId = R.drawable.ic_community,
        labelResId = R.string.about_community,
        url = "https://t.me/neo_launcher"
    )
)

private val contributors = listOf(
    TeamMember(
        name = R.string.author,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/6044050",
        webpage = "https://github.com/saulhdev"
    ),
    TeamMember(
        name = R.string.contributor1,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/40302595",
        webpage = "https://github.com/machiav3lli"
    )
)
