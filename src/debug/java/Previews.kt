import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.looker.droidify.ui.compose.pages.app_detail.components.ReleaseItem
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.utility.SampleData

@Preview
@Composable
fun ReleaseItemPreview() {
    AppTheme(blackTheme = false) {
        ReleaseItem(
            release = SampleData.demoRelease
        )
    }
}