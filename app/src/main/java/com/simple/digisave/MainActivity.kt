package com.simple.digisave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.simple.digisave.ui.navigation.AppNavGraph
import com.simple.digisave.ui.theme.DigiSaveTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DigiSaveTheme {
                val rootNavController = rememberNavController()
                AppNavGraph(rootNavController = rootNavController)
            }
        }
    }
}
