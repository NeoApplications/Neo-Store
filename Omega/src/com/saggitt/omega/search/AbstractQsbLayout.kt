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

package com.saggitt.omega.search

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.Config

abstract class AbstractQsbLayout(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    var mContext: Context = context
    protected var prefs: OmegaPreferences = Utilities.getOmegaPrefs(mContext)
    protected var controller = SearchProviderController.getInstance(getContext())
    protected var searchProvider: SearchProvider = controller.searchProvider

    private var micIconView: ImageView? = null
    private var searchLogoView: ImageView? = null
    private var lensIcon: ImageView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        searchLogoView = findViewById<AppCompatImageView?>(R.id.search_engine_logo).apply {
            setImageDrawable(searchProvider.icon)
            setOnClickListener {
                if (searchProvider.supportsFeed) {
                    searchProvider.startFeed { intent ->
                        mContext.startActivity(intent)
                    }
                } else {
                    mContext.startActivity(
                        Intent("android.search.action.GLOBAL_SEARCH").addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        ).setPackage(searchProvider.packageName)
                    )
                }
            }
        }

        micIconView = findViewById<AppCompatImageView?>(R.id.mic_icon).apply {
            if (searchProvider.supportsVoiceSearch) {
                if (searchProvider.supportsAssistant) {
                    setImageDrawable(searchProvider.assistantIcon)
                    setOnClickListener {
                        searchProvider.startAssistant { intent -> mContext.startActivity(intent) }
                    }
                } else {
                    setImageDrawable(searchProvider.voiceIcon)
                    setOnClickListener {
                        searchProvider.startVoiceSearch { intent -> mContext.startActivity(intent) }
                    }
                }
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        lensIcon = findViewById<ImageView?>(R.id.lens_icon).apply {
            val lensIntent = Intent.makeMainActivity(
                ComponentName(
                    Config.LENS_PACKAGE,
                    Config.LENS_ACTIVITY
                )
            ).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )

            if (searchProvider.packageName == Config.GOOGLE_QSB && mContext.packageManager.resolveActivity(
                    lensIntent,
                    0
                ) != null
            ) {
                setImageResource(R.drawable.ic_lens_color)

                setOnClickListener {
                    mContext.startActivity(lensIntent)
                }
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        setOnClickListener { view: View? ->
            mContext.startActivity(
                Intent("android.search.action.GLOBAL_SEARCH").addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                ).setPackage(searchProvider.packageName)
            )
        }
    }

}