package com.louaraya.macrame

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.louaraya.macrame.ui.navigation.AppRoot
import com.louaraya.macrame.ui.theme.MacrameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // La paleta es clara: íconos oscuros en las barras del sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        val container = (application as MacrameApp).container
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                MacrameTheme {
                    AppRoot()
                }
            }
        }
    }
}
