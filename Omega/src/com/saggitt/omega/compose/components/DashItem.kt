package com.saggitt.omega.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.launcher3.R

@Composable
fun ControlDashItem(
    modifier: Modifier = Modifier,
    ratio: Float = 3f,
    icon: Painter,
    description: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    isExtendable: Boolean = true,
    enabled: Boolean = false,
    onClick: () -> Unit
) {
    CardButton(
        modifier = modifier,
        ratio = ratio,
        icon = icon,
        description = description,
        tint = tint,
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f),
            painter = icon,
            contentDescription = description
        )
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = description,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (isExtendable) Image(
                painter = painterResource(id = R.drawable.ic_explore),
                contentDescription = description
            )
        }
    }
}

@Preview
@Composable
fun ControlDashItemPreview() {
    ControlDashItem(
        icon = painterResource(id = R.drawable.ic_desktop),
        description = "ControlThis"
    ) {

    }
}

@Composable
fun ActionDashItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    description: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = false,
    onClick: () -> Unit
) {
    CardButton(
        modifier = modifier.height(IntrinsicSize.Min),
        ratio = 1f,
        icon = icon,
        description = description,
        tint = tint,
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f),
            painter = icon,
            contentDescription = description
        )
    }
}

@Preview
@Composable
fun ActionDashItemPreview() {
    ActionDashItem(icon = painterResource(id = R.drawable.ic_desktop), description = "ActionThat") {

    }
}

@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    ratio: Float = 1f,
    icon: Painter,
    tint: Color = MaterialTheme.colorScheme.primary,
    description: String,
    enabled: Boolean = false,
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit) = {
        Icon(painter = icon, contentDescription = description)
    }
) {
    ElevatedButton(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(ratio),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
            contentColor = if (enabled) MaterialTheme.colorScheme.background else tint
        ),
        contentPadding = PaddingValues(8.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = { onClick() },
        content = content
    )
}



