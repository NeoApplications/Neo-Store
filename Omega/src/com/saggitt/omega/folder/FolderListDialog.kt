package com.saggitt.omega.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.FolderInfo
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.gestures.GestureController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FolderListDialog(
    folder: FolderInfo,
    openDialogCustom: MutableState<Boolean>
) {
    Dialog(
        onDismissRequest = { openDialogCustom.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FolderListDialogUI(
            folder = folder,
            openDialogCustom = openDialogCustom
        )
    }
}

@Composable
fun FolderListDialogUI(
    folder: FolderInfo,
    openDialogCustom: MutableState<Boolean>
) {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)

    var radius = 16.dp
    if (prefs.themeCornerRadius.onGetValue() > -1f) {
        radius = prefs.themeCornerRadius.onGetValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }
    val colors = RadioButtonDefaults.colors(
        selectedColor = MaterialTheme.colorScheme.primary,
        unselectedColor = Color.Gray
    )

    Card(
        shape = RoundedCornerShape(cornerRadius),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                val gestures = GestureController.getGestureHandlers(context, true, true)
                val (selectedOption, onOptionSelected) = remember {
                    mutableStateOf(folder.swipeUpAction)
                }
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .weight(1f, false)
                ) {
                    itemsIndexed(gestures) { _, item ->
                        ListItemWithIcon(
                            title = item.displayName,
                            modifier = Modifier.clickable {
                                folder.setSwipeUpAction(context, item.javaClass.name.toString())
                                onOptionSelected(item.javaClass.name.toString())
                            },
                            endCheckbox = {
                                RadioButton(
                                    selected = (item.javaClass.name.toString() == selectedOption),
                                    onClick = {
                                        folder.setSwipeUpAction(
                                            context,
                                            item.javaClass.name.toString()
                                        )
                                        onOptionSelected(item.javaClass.name.toString())
                                    },
                                    colors = colors
                                )
                            },
                            verticalPadding = 2.dp
                        )
                    }
                }
            }

            //Button Rows
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { openDialogCustom.value = false }
                )
            }
        }
    }
}