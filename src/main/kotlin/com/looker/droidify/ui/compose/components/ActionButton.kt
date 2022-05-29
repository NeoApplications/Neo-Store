package com.looker.droidify.ui.compose.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.droidify.entity.PackageState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainActionButton(
    modifier: Modifier = Modifier,
    packageState: PackageState,
    onClick: (PackageState) -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        onClick = { onClick(packageState) }
    ) {
        AnimatedContent(
            targetState = packageState,
            // TODO Fix redrawing changing state 
            /*transitionSpec = {
                when (targetState) {
                    is Cancelable -> {
                        slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut()
                    }
                    is ButtonWork -> {
                        (slideInVertically { height -> -height } + fadeIn() with
                                slideOutVertically { height -> height } + fadeOut())
                    }
                }
                    .using(SizeTransform(clip = false))
            }*/
        ) {
            Row(
                Modifier
                    .defaultMinSize(minHeight = ButtonDefaults.MinHeight)
                    .padding(ButtonDefaults.ContentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = it.icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = it.textId))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    packageState: PackageState?,
    onClick: () -> Unit
) {
    packageState?.let {
        ElevatedButton(
            modifier = modifier,
            onClick = { onClick() }
        ) {
            Row(
                Modifier
                    .defaultMinSize(minHeight = ButtonDefaults.MinHeight)
                    .padding(
                        vertical = ButtonDefaults.ContentPadding.calculateTopPadding(),
                        horizontal = 0.dp
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = it.icon, contentDescription = null)
            }
        }
    }
}