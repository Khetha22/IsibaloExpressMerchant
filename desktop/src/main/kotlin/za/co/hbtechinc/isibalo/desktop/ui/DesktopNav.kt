package za.co.hbtechinc.isibalo.desktop.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class DesktopNav(val title: String, val icon: ImageVector) {
    Dashboard("Owner Dashboard", Icons.Rounded.Dashboard),
    POS("Point of Sale (POS)", Icons.Rounded.Storefront),
    Stock("Stock & Inventory", Icons.Rounded.Inventory2),
    Sales("Sales Analytics", Icons.Rounded.Payments),
    CashUp("Cash-Up Reconciliation", Icons.Rounded.AccountBalanceWallet),
    Expenses("Business Expenses", Icons.Rounded.ReceiptLong),
    Credit("Customer Credit Tabs", Icons.Rounded.People),
    Staff("Staff & Shift Payroll", Icons.Rounded.Groups),
    Reports("Financial Reports", Icons.Rounded.Assessment),
    Assistant("AI Business Assistant", Icons.Rounded.AutoAwesome)
}
