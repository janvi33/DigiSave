package com.simple.digisave.domain.grouping

import com.simple.digisave.ui.dashboard.model.TransactionUi
import java.text.SimpleDateFormat
import java.util.*

enum class GroupOption {
    NONE,
    DAY,
    WEEK,
    MONTH
}

// Sealed class representing grouped list items
sealed class GroupItem {
    data class Header(val label: String) : GroupItem()
    data class Item(val tx: TransactionUi) : GroupItem()
}

fun groupTransactions(
    list: List<TransactionUi>,
    option: GroupOption
): List<GroupItem> {

    if (option == GroupOption.NONE) {
        return list.map { GroupItem.Item(it) }
    }

    val output = mutableListOf<GroupItem>()

    val dayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    var lastHeader: String? = null

    list.forEach { tx ->
        val date = Date(tx.timestamp)

        val header: String = when (option) {
            GroupOption.DAY -> dayFormat.format(date)

            GroupOption.WEEK -> {
                val weekStart = getStartOfWeek(tx.timestamp)
                "Week of ${dayFormat.format(Date(weekStart))}"
            }

            GroupOption.MONTH -> monthFormat.format(date)

            else -> ""
        }

        if (header != lastHeader) {
            output.add(GroupItem.Header(header))
            lastHeader = header
        }

        output.add(GroupItem.Item(tx))
    }

    return output
}

private fun getStartOfWeek(timestamp: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    return cal.timeInMillis
}
