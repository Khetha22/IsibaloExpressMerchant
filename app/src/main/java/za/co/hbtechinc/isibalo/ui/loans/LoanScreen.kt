package za.co.hbtechinc.isibalo.ui.loans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.MerchantLoan
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.util.rands
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoanScreen(vm: LoanViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val loans by vm.loans.collectAsStateWithLifecycle()
    var applying by remember { mutableStateOf(false) }
    var repayingLoan by remember { mutableStateOf<MerchantLoan?>(null) }
    val cs = MaterialTheme.colorScheme

    val activeDebt = loans.filter { it.status == "Active" }.sumOf { it.remainingCents }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primary) {
                Column(Modifier.padding(16.dp)) {
                    Text("Working Capital & Cash Advances", color = cs.onPrimary, style = MaterialTheme.typography.labelLarge)
                    Text(activeDebt.rands(), color = cs.onPrimary, style = MaterialTheme.typography.headlineMedium)
                    Text("Pre-approved based on your daily POS sales history", color = cs.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            SquareButton("APPLY FOR WORKING CAPITAL ADVANCE", { applying = true }, Modifier.fillMaxWidth())
        }
        if (loans.isEmpty()) {
            item {
                Text("No cash advances requested yet. Grow your stock with instant working capital.", color = cs.onSurfaceVariant)
            }
        }
        items(loans, key = { it.id }) { loan ->
            LoanItem(loan = loan, onRepay = { repayingLoan = loan })
        }
    }

    if (applying) {
        ApplyLoanDialog(
            onDismiss = { applying = false },
            onApply = { amt ->
                vm.applyLoan(amt)
                applying = false
            }
        )
    }

    repayingLoan?.let { loan ->
        RepayLoanDialog(
            loan = loan,
            onDismiss = { repayingLoan = null },
            onRepay = { amt ->
                vm.makeRepayment(loan.id, amt)
                repayingLoan = null
            }
        )
    }
}

@Composable
fun LoanItem(loan: MerchantLoan, onRepay: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ROOT).format(Date(loan.createdAt))

    SquareCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Advance: ${loan.principalCents.rands()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Fee (10%): ${loan.feeCents.rands()}  |  Total: ${loan.totalRepayCents.rands()}", style = MaterialTheme.typography.labelSmall)
                Text("Date: $dateStr", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                Text(
                    text = if (loan.status == "Repaid") "REPAID ✅" else "REMAINING: ${loan.remainingCents.rands()}",
                    fontWeight = FontWeight.Bold,
                    color = if (loan.status == "Repaid") cs.primary else cs.error
                )
            }
            if (loan.status == "Active") {
                IconButton(onClick = onRepay) {
                    Icon(Icons.Filled.Payment, contentDescription = "Make Repayment", tint = cs.primary)
                }
            }
        }
    }
}

@Composable
private fun ApplyLoanDialog(onDismiss: () -> Unit, onApply: (String) -> Unit) {
    var amount by remember { mutableStateOf("1000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = { Text("Request Merchant Cash Advance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Instantly unlock working capital to restock fast-moving inventory based on your offline POS sales data. Flat 10% service fee applies.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it }, label = { Text("Requested Amount (R)") },
                    singleLine = true, shape = RectangleShape, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            SquareButton("APPLY & DISBURSE", {
                if (amount.isNotBlank()) {
                    onApply(amount)
                }
            })
        },
        dismissButton = { SquareButton("CANCEL", onDismiss, outlined = true) }
    )
}

@Composable
private fun RepayLoanDialog(loan: MerchantLoan, onDismiss: () -> Unit, onRepay: (String) -> Unit) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = { Text("Make Loan Repayment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Remaining Balance: ${loan.remainingCents.rands()}", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it }, label = { Text("Repayment Amount (R)") },
                    singleLine = true, shape = RectangleShape, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            SquareButton("SUBMIT PAYMENT", {
                if (amount.isNotBlank()) {
                    onRepay(amount)
                }
            })
        },
        dismissButton = { SquareButton("CANCEL", onDismiss, outlined = true) }
    )
}
