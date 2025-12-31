package com.simple.digisave.ui.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simple.digisave.data.repository.CategoryWithTotal
import com.simple.digisave.ui.theme.AccentTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val grouped = categories.groupBy { it.group }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Category", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp) // 👉 spacing ONLY between groups
        ) {
            grouped.forEach { (group, cats) ->
                item {
                    Column {
                        // Group Header
                        Text(
                            text = group,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Group container (tight list)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                cats.forEachIndexed { index, category ->
                                    CategoryRow(
                                        category = category,
                                        onCategoryClick = {
                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("selectedCategory", category)
                                            navController.popBackStack()
                                        }
                                    )

                                    // Divider between items (but not after last)
                                    if (index < cats.lastIndex) {
                                        Divider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRow(
    category: CategoryWithTotal,
    onCategoryClick: (CategoryWithTotal) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onCategoryClick(category) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${category.icon} ${category.name}",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )
        Text(
            text = "$${"%.2f".format(category.total)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = when {
                category.total < 0 -> MaterialTheme.colorScheme.error
                category.total > 0 -> AccentTeal
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
