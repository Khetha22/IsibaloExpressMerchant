package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import za.co.hbtechinc.isibalo.desktop.data.CustomerDebt
import za.co.hbtechinc.isibalo.desktop.data.DesktopRepository
import za.co.hbtechinc.isibalo.desktop.ui.*
import za.co.hbtechinc.isibalo.desktop.util.rands
import za.co.hbtechinc.isibalo.desktop.util.toCents

@Composable
fun CreditDesktopScreen(repository: DesktopRepository) {
    val debts by repository.customerDebts.collectAsState()
    var nameText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var reminderMsg by remember { mutableStateOf<String?>(null) }

    val openDebts = debts.filter { !it.settled }
    val settledDebts = debts.filter { it.settled }
    val totalOutstandingCents = openDebts.sumOf { it.amountCents }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left Column: Add Customer Credit Tab
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LightBlueContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Customer Credit Tab (Isikwelete)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

                SquareField(value = nameText, onValueChange = { nameText = it }, label = "Customer Full Name")
                SquareField(value = phoneText, onValueChange = { phoneText = it }, label = "Phone / WhatsApp Number", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                SquareField(value = amountText, onValueChange = { amountText = it }, label = "Credit Amount (Rands)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                SquareField(value = noteText, onValueChange = { noteText = it }, label = "Items Taken / Notes")

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        val cents = amountText.toCents()
                        if (nameText.isNotBlank() && cents != null && cents > 0) {
                            repository.saveDebt(CustomerDebt(customerName = nameText, phone = phoneText, amountCents = cents, note = noteText))
                            nameText = ""
                            phoneText = ""
                            amountText = ""
                            noteText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                ) {
                    Text("Add Credit Tab", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Right Column: Open & Settled Tabs List
        Column(modifier = Modifier.weight(1.2f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightBlueContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Total Outstanding Customer Debt", fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                        Text("${openDebts.size} Active Credit Tabs", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                    }
                    Text(totalOutstandingCents.rands(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = LightBluePrimary)
                }
            }

            Text("Credit Tab Book", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(debts, key = { it.id }) { debt ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (debt.settled) LightBlueContainer else ErrorContainerLight)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(debt.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                                    Surface(shape = RoundedCornerShape(8.dp), color = if (debt.settled) LightBlueContainer else ErrorContainerLight) {
                                        Text(if (debt.settled) "Settled" else "Unpaid", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if (debt.settled) LightBluePrimary else OnErrorContainerLight)
                                    }
                                }
                                if (debt.phone.isNotBlank()) Text("Phone: ${debt.phone}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                                if (debt.note.isNotBlank()) Text("Note: ${debt.note}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(debt.amountCents.rands(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = if (debt.settled) LightBluePrimary else MaterialTheme.colorScheme.error)

                                if (!debt.settled) {
                                    Button(
                                        onClick = { repository.settleDebt(debt.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Settle")
                                        Spacer(Modifier.width(4.dp))
                                        Text("Settle Tab")
                                    }

                                    IconButton(onClick = {
                                        val txt = "Hello ${debt.customerName}, friendly reminder from store: Outstanding tab amount of ${debt.amountCents.rands()} is due."
                                        repository.logWhatsAppShare("Tab Reminder: ${debt.customerName}", txt)
                                        reminderMsg = txt
                                    }) {
                                        Icon(Icons.Rounded.Share, contentDescription = "WhatsApp Reminder", tint = LightBluePrimary)
                                    }
                                }

                                IconButton(onClick = { repository.deleteDebt(debt) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    reminderMsg?.let { txt ->
        AlertDialog(
            onDismissRequest = { reminderMsg = null },
            confirmButton = { TextButton(onClick = { reminderMsg = null }) { Text("Close") } },
            title = { Text("WhatsApp Tab Reminder") },
            text = { Text("WhatsApp message reminder logged:\n\n$txt") }
        )
    }
}
