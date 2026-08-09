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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Customer
import com.example.data.entity.Product
import com.example.ui.components.BarCodeScannerDialog
import com.example.ui.components.InvoiceReceiptDialog
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose600
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesPosScreen(viewModel: StoreViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val discountAmount by viewModel.discountAmount.collectAsState()
    val paidAmountInput by viewModel.paidAmountInput.collectAsState()
    val paymentType by viewModel.paymentType.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val showInvoiceDialog by viewModel.showInvoiceDialog.collectAsState()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsState()
    val lastCompletedSaleItems by viewModel.lastCompletedSaleItems.collectAsState()

    var showBarcodeScanner by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Add Products, 1: Cart Checkout
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    // Filter products for POS list
    val posProducts = allProducts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.barcode.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    val cartSubtotal = cart.sumOf { it.total }
    val cartNetTotal = (cartSubtotal - discountAmount).coerceAtLeast(0.0)
    val paidVal = paidAmountInput.toDoubleOrNull() ?: cartNetTotal
    val remainingDue = (cartNetTotal - paidVal).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Selector (Catalog vs Cart)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Items")
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cart (${cart.sumOf { it.quantity.toInt() }})")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // CATALOG / PRODUCT SELECTOR TAB
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search product name or barcode...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showBarcodeScanner = true },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Category Filter Chips
            val categories = listOf("All", "Hardware", "Sanitary", "Plumbing", "Paints", "Tools", "Cement", "Adhesives")
            var selectedCategory by remember { mutableStateOf("All") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(5).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            if (category == "All") {
                                viewModel.setSearchQuery("")
                            } else {
                                viewModel.setSearchQuery(category)
                            }
                        },
                        label = { Text(category, fontSize = 11.sp, fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Products Catalog List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(posProducts) { product ->
                    val inCartItem = cart.find { it.product.id == product.id }
                    val qtyInCart = inCartItem?.quantity?.toInt() ?: 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.addToCart(product, 1.0) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (qtyInCart > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${product.category} • Stock: ${product.stockQuantity.toInt()} ${product.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${settings.currencySymbol} ${product.salePrice.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Button(
                                    onClick = { viewModel.addToCart(product, 1.0) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (qtyInCart > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    if (qtyInCart > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("$qtyInCart")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Floating Bar to jump to Checkout
            if (cart.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${cart.sumOf { it.quantity.toInt() }} Items in Cart", fontWeight = FontWeight.Bold)
                        Text("Proceed to Checkout (${settings.currencySymbol} ${cartSubtotal.toInt()}) →", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        } else {
            // CART CHECKOUT TAB
            if (cart.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cart is empty", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Text("Tap 'Select Items' to add grocery products.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { selectedTab = 0 }) {
                            Text("Browse Items Catalog")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Selected Cart Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { viewModel.clearCart() }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Rose600, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Cart", color = Rose600)
                            }
                        }
                    }

                    // Cart Items List
                    items(cart) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(item.product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text("@ ${settings.currencySymbol} ${item.customPrice.toInt()} / ${item.product.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQty(item.product.id, item.quantity - 1) },
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                                    }

                                    Text(
                                        text = "${item.quantity.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.updateCartQty(item.product.id, item.quantity + 1) },
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = "${settings.currencySymbol} ${item.total.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Customer Selector & Payment Settings
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Customer & Payment Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Customer Dropdown
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                ExposedDropdownMenuBox(
                                    expanded = customerDropdownExpanded,
                                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = selectedCustomer?.let { "${it.name} (${if (it.balance > 0) "Udhaar: ${settings.currencySymbol} ${it.balance.toInt()}" else "No Due"})" } ?: "Cash Customer (Default)",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Select Customer") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = customerDropdownExpanded,
                                        onDismissRequest = { customerDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Cash Customer (Default)") },
                                            onClick = {
                                                viewModel.setSelectedCustomer(null)
                                                customerDropdownExpanded = false
                                            }
                                        )
                                        customers.forEach { cust ->
                                            DropdownMenuItem(
                                                text = { Text("${cust.name} ${if (cust.balance > 0) "- Udhaar: ${settings.currencySymbol} ${cust.balance.toInt()}" else ""}") },
                                                onClick = {
                                                    viewModel.setSelectedCustomer(cust)
                                                    customerDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { showAddCustomerDialog = true },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Customer", tint = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Payment Type Segmented Buttons
                            Text("Payment Mode", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            val paymentModes = listOf("Cash", "Udhaar / Credit", "Card / Online")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                paymentModes.forEach { mode ->
                                    FilterChip(
                                        selected = paymentType == mode,
                                        onClick = { viewModel.setPaymentType(mode) },
                                        label = { Text(mode) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Discount & Paid Inputs
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (discountAmount == 0.0) "" else discountAmount.toInt().toString(),
                                    onValueChange = { viewModel.setDiscountAmount(it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text("Discount (${settings.currencySymbol})") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                OutlinedTextField(
                                    value = paidAmountInput,
                                    onValueChange = { viewModel.setPaidAmountInput(it) },
                                    label = { Text("Paid Amount") },
                                    placeholder = { Text("${cartNetTotal.toInt()}") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Summary Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                                        Text("${settings.currencySymbol} ${cartSubtotal.toInt()}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (discountAmount > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Discount:", style = MaterialTheme.typography.bodyMedium, color = Rose600)
                                            Text("- ${settings.currencySymbol} ${discountAmount.toInt()}", style = MaterialTheme.typography.bodyMedium, color = Rose600)
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Net Payable Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${settings.currencySymbol} ${cartNetTotal.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (remainingDue > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Remaining Udhaar / Due:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Rose600)
                                            Text("${settings.currencySymbol} ${remainingDue.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Rose600)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Checkout Button
                            Button(
                                onClick = { viewModel.processSale() },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete Sale & Print Bill (${settings.currencySymbol} ${cartNetTotal.toInt()})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Barcode Scanner Dialog
    if (showBarcodeScanner) {
        BarCodeScannerDialog(
            productsList = allProducts,
            onBarcodeScanned = { barcode ->
                val p = allProducts.find { it.barcode == barcode }
                if (p != null) {
                    viewModel.addToCart(p, 1.0)
                    viewModel.showToast("Added '${p.name}' to cart!")
                } else {
                    viewModel.setSearchQuery(barcode)
                }
                showBarcodeScanner = false
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }

    // Add Customer Quick Modal
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onSave = { name, phone, address ->
                viewModel.saveCustomer(Customer(name = name, phone = phone, address = address))
                showAddCustomerDialog = false
            },
            onDismiss = { showAddCustomerDialog = false }
        )
    }

    // Invoice Receipt Dialog
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

@Composable
fun AddCustomerDialog(
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Customer") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address / Location") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onSave(name.trim(), phone.trim(), address.trim())
            }) { Text("Save Customer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
