package com.example

import com.example.data.entity.Sale
import com.example.data.entity.SaleItem
import com.example.data.entity.StoreSettings
import com.example.util.InvoiceFormattingService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PosCalculationUnitTest {

    @Test
    fun testCartItemSubtotalCalculation() {
        val unitPrice = 150.0
        val quantity = 3
        val discount = 50.0
        val taxRatePercent = 5.0 // 5%

        val rawTotal = unitPrice * quantity
        val afterDiscount = rawTotal - discount
        val taxAmount = afterDiscount * (taxRatePercent / 100.0)
        val finalTotal = afterDiscount + taxAmount

        assertEquals(450.0, rawTotal, 0.001)
        assertEquals(400.0, afterDiscount, 0.001)
        assertEquals(20.0, taxAmount, 0.001)
        assertEquals(420.0, finalTotal, 0.001)
    }

    @Test
    fun testInventoryStockDeduction() {
        val initialStock = 25
        val soldQuantity = 5
        val remainingStock = initialStock - soldQuantity

        assertEquals(20, remainingStock)
    }

    @Test
    fun testInvoiceFormattingServiceSaleTransaction() {
        val settings = StoreSettings(
            storeName = "Super Store",
            ownerName = "John Doe",
            phone = "1234567890",
            address = "123 Main St",
            currencySymbol = "$"
        )

        val sale = Sale(
            id = 1001L,
            invoiceNumber = "INV-1001",
            customerName = "Jane Smith",
            totalAmount = 200.0,
            discount = 20.0,
            netAmount = 180.0,
            paidAmount = 200.0,
            dueAmount = 0.0,
            paymentType = "Cash"
        )

        val items = listOf(
            SaleItem(
                id = 1L,
                saleId = 1001L,
                productId = 5L,
                productName = "Product A",
                quantity = 2.0,
                unit = "Pcs",
                purchasePrice = 50.0,
                salePrice = 100.0,
                totalPrice = 200.0
            )
        )

        val printableInvoice = InvoiceFormattingService.formatSaleTransaction(
            sale = sale,
            items = items,
            settings = settings
        )

        assertNotNull(printableInvoice)
        assertEquals("Super Store", printableInvoice.header.storeName)
        assertEquals("Jane Smith", printableInvoice.customer.name)
        assertEquals(1, printableInvoice.items.size)
        assertEquals("Product A", printableInvoice.items[0].productName)
        assertEquals(200.0, printableInvoice.totals.subtotal, 0.001)
        assertEquals(20.0, printableInvoice.totals.discount, 0.001)
        assertEquals(180.0, printableInvoice.totals.netAmount, 0.001)
    }
}

