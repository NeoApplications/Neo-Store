package com.machiav3lli.fdroid.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.components.ActionButton
import com.machiav3lli.fdroid.ui.compose.components.FlatActionButton

@Composable
fun ActionsDialogUI(
    titleText: String,
    messageText: String,
    openDialogCustom: MutableState<Boolean>,
    primaryText: String,
    primaryIcon: ImageVector? = null,
    primaryAction: (() -> Unit) = {},
    secondaryText: String = "",
    secondaryIcon: ImageVector? = null,
    secondaryAction: (() -> Unit)? = null,
) {
    val scrollState = rememberScrollState()

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .weight(1f, false)
            ) {
                Text(text = messageText, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                FlatActionButton(text = stringResource(id = R.string.cancel)) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                if (secondaryAction != null && secondaryText.isNotEmpty()) {
                    ActionButton(
                        text = secondaryText,
                        icon = secondaryIcon,
                        positive = false
                    ) {
                        secondaryAction()
                        openDialogCustom.value = false
                    }
                    Spacer(Modifier.requiredWidth(8.dp))
                }
                ActionButton(
                    text = primaryText,
                    icon = primaryIcon,
                ) {
                    primaryAction()
                    openDialogCustom.value = false
                }
            }
        }
    }
}