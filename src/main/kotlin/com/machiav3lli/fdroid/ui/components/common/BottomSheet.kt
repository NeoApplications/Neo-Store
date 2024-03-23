package com.machiav3lli.fdroid.ui.components.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    sheetState: SheetState,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    onDismiss: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        scrimColor = Color.Transparent,
        dragHandle = null,
        shape = shape,
        onDismissRequest = onDismiss,
        content = content,
    )
}