package pro.shade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import pro.shade.ui.AppListScreen
import pro.shade.ui.ShadeTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPreferences(this)
        enableEdgeToEdge()
        setContent {
            ShadeTheme {
                AppListScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    prefs = prefs,
                )
            }
        }
    }
}
