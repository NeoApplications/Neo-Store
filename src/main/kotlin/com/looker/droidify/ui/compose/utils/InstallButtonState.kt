package com.looker.droidify.ui.compose.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface ButtonStates {
    val icon: ImageVector
    val text: String
}

sealed class Cancelable(
    override val text: String,
    override val icon: ImageVector = Icons.Rounded.Close
) : ButtonStates

object Connecting : Cancelable("Connecting")
object Downloading : Cancelable("Downloading")
object Installing : Cancelable("Installing")


sealed class ButtonWork(
    override val text: String,
    override val icon: ImageVector = Icons.Rounded.Download
) : ButtonStates

object Download : ButtonWork("Download")
object Install : ButtonWork("Install")
object Launch : ButtonWork("Launch", Icons.Rounded.Launch)