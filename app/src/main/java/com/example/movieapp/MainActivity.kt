package com.example.movieapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.movieapp.core.ui.theme.MovieAppTheme
import com.example.movieapp.domain.InjectAutomationTokenUseCase
import com.example.movieapp.screens.root.MovieAppRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var injectAutomationToken: InjectAutomationTokenUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (BuildConfig.IS_AUTOMATION) {
            val accessToken = intent.getStringExtra(EXTRA_AUTH_TOKEN)
            Log.d(TAG, "IS_AUTOMATION=true | auth_token received: ${accessToken != null}")
            if (accessToken != null) {
                val refreshToken = intent.getStringExtra(EXTRA_REFRESH_TOKEN) ?: accessToken
                Log.d(TAG, "Injecting tokens via runBlocking…")
                // runBlocking ensures the token is fully persisted before setContent runs,
                // so SplashViewModel always sees hasSavedToken() == true on first composition.
                runBlocking { injectAutomationToken(accessToken, refreshToken) }
                Log.d(TAG, "Tokens injected successfully.")
            } else {
                Log.w(TAG, "auth_token extra is NULL — app will route to Login!")
            }
        }

        setContent {
            MyApp {
                MovieAppRoot()
            }
        }
    }

    companion object {
        private const val TAG = "AutomationToken"

        /** adb shell am start -n …/.MainActivity --es auth_token "eyJ…" */
        const val EXTRA_AUTH_TOKEN = "auth_token"

        /** adb shell am start -n …/.MainActivity --es refresh_token "eyJ…" */
        const val EXTRA_REFRESH_TOKEN = "refresh_token"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(content: @Composable () -> Unit) {
    MovieAppTheme {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp {
        MovieAppRoot()
    }
}
