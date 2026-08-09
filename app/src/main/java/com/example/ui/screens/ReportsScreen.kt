package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.InvoiceReceiptDialog
import com.example.ui.components.KpiCard
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose600
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(viewModel: StoreViewModel) {
    val allSales by viewModel.allSales.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val lowStockItems by viewModel.lowStockProducts.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val showInvoiceDialog by viewModel.showInvoiceDialog.collectAsState()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsState()
    val lastCompletedSaleItems by viewModel.lastCompletedSaleItems.collectAsState()

    var activeReportTab by remember { mutableStateOf(0) } // 0: Overview & Profit, 1: Invoices, 2: Expenses, 3: Low Stock
    var showAddExpenseModal by remember { mutableStateOf(false) }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseCategory by remember { mutableStateOf("General") }
    var expenseNote by remember { mutableStateOf("") }
    var saleToDelete by remember { mutableStateOf<com.example.data.entity.Sale?>(null) }

    // Time periods
    val calToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val todayMs = calToday.timeInMillis

    val calMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val monthMs = calMonth.timeInMillis

    val todaySales = allSales.filter { it.timestamp >= todayMs }
    val monthSales = allSales.filter { it.timestamp >= monthMs }
    val monthExpenses = allExpenses.filter { it.timestamp >= monthMs }

    val todayTotalRevenue = todaySales.sumOf { it.netAmount }
    val monthTotalRevenue = monthSales.sumOf { it.netAmount }
    val monthTotalDiscounts = monthSales.sumOf { it.discount }
    val monthTotalExpenses = monthExpenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(selectedTabIndex = activeReportTab) {
            Tab(selected = activeReportTab == 0, onClick = { activeReportTab = 0 }, text = { Text("P&L Overview") })
            Tab(selected = activeReportTab == 1, onClick = { activeReportTab = 1 }, text = { Text("Invoices") })
            Tab(selected = activeReportTab == 2, onClick = { activeReportTab = 2 }, text = { Text("Expenses") })
            Tab(selected = activeReportTab == 3, onClick = { activeReportTab = 3 }, text = { Text("Low Stock") })
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeReportTab == 0) {
            // P&L OVERVIEW & STATS
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Daily & Monthly Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = "Today's Sales",
                            value = "${settings.currencySymbol} ${todayTotalRevenue.toInt()}",
                            subtitle = "${todaySales.size} Invoices today",
                            icon = Icons.Default.TrendingUp,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )

                        KpiCard(
                            title = "This Month Sales",
                            value = "${settings.currencySymbol} ${monthTotalRevenue.toInt()}",
                            subtitle = "${monthSales.size} Total transactions",
                            icon = Icons.Default.DateRange,
                            containerColor = Emerald100,
                            contentColor = Emerald600,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = Emerald600)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Monthly Sales & Discount Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gross Monthly Revenue:", style = MaterialTheme.typography.bodyMedium)
                                Text("${settings.currencySymbol} ${(monthTotalRevenue + monthTotalDiscounts).toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Discounts Given:", style = MaterialTheme.typography.bodyMedium, color = Rose600)
                                Text("- ${settings.currencySymbol} ${monthTotalDiscounts.toInt()}", style = MaterialTheme.typography.bodyMedium, color = Rose600)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monthly Store Expenses:", style = MaterialTheme.typography.bodyMedium, color = Rose600)
                                Text("- ${settings.currencySymbol} ${monthTotalExpenses.toInt()}", style = MaterialTheme.typography.bodyMedium, color = Rose600, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Collected Revenue:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${settings.currencySymbol} ${monthTotalRevenue.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Operating Profit:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                val netProfit = monthTotalRevenue - monthTotalExpenses
                                Text(
                                    "${settings.currencySymbol} ${netProfit.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (netProfit >= 0) Emerald600 else Rose600
                                )
                            }
                        }
                    }
                }
            }
        } else if (activeReportTab == 1) {
            // ALL INVOICES HISTORY
            if (allSales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No sales invoices generated yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allSales) { sale ->
                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(sale.timestamp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.viewInvoice(sale) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sale.invoiceNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${sale.customerName} • $dateStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${settings.currencySymbol} ${sale.netAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                        Text(sale.paymentType, style = MaterialTheme.typography.labelSmall, color = if (sale.dueAmount > 0) Rose600 else Emerald600)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(onClick = { saleToDelete = sale }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Invoice", tint = Color(0xFFDC2626))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeReportTab == 2) {
            // STORE EXPENSES TAB
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Store Expense Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Recorded Offline in Local SQLite", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Button(onClick = { showAddExpenseModal = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Expense")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (allExpenses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No expenses recorded yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allExpenses) { exp ->
                            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(exp.timestamp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exp.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${exp.category} • $dateStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        if (exp.note.isNotBlank()) {
                                            Text(exp.note, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "- ${settings.currencySymbol} ${exp.amount.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Rose600
                                        )
                                        IconButton(onClick = { viewModel.deleteExpense(exp) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Expense", tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // LOW STOCK REORDER REPORT
            if (lowStockItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("All product stock levels are healthy! No low stock alerts.", color = Emerald600, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Rose100, contentColor = Rose600)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("${lowStockItems.size} Items requiring stock reorder", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(lowStockItems) { p ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(p.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Category: ${p.category} • Min Threshold: ${p.minStockLevel.toInt()} ${p.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }

                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Rose100).padding(8.dp)
                                ) {
                                    Text("${p.stockQuantity.toInt()} ${p.unit} Left", color = Rose600, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseModal) {
        AlertDialog(
            onDismissRequest = { showAddExpenseModal = false },
            title = { Text("Record Store Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = expenseTitle,
                        onValueChange = { expenseTitle = it },
                        label = { Text("Expense Title (e.g. Shop Electricity)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Amount (${settings.currencySymbol})") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expenseCategory,
                        onValueChange = { expenseCategory = it },
                        label = { Text("Category (Utilities, Rent, Transport, Salary)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expenseNote,
                        onValueChange = { expenseNote = it },
                        label = { Text("Note / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expenseAmount.toDoubleOrNull() ?: 0.0
                        if (expenseTitle.isNotBlank() && amt > 0) {
                            viewModel.addExpense(expenseTitle.trim(), expenseCategory.trim(), amt, expenseNote.trim())
                            showAddExpenseModal = false
                            expenseTitle = ""
                            expenseAmount = ""
                            expenseNote = ""
                        } else {
                            viewModel.showToast("Please enter valid expense title and amount!")
                        }
                    }
                ) {
                    Text("Save Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (saleToDelete != null) {
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("Delete Invoice", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this invoice?") },
            confirmButton = {
                Button(
                    onClick = {
                        val s = saleToDelete
                        saleToDelete = null
                        if (s != null) {
                            viewModel.deleteInvoice(s)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInvoiceDialog && lastCompletedSale != null) {
        InvoiceReceiptDialog(
            sale = lastCompletedSale!!,
            items = lastCompletedSaleItems,
            settings = settings,
            onDismiss = { viewModel.dismissInvoiceDialog() },
            onDeleteInvoice = { viewModel.deleteInvoice(it) }
        )
    }
}
