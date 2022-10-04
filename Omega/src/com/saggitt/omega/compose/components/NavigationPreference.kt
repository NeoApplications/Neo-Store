package com.saggitt.omega.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.util.addIf

@Composable
fun NavigationPreference(
    title: String,
    route: String,
    modifier: Modifier = Modifier,
    summary: String = "",
    startIcon: Int = 0,
    endIcon: Int = 0,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    val navController = LocalNavController.current
    Column(
        modifier = Modifier.clickable {
            navController.navigate(route)
        }
    ) {
        if (showDivider) {
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
        Row(
            verticalAlignment = verticalAlignment,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        ) {
            if (startIcon != 0) {
                Icon(
                    painter = painterResource(id = startIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.requiredWidth(16.dp))

            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .addIf(!enabled) {
                        alpha(0.3f)
                    }
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )
                if (summary.isNotEmpty()) {
                    Text(
                        text = summary,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6F)
                    )
                }
            }

            if (endIcon != 0) {

                Spacer(modifier = Modifier.requiredWidth(16.dp))
                Icon(
                    painter = painterResource(id = endIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}