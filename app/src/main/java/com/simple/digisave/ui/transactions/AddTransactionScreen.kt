package com.simple.digisave.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    // ── Form state ───────────────────────────────────────────────
    var title          by rememberSaveable { mutableStateOf("") }
    var amountText     by rememberSaveable { mutableStateOf("") }
    var isIncome       by rememberSaveable { mutableStateOf(true) }
    var selectedCategory by rememberSaveable { mutableStateOf<CategoryWithTotal?>(null) }
    var note           by rememberSaveable { mutableStateOf("") }
    var dateText       by rememberSaveable { mutableStateOf("Select Date") }
    var selectedTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    LaunchedEffect(preselectedType) {
        isIncome = preselectedType != "expense"
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

    // ── Screen ───────────────────────────────────────────────────
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
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        title.isBlank() -> scope.launch {
                            snackbarHostState.showSnackbar("Enter a title")
                        }
                        amount == null || amount <= 0 -> scope.launch {
                            snackbarHostState.showSnackbar("Enter a valid amount greater than 0")
                        }
                        selectedCategory == null -> scope.launch {
                            snackbarHostState.showSnackbar("Pick a category")
                        }
                        userId.isBlank() -> scope.launch {
                            snackbarHostState.showSnackbar("User not logged in")
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) IncomeText else ExpenseText
                )
            ) {
                Icon(Icons.Default.Save, null, tint = androidx.compose.ui.graphics.Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isIncome) "Save Income" else "Save Expense",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Income / Expense toggle ──────────────────────────
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = isIncome,
                    onClick = { if (!isIncome) { isIncome = true; selectedCategory = null } },
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = IncomeText.copy(alpha = 0.12f),
                        activeContentColor = IncomeText,
                        activeBorderColor = IncomeText
                    )
                ) {
                    Text("Income", fontWeight = FontWeight.SemiBold)
                }
                SegmentedButton(
                    selected = !isIncome,
                    onClick = { if (isIncome) { isIncome = false; selectedCategory = null } },
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = ExpenseText.copy(alpha = 0.10f),
                        activeContentColor = ExpenseText,
                        activeBorderColor = ExpenseText
                    )
                ) {
                    Text("Expense", fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Amount ───────────────────────────────────────────
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

            // ── Title ────────────────────────────────────────────
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Payee / Title") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // ── Category ─────────────────────────────────────────
            SelectableField(
                value = selectedCategory?.name ?: "Choose Category",
                label = "Category",
                leadingIcon = { Icon(Icons.Default.Category, null) },
                onClick = { navController.navigate("categories?type=${if (isIncome) "income" else "expense"}") }
            )

            // ── Date ─────────────────────────────────────────────
            SelectableField(
                value = dateText,
                label = "Date",
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                onClick = { showDatePicker = true }
            )

            // ── Note ─────────────────────────────────────────────
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))
        }
    }

    // ── Date picker ──────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
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
