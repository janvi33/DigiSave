package com.simple.digisave.ui.transactions

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.simple.digisave.ui.components.DigiSaveTopBar
import com.simple.digisave.ui.dashboard.DashboardViewModel
import com.simple.digisave.data.repository.CategoryWithTotal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    // ✅ Use rememberSaveable so fields persist after returning from CategoriesScreen
    var title by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var isIncome by rememberSaveable { mutableStateOf(true) }
    var selectedCategory by rememberSaveable { mutableStateOf<CategoryWithTotal?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var note by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf("Select Date") }
    var selectedTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    val datePickerState = rememberDatePickerState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 🔑 Listen for category returned from CategoriesScreen
    val backStackEntry by navController.currentBackStackEntryAsState()
    val savedStateHandle = backStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.get<CategoryWithTotal>("selectedCategory")?.let { category ->
            selectedCategory = category
            savedStateHandle.remove<CategoryWithTotal>("selectedCategory")
        }
    }

    Scaffold(
        topBar = {
            DigiSaveTopBar("Add Transaction", showBackButton = true, navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Redesigned Save button
            FilledTonalButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        title.isBlank() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("⚠️ Please enter a title")
                            }
                        }
                        amount == null -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("⚠️ Enter a valid amount")
                            }
                        }
                        selectedCategory == null -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("⚠️ Please choose a category")
                            }
                        }
                        userId == null -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("⚠️ User not logged in")
                            }
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
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Save Transaction", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Income / Expense Toggle
            IncomeExpenseToggle(isIncome = isIncome, onToggle = { isIncome = it })

            // Amount field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                ),
                placeholder = {
                    Text("0.00", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    )
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Payee / Title row
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Payee / Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            // Category row
            SelectableField(
                value = selectedCategory?.let { "${it.icon} ${it.name}" } ?: "Choose Category",
                label = "Category",
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                onClick = { navController.navigate("categories") }
            )

            // Date row
            SelectableField(
                value = dateText,
                label = "Date",
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                onClick = { showDatePicker = true }
            )

            // Note row
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { utcMillis ->

                            // 1️⃣ Convert selected UTC millis → Local calendar day
                            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            utcCalendar.timeInMillis = utcMillis

                            val year = utcCalendar.get(Calendar.YEAR)
                            val month = utcCalendar.get(Calendar.MONTH)
                            val day = utcCalendar.get(Calendar.DAY_OF_MONTH)

                            // 2️⃣ Rebuild this date in LOCAL timezone at NOON (prevents rollback)
                            val localCal = Calendar.getInstance() // uses device local timezone
                            localCal.set(Calendar.YEAR, year)
                            localCal.set(Calendar.MONTH, month)
                            localCal.set(Calendar.DAY_OF_MONTH, day)

                            // normalize the time to noon
                            localCal.set(Calendar.HOUR_OF_DAY, 12)
                            localCal.set(Calendar.MINUTE, 0)
                            localCal.set(Calendar.SECOND, 0)
                            localCal.set(Calendar.MILLISECOND, 0)

                            val finalTimestamp = localCal.timeInMillis

                            // 3️⃣ Show date correctly in UI
                            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            dateText = formatter.format(Date(finalTimestamp))

                            // 4️⃣ Save corrected timestamp
                            selectedTimestamp = finalTimestamp
                        }


                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun IncomeExpenseToggle(
    isIncome: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expense
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(25.dp))
                .background(
                    if (!isIncome) MaterialTheme.colorScheme.error
                    else Color.Transparent
                )
                .clickable { onToggle(false) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Expense",
                color = if (!isIncome) Color.White else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        // Income
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(25.dp))
                .background(
                    if (isIncome) Color(0xFF2E7D32)
                    else Color.Transparent
                )
                .clickable { onToggle(true) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Income",
                color = if (isIncome) Color.White else Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
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
        label = { Text(label) },
        leadingIcon = leadingIcon,
        readOnly = true,
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = LocalContentColor.current,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}