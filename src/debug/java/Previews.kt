import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.machiav3lli.fdroid.ui.compose.pages.app_detail.components.AntiFeaturesGrid
import com.machiav3lli.fdroid.ui.compose.pages.app_detail.components.PermissionGrid
import com.machiav3lli.fdroid.ui.compose.pages.app_detail.components.ReleaseItem
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.compose.utils.CustomChip
import com.machiav3lli.fdroid.utility.SampleData

@Preview
@Composable
fun ReleaseItemPreview() {
    AppTheme(blackTheme = false) {
        ReleaseItem(
            release = SampleData.demoRelease,
            repository = SampleData.demoRepository
        )
    }
}

@Preview
@Composable
fun CustomChipPrev() {
    AppTheme(blackTheme = false, darkTheme = true) {
        CustomChip(text = "Test Chip")
    }
}

@Preview
@Composable
fun ChipGridPreview() {
    AppTheme(blackTheme = false) {
        Column {
            PermissionGrid(
                permissions = listOf(
                    "test",
                    "test",
                    "te12312st",
                    "te123st",
                    "te123123st",
                    "test",
                    "test",
                    "test",
                    "test",
                    "test",
                    "te12312st",
                    "te123st",
                    "te123123st",
                    "test",
                    "test"
                )
            )
            AntiFeaturesGrid(
                antiFeatures = listOf(
                    "test",
                    "test",
                    "te12312st",
                    "te123st",
                    "te123123st",
                    "test",
                    "test",
                    "test",
                    "test",
                    "test",
                    "te12312st",
                    "te123st",
                    "te123123st",
                    "test",
                    "test"
                )
            )
        }
    }
}
