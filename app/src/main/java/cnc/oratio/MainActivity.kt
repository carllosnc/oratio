package cnc.oratio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cnc.oratio.data.repository.PrayerRepository
import cnc.oratio.notification.NotificationHelper
import cnc.oratio.ui.OratioApp
import cnc.oratio.ui.theme.OratioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = PrayerRepository(applicationContext)
        val targetPrayerId = intent?.getStringExtra(NotificationHelper.EXTRA_PRAYER_ID)

        setContent {
            OratioTheme {
                OratioApp(
                    repository = repository,
                    targetPrayerId = targetPrayerId
                )
            }
        }
    }
}