/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.saggitt.omega.iconpack

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.util.ComponentKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.theme.OmegaAppTheme

class EditIconActivity : AppCompatActivity() {

    val packs = IconPackProvider.INSTANCE.get(this).getIconPackList()
    private val isFolder by lazy { intent.getBooleanExtra(EXTRA_FOLDER, false) }
    private val component by lazy {
        if (intent.hasExtra(EXTRA_COMPONENT)) {
            ComponentKey(
                intent.getParcelableExtra(EXTRA_COMPONENT),
                intent.getParcelableExtra(EXTRA_USER)
            )
        } else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = intent.getStringExtra(EXTRA_TITLE)
        setContent {
            OmegaAppTheme {
                EditIconScreen(app, component, packs, isFolder)
            }
        }

    }

    companion object {
        const val EXTRA_ENTRY = "entry"
        const val EXTRA_TITLE = "title"
        const val EXTRA_COMPONENT = "component"
        const val EXTRA_USER = "user"
        const val EXTRA_FOLDER = "is_folder"

        fun newIntent(
            context: Context,
            title: String,
            isFolder: Boolean,
            componentKey: ComponentKey? = null
        ): Intent {
            return Intent(context, EditIconActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                componentKey?.run {
                    putExtra(EXTRA_COMPONENT, componentName)
                    putExtra(EXTRA_USER, user)
                    putExtra(EXTRA_FOLDER, isFolder)
                }
            }
        }
    }
}

@Composable
fun EditIconScreen(
    title: String?,
    component: ComponentKey?,
    iconPacks: List<IconPackInfo>,
    isFolder: Boolean
) {
    Column {
        Text(
            text = title ?: "",
            modifier = Modifier
                .padding(start = 24.dp)
                .fillMaxWidth(),
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(8f))
                .horizontalScroll(scrollState)

        ) {
            //Original Icon
            Image(
                painter = rememberDrawablePainter(drawable = OmegaLauncher.currentEditIcon),
                contentDescription = null,
                modifier = Modifier.requiredSize(60.dp)
            )

            //Vertical Divider
            Divider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp)
                    .width(1.dp)
            )

            //Package Icons
            val iconDpi = LocalContext.current.resources.configuration.densityDpi
            val ip = IconPackProvider.INSTANCE.get(LocalContext.current)
            iconPacks.forEach {
                if (it.packageName == "") {
                    Image(
                        painter = rememberDrawablePainter(drawable = OmegaLauncher.currentEditIcon),
                        contentDescription = null,
                        modifier = Modifier.requiredSize(60.dp)
                    )
                    return@forEach
                }
                val pack: IconPack? = ip.getIconPack(it.packageName)
                if (pack != null) {
                    val iconEntry = pack.getIcon(component!!.componentName)
                    if (iconEntry != null) {
                        val mIcon: Drawable? = ip.getDrawable(
                            iconEntry,
                            iconDpi,
                            component.user
                        )
                        if (mIcon != null) {
                            Image(
                                painter = rememberDrawablePainter(drawable = mIcon),
                                contentDescription = null,
                                modifier = Modifier
                                    .requiredSize(60.dp)
                                    .padding(start = 8.dp, end = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        //Divider
        Divider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp)
        )

        //Icon Packs
        iconPacks.forEach {
            ListItemWithIcon(
                title = {
                    Text(
                        text = it.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp
                    )
                },
                modifier = Modifier
                    .clickable {

                    }
                    .padding(start = 16.dp),
                description = {},
                startIcon = {
                    Image(
                        painter = rememberDrawablePainter(drawable = it.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.12F)
                            )
                    )
                },
                horizontalPadding = 8.dp,
                verticalPadding = 8.dp
            )
        }

        //Divider
        Divider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp)
        )
    }
}