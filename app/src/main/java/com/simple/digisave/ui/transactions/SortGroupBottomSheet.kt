package com.simple.digisave.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.simple.digisave.domain.sorting.SortOption
import com.simple.digisave.domain.grouping.GroupOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortGroupBottomSheet(
    currentSort: SortOption,
    currentGroup: GroupOption,
    onApply: (SortOption, GroupOption) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSort by remember { mutableStateOf(currentSort) }
    var selectedGroup by remember { mutableStateOf(currentGroup) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {

        // ⭐ FIX 1: Make content scrollable
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                // ⭐ FIX 2: Add bottom inset so APPLY buttons are always visible
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding() + 24.dp
                )
        ) {

            // Title
            Text(
                text = "Sort & Group",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))


            // SORT SECTION
            Text(
                text = "Sort By",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            SortOption.values().forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedSort == option,
                        onClick = { selectedSort = option }
                    )
                    Text(option.displayLabel(), fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            // GROUP SECTION
            Text(
                text = "Group By",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            GroupOption.values().forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedGroup == option,
                        onClick = { selectedGroup = option }
                    )
                    Text(option.displayLabel(), fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // RESET + APPLY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onReset) {
                    Text(
                        text = "Reset",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        onApply(selectedSort, selectedGroup)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// Extension labels for UI formatting
private fun SortOption.displayLabel(): String =
    when (this) {
        SortOption.CATEGORY -> "Category"
        SortOption.AMOUNT_ASC -> "Amount (Low → High)"
        SortOption.AMOUNT_DESC -> "Amount (High → Low)"
        SortOption.DATE_ADDED -> "Date Added"
        SortOption.DATE_TRANSACTION -> "Transaction Date"
        SortOption.NONE -> "None"
    }

private fun GroupOption.displayLabel(): String =
    when (this) {
        GroupOption.NONE -> "None"
        GroupOption.DAY -> "Day"
        GroupOption.WEEK -> "Week"
        GroupOption.MONTH -> "Month"
    }
