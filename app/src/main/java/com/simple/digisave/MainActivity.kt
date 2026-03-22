package com.simple.digisave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.simple.digisave.data.repository.CategoryRepository
import com.simple.digisave.ui.navigation.AppNavGraph
import com.simple.digisave.ui.theme.DigiSaveTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var categoryRepository: CategoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            categoryRepository.ensureDefaultCategories()

            setContent {
                DigiSaveTheme {
                    val rootNavController = rememberNavController()
                    AppNavGraph(rootNavController = rootNavController)
                }
            }
        }
    }
}
