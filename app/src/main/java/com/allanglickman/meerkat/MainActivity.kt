package com.allanglickman.meerkat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.allanglickman.meerkat.ui.DetectionRoute
import com.allanglickman.meerkat.ui.theme.MeerkatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeerkatTheme {
                DetectionRoute()
            }
        }
    }
}
