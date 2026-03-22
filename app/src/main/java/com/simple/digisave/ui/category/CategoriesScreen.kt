package com.simple.digisave.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simple.digisave.data.repository.CategoryWithTotal
import com.simple.digisave.ui.components.EmptyState
import com.simple.digisave.ui.theme.ExpenseText
import com.simple.digisave.ui.theme.IncomeText
import com.simple.digisave.ui.theme.PastelGreen
import com.simple.digisave.ui.theme.PastelRed
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    navController: NavController,
    typeFilter: String = "expense",          // "income" or "expense"
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()

    val isIncomeFilter = typeFilter == "income"
    val accentColor    = if (isIncomeFilter) IncomeText else ExpenseText

    // Filter to only relevant type, then group by the category's group field
    val filtered = categories.filter { it.type == typeFilter }
    val grouped  = filtered.groupBy { it.group }

    // ── Custom category dialog state ──────────────────────────────────────
    var showAddDialog by remember { mutableStateOf(false) }
    var customName    by remember { mutableStateOf("") }
    var customIcon    by remember { mutableStateOf("") }
    var nameError     by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                customName = ""; customIcon = ""; nameError = false
            },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isIncomeFilter) "New Income Category" else "New Expense Category",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Emoji icon input
                    OutlinedTextField(
                        value = customIcon,
                        onValueChange = { if (it.length <= 3) customIcon = it },
                        label = { Text("Icon (paste an emoji)") },
                        placeholder = { Text(if (isIncomeFilter) "💡" else "📌") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Name input
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it; nameError = false },
                        label = { Text("Category Name") },
                        placeholder = { Text("e.g. Side Income") },
                        singleLine = true,
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("Name cannot be empty") }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isBlank()) {
                            nameError = true
                        } else {
                            viewModel.addCategory(
                                name  = customName.trim(),
                                icon  = customIcon.trim().ifBlank {
                                    if (isIncomeFilter) "💡" else "📌"
                                },
                                type  = typeFilter,
                                group = "Custom"
                            )
                            showAddDialog = false
                            customName = ""; customIcon = ""; nameError = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Add Category")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    customName = ""; customIcon = ""; nameError = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isIncomeFilter) "Select Income Category"
                               else "Select Expense Category",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Custom") },
                containerColor = accentColor,
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        }
    ) { innerPadding ->

        if (filtered.isEmpty()) {
            EmptyState(
                icon = if (isIncomeFilter) "💼" else "🧾",
                message = "No categories yet",
                subtitle = "Categories will appear here"
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            grouped.forEach { (group, cats) ->
                item {
                    Column {

                        // ── Section header ─────────────────────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp, start = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(20.dp)
                                    .background(
                                        color = accentColor.copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = group,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(accentColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${cats.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = accentColor
                                )
                            }
                        }

                        // ── Category card ──────────────────────────────
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                cats.forEachIndexed { index, category ->
                                    CategoryRow(
                                        category     = category,
                                        accentColor  = accentColor,
                                        onCategoryClick = {
                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("selectedCategory", category)
                                            navController.popBackStack()
                                        }
                                    )
                                    if (index < cats.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
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
    accentColor: androidx.compose.ui.graphics.Color,
    onCategoryClick: (CategoryWithTotal) -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val isIncome          = category.type == "income"
    val iconBg            = if (isIncome) PastelGreen.copy(alpha = 0.30f) else PastelRed.copy(alpha = 0.20f)

    // Amount driven by actual net total sign — handles mixed-use categories correctly
    val hasTotal    = category.total != 0.0
    val netPositive = category.total > 0
    val amountColor = if (netPositive) IncomeText else ExpenseText
    val amountText  = if (hasTotal) {
        val formatted = currencyFormatter.format(kotlin.math.abs(category.total))
        if (netPositive) "+$formatted" else "-$formatted"
    } else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick(category) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // ── Left: icon + name ─────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(color = iconBg, shape = RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.icon, fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // ── Right: amount + chevron ───────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (amountText != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = amountColor
                    )
                    Text(
                        text = "all time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
                Spacer(Modifier.width(10.dp))
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
