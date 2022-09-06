package com.saggitt.omega.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.util.addIf

@Composable
fun NavigationActionPreference(
    title: String,
    destination: String,
    modifier: Modifier = Modifier,
    summary: String = "",
    icon: Int = 0,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    val navController = LocalNavController.current
    Column(
        modifier = Modifier.clickable {
            navController.navigate(destination)
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
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12F))
            )

            Spacer(modifier = Modifier.requiredWidth(16.dp))

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
        }
    }
}