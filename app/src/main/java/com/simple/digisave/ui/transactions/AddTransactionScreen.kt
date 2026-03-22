package com.simple.digisave.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simple.digisave.ui.components.DigiSaveTopBar
import com.simple.digisave.ui.dashboard.DashboardViewModel
import com.simple.digisave.data.repository.CategoryWithTotal
import com.simple.digisave.ui.theme.ExpenseText
import com.simple.digisave.ui.theme.IncomeText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    preselectedType: String? = null,
    userId: String = "",
    viewModel: DashboardViewModel = hiltViewModel()
) {

    // -----------------------------
    // FORM STATES
    // -----------------------------
    var title by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var isIncome by rememberSaveable { mutableStateOf(true) } // updated by FAB
    var selectedCategory by rememberSaveable { mutableStateOf<CategoryWithTotal?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf("Select Date") }
    var selectedTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    // -----------------------------
    // APPLY FAB TYPE ONCE
    // -----------------------------
    LaunchedEffect(preselectedType) {
        isIncome = preselectedType == "income"
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Listen for returned category
    val backStackEntry by navController.currentBackStackEntryAsState()
    val savedState = backStackEntry?.savedStateHandle

    LaunchedEffect(savedState) {
        savedState?.get<CategoryWithTotal>("selectedCategory")?.let {
            selectedCategory = it
            savedState.remove<CategoryWithTotal>("selectedCategory")
        }
    }

    // -----------------------------
    // SCREEN UI
    // -----------------------------
    Scaffold(
        topBar = {
            DigiSaveTopBar(
                title = if (isIncome) "Add Income" else "Add Expense",
                showBackButton = true,
                navController = navController
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },

        bottomBar = {
            // SAVE BUTTON
            FilledTonalButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        title.isBlank() -> scope.launch {
                            snackbarHostState.showSnackbar("⚠️ Enter a title")
                        }

                        amount == null || amount <= 0 -> scope.launch {
                            snackbarHostState.showSnackbar("⚠️ Enter a valid amount greater than 0")
                        }

                        selectedCategory == null -> scope.launch {
                            snackbarHostState.showSnackbar("⚠️ Pick a category")
                        }

                        userId.isBlank() -> scope.launch {
                            snackbarHostState.showSnackbar("⚠️ User not logged in")
                        }

                        else -> {
                            val finalAmount = if (isIncome) amount else -amount
                            viewModel.addTransaction(
                                userId = userId,
                                title = title,
                                amount = finalAmount,
                                categoryId = selectedCategory!!.id,
                                note = note,
                                timestamp = selectedTimestamp
                            )
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Transaction", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // -----------------------------
            // AMOUNT INPUT
            // -----------------------------
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeText else ExpenseText
                ),
                placeholder = { Text("0.00", fontSize = 26.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text(
                        text = if (isIncome) "+" else "-",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) IncomeText else ExpenseText
                    )
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // -----------------------------
            // TITLE / PAYEE
            // -----------------------------
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Payee / Title") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // -----------------------------
            // CATEGORY SELECTOR
            // -----------------------------
            SelectableField(
                value = selectedCategory?.name ?: "Choose Category",
                label = "Category",
                leadingIcon = { Icon(Icons.Default.Category, null) },
                onClick = { navController.navigate("categories") }
            )

            // -----------------------------
            // DATE SELECTOR
            // -----------------------------
            SelectableField(
                value = dateText,
                label = "Date",
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                onClick = { showDatePicker = true }
            )

            // -----------------------------
            // NOTE INPUT
            // -----------------------------
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    // -----------------------------
    // DATE PICKER dialog
    // -----------------------------
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            // selectedDateMillis is always UTC midnight — read it with a UTC calendar
                            // to get the exact date the user tapped, then rebuild in local time at noon
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            utcCal.timeInMillis = millis

                            val localCal = Calendar.getInstance()
                            localCal.set(Calendar.YEAR,         utcCal.get(Calendar.YEAR))
                            localCal.set(Calendar.MONTH,        utcCal.get(Calendar.MONTH))
                            localCal.set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                            localCal.set(Calendar.HOUR_OF_DAY,  12)
                            localCal.set(Calendar.MINUTE,       0)
                            localCal.set(Calendar.SECOND,       0)
                            localCal.set(Calendar.MILLISECOND,  0)

                            selectedTimestamp = localCal.timeInMillis
                            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            dateText = formatter.format(Date(selectedTimestamp))
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SelectableField(
    value: String,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = LocalContentColor.current,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
