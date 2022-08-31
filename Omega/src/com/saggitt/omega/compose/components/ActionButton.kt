package com.saggitt.omega.compose.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ElevatedActionButton(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    @StringRes textId: Int = android.R.string.ok,
    @DrawableRes iconId: Int = -1,
    onClick: () -> Unit = {}
) {
    ElevatedButton(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
        contentPadding = PaddingValues(vertical = 18.dp, horizontal = 8.dp),
        onClick = onClick
    ) {
        if (iconId != -1)
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = stringResource(id = textId)
            )
        Text(
            text = stringResource(id = textId),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
