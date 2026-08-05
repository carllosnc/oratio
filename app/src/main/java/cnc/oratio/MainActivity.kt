package cnc.oratio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cnc.oratio.data.repository.PrayerRepository
import cnc.oratio.ui.MainPrayerScreen
import cnc.oratio.ui.theme.OratioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = PrayerRepository(applicationContext)

        setContent {
            OratioTheme {
                MainPrayerScreen(repository = repository)
            }
        }
    }
}