package ru.custle.mobile

import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.ui.theme.CustleTheme

class MainActivity : ComponentActivity() {
    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as CustleApplication
        pendingDeepLink = intent?.dataString

        setContent {
            CompositionLocalProvider(LocalAppContainer provides app.container) {
                val isDark by app.container.sessionStore.darkThemeFlow.collectAsState(initial = true)
                CustleTheme(darkTheme = isDark) {
                    CustleApp(
                        pendingDeepLink = pendingDeepLink,
                        onDeepLinkConsumed = { pendingDeepLink = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink = intent.dataString
    }
}
