package com.machiav3lli.fdroid.ui.components.privacy

import androidx.annotation.IntRange
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.entity.PrivacyIndicator
import com.machiav3lli.fdroid.ui.compose.icons.Icon
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.icon.Opensource
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleLeft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleRight
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Bug
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyQuestion
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyleft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyright
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.EyeSlash
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Images
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldCheck
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.UserFocus
import com.machiav3lli.fdroid.ui.compose.theme.LightGreen
import com.machiav3lli.fdroid.ui.compose.theme.Orange
import com.materialkolor.ktx.isLight
import kotlinx.coroutines.launch

@Composable
fun PrivacyIndicatorsBar(
    modifier: Modifier = Modifier,
    @IntRange(1, 5) physicalRank: Int,
    @IntRange(1, 5) identificationRank: Int,
    @IntRange(1, 5) sourceRank: Int,
    @IntRange(1, 5) antiFeaturesRank: Int,
    physicalCount: Int = 0,
    identificationCount: Int = 0,
    antiFeaturesCount: Int = 0,
    currentPage: Int,
    onIndicatorClick: (PrivacyIndicator) -> Unit = {},
    onNavigateClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PrivacyIndicatorIcon(
                icon = Phosphor.Images,
                rank = physicalRank,
                badge = physicalCount.takeIf { it > 0 },
                tooltipText = when (physicalRank) {
                    5    -> stringResource(id = R.string.permissions_physical_5)
                    4    -> stringResource(id = R.string.permissions_physical_4)
                    3    -> stringResource(id = R.string.permissions_physical_3)
                    2    -> stringResource(id = R.string.permissions_physical_2)
                    1    -> stringResource(id = R.string.permissions_physical_1)
                    else -> stringResource(id = R.string.permissions_unknown)
                },
                onClick = { onIndicatorClick(PrivacyIndicator.Physical) },
            )
            PrivacyIndicatorIcon(
                icon = Phosphor.UserFocus,
                rank = identificationRank,
                badge = identificationCount.takeIf { it > 0 },
                tooltipText = when (identificationRank) {
                    5    -> stringResource(id = R.string.permissions_identification_5)
                    4    -> stringResource(id = R.string.permissions_identification_4)
                    3    -> stringResource(id = R.string.permissions_identification_3)
                    2    -> stringResource(id = R.string.permissions_identification_2)
                    1    -> stringResource(id = R.string.permissions_identification_1)
                    else -> stringResource(id = R.string.permissions_unknown)
                },
                onClick = { onIndicatorClick(PrivacyIndicator.Identification) },
            )
            PrivacyIndicatorIcon(
                icon = when (sourceRank) {
                    5    -> Phosphor.Copyleft
                    4    -> Icon.Opensource
                    3    -> Phosphor.Copyright
                    2, 1 -> Phosphor.EyeSlash
                    else -> Phosphor.CircleWavyQuestion
                },
                rank = sourceRank,
                tooltipText = when (sourceRank) {
                    5    -> stringResource(id = R.string.source_copyleft)
                    4    -> stringResource(id = R.string.source_open)
                    3    -> stringResource(id = R.string.source_dependencies)
                    2    -> stringResource(id = R.string.source_unclear)
                    1    -> stringResource(id = R.string.source_proprietary)
                    else -> stringResource(id = R.string.unknown)
                },
                onClick = { onIndicatorClick(PrivacyIndicator.SourceCode) },
            )
            PrivacyIndicatorIcon(
                icon = when {
                    antiFeaturesRank == 5 -> Phosphor.ShieldCheck
                    antiFeaturesRank > 2  -> Phosphor.ShieldWarning
                    else                  -> Phosphor.Bug
                },
                rank = antiFeaturesRank,
                badge = antiFeaturesCount.takeIf { it > 0 },
                tooltipText = when (antiFeaturesRank) {
                    5    -> stringResource(id = R.string.anti_features_indicator_5)
                    4    -> stringResource(id = R.string.anti_features_indicator_4)
                    3    -> stringResource(id = R.string.anti_features_indicator_3)
                    2    -> stringResource(id = R.string.anti_features_indicator_2)
                    1    -> stringResource(id = R.string.anti_features_indicator_1)
                    else -> stringResource(id = R.string.anti_features_indicator_3)
                },
                onClick = { onIndicatorClick(PrivacyIndicator.AntiFeatures) },
            )
        }
        IconButton(onClick = onNavigateClick) {
            Icon(
                imageVector = if (currentPage == 0) Phosphor.ArrowCircleRight
                else Phosphor.ArrowCircleLeft,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = stringResource(id = R.string.privacy_panel),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyIndicatorIcon(
    icon: ImageVector,
    rank: Int?,
    badge: Int? = null,
    tooltipText: String,
    onClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state = rememberTooltipState()
    val tint by animateColorAsState(
        when (rank) {
            5    -> Color.Green
            4    -> LightGreen
            3    -> Color.Yellow
            2    -> Orange
            1    -> Color.Red
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        label = "tintColor",
    )
    val onTintIcon by animateColorAsState(
        if (MaterialTheme.colorScheme.surface.isLight()) Color.Black
        else Color.White,
        label = "onTintColor",
    )
    val onTintBadge by animateColorAsState(
        if (tint.isLight()) Color.Black
        else Color.White,
        label = "onTintColor",
    )

    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Below
            ),
        tooltip = {
            PlainTooltip { Text(tooltipText) }
        },
        state = state,
    ) {
        BadgedBox(
            badge = {
                if (badge != null) {
                    Badge(
                        containerColor = tint,
                        contentColor = onTintBadge,
                    ) {
                        Text(text = badge.toString())
                    }
                }
            },
        ) {
            Surface(
                color = tint.copy(0.3f),
                contentColor = onTintIcon,
                shape = MaterialTheme.shapes.small,
                onClick = {
                    onClick()
                    scope.launch { state.show() }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tooltipText,
                    modifier = Modifier
                        .padding(8.dp),
                )
            }
        }
    }
}