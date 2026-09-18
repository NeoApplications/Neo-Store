package com.machiav3lli.fdroid.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.content.weightEntries
import com.machiav3lli.fdroid.data.entity.PermissionWeights
import com.machiav3lli.fdroid.ui.components.DialogNegativeButton
import com.machiav3lli.fdroid.ui.components.DialogPositiveButton
import kotlin.math.roundToInt

private val WEIGHT_SLIDER_RANGE = 0f..2f

@Composable
fun PermissionWeightsPrefDialogUI(
    openDialogCustom: MutableState<Boolean>,
) {
    val currentWeights = Preferences[Preferences.Key.PermissionWeightsKey]
    var editedWeights by remember { mutableStateOf(currentWeights) }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.prefs_permission_weights),
                style = MaterialTheme.typography.titleLarge,
            )

            weightEntries.forEach { group ->
                val weightValue = PermissionWeights.getWeightForGroup(editedWeights, group)

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                    leadingContent = {
                        Icon(imageVector = group.icon, contentDescription = group.name)
                    },
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(text = stringResource(group.labelId))
                            Text(
                                text = "%.2f".format(weightValue),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    supportingContent = {
                        Slider(
                            value = weightValue,
                            valueRange = WEIGHT_SLIDER_RANGE,
                            steps = 3,
                            onValueChange = { newValue ->
                                val roundedValue = (newValue * 4).roundToInt() / 4.0f
                                editedWeights = PermissionWeights.setWeightForGroup(
                                    editedWeights,
                                    group,
                                    roundedValue,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DialogNegativeButton(
                    textId = R.string.action_reset,
                    onClick = {
                        editedWeights = PermissionWeights.DEFAULT
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                DialogNegativeButton(
                    onClick = { openDialogCustom.value = false }
                )
                DialogPositiveButton(
                    onClick = {
                        Preferences[Preferences.Key.PermissionWeightsKey] = editedWeights
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}