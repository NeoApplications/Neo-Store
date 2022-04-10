package com.looker.droidify.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.looker.droidify.ui.compose.utils.ButtonStates
import com.looker.droidify.ui.compose.utils.ButtonWork
import com.looker.droidify.ui.compose.utils.Cancelable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InstallButton(
    modifier: Modifier = Modifier,
    buttonState: ButtonStates,
    onClick: (ButtonStates) -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        onClick = { onClick(buttonState) }
    ) {
        AnimatedContent(
            targetState = buttonState,
            transitionSpec = {
                when (targetState) {
                    is Cancelable -> {
                        slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut()
                    }
                    is ButtonWork -> {
                        slideInVertically { height -> -height } + fadeIn() with
                                slideOutVertically { height -> height } + fadeOut()
                    }
                }.using(SizeTransform(clip = false))
            }
        ) {
            Row(
                Modifier
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
                    .padding(ButtonDefaults.ContentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = it.icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = it.text)
            }
        }
    }
}