package com.simple.digisave.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.digisave.domain.grouping.GroupOption
import com.simple.digisave.domain.sorting.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val isModified = selectedSort != SortOption.DATE_ADDED || selectedGroup != GroupOption.NONE

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding() + 24.dp
                )
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort & Group",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isModified) {
                    TextButton(
                        onClick = {
                            selectedSort = SortOption.DATE_ADDED
                            selectedGroup = GroupOption.NONE
                            onReset()
                        }
                    ) {
                        Text(
                            "Reset",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── SORT BY ──────────────────────────────────────
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = selectedSort == option,
                        onClick = { selectedSort = option },
                        label = { Text(option.displayLabel(), fontSize = 13.sp) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── GROUP BY ─────────────────────────────────────
            Text(
                text = "Group By",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GroupOption.entries.forEach { option ->
                    FilterChip(
                        selected = selectedGroup == option,
                        onClick = { selectedGroup = option },
                        label = { Text(option.displayLabel(), fontSize = 13.sp) }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── APPLY ─────────────────────────────────────────
            Button(
                onClick = {
                    onApply(selectedSort, selectedGroup)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

private fun SortOption.displayLabel(): String = when (this) {
    SortOption.NONE -> "None"
    SortOption.DATE_ADDED -> "Date Added"
    SortOption.DATE_TRANSACTION -> "Trans. Date"
    SortOption.CATEGORY -> "Category"
    SortOption.AMOUNT_ASC -> "Low → High"
    SortOption.AMOUNT_DESC -> "High → Low"
}

private fun GroupOption.displayLabel(): String = when (this) {
    GroupOption.NONE -> "None"
    GroupOption.DAY -> "Day"
    GroupOption.WEEK -> "Week"
    GroupOption.MONTH -> "Month"
}
