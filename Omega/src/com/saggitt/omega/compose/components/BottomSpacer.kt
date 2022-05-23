package com.saggitt.omega.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.android.quickstep.SysUINavigationMode

@Composable
fun BottomSpacer() {
    Box(
        contentAlignment = Alignment.BottomStart
    ) {
        Spacer(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
        )
        if (SysUINavigationMode.getMode(LocalContext.current) != SysUINavigationMode.Mode.NO_BUTTON) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
            )
        } else {
            Spacer(
                modifier = Modifier
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .fillMaxWidth()
                    .pointerInput(Unit) {},
            )
        }
    }
}