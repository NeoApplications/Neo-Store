package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DialogPositiveButton(
    modifier: Modifier = Modifier,
    textId: Int = android.R.string.ok,
    onClick: () -> Unit = {}
) {
    TextButton(
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = stringResource(id = textId),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
        )
    }
}

@Composable
fun DialogNegativeButton(
    modifier: Modifier = Modifier,
    textId: Int = android.R.string.cancel,
    onClick: () -> Unit = {}
) {
    TextButton(
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = textId),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 8.dp)
        )
    }
}