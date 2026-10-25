package za.co.hbtechinc.isibalo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import za.co.hbtechinc.isibalo.ui.MainScaffold
import za.co.hbtechinc.isibalo.ui.SplashScreen
import za.co.hbtechinc.isibalo.ui.theme.IsibaloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bars are always blue, so keep system bar icons light in both themes.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        val store = (application as IsibaloApp).container.themeStore
        setContent {
            val mode by store.mode.collectAsStateWithLifecycle()
            var splash by rememberSaveable { mutableStateOf(true) }
            LaunchedEffect(Unit) { delay(1400); splash = false }
            IsibaloTheme(mode) {
                Crossfade(targetState = splash, animationSpec = tween(450), label = "splash") { showing ->
                    if (showing) SplashScreen() else MainScaffold(mode) { store.set(it) }
                }
            }
        }
    }
}
