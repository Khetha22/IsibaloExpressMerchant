package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import za.co.hbtechinc.isibalo.desktop.data.DesktopRepository
import za.co.hbtechinc.isibalo.desktop.data.Staff
import za.co.hbtechinc.isibalo.desktop.ui.*
import za.co.hbtechinc.isibalo.desktop.util.rands
import za.co.hbtechinc.isibalo.desktop.util.toCents

@Composable
fun StaffDesktopScreen(repository: DesktopRepository) {
    val staffList by repository.staff.collectAsState()
    var isAdding by remember { mutableStateOf(false) }
    var editingStaff by remember { mutableStateOf<Staff?>(null) }

    val activeStaff = staffList.filter { it.active }
    val totalDailyPayroll = activeStaff.sumOf { it.dailyWageCents }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LightBlueContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Staff Roster & Shift Payroll", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                    Text("Total Active Daily Payroll: ${totalDailyPayroll.rands()}", style = MaterialTheme.typography.bodyMedium, color = LightBlueSecondary)
                }

                Button(
                    onClick = { isAdding = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Staff")
                    Spacer(Modifier.width(6.dp))
                    Text("Add Staff Member")
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(staffList, key = { it.id }) { staff ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, LightBlueContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(staff.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                                Surface(shape = RoundedCornerShape(8.dp), color = LightBlueContainer) {
                                    Text(staff.role, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = LightBluePrimary)
                                }
                            }
                            if (staff.phone.isNotBlank()) Text("Phone: ${staff.phone}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                            Text("Daily Wage: ${staff.dailyWageCents.rands()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Switch(
                                checked = staff.active,
                                onCheckedChange = { active -> repository.saveStaff(staff.copy(active = active)) }
                            )

                            IconButton(onClick = { editingStaff = staff }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit Staff", tint = LightBluePrimary)
                            }

                            IconButton(onClick = { repository.deleteStaff(staff) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAdding || editingStaff != null) {
        val target = editingStaff
        var nameText by remember { mutableStateOf(target?.name ?: "") }
        var roleText by remember { mutableStateOf(target?.role ?: "Cashier") }
        var wageText by remember { mutableStateOf(if (target != null) String.format(java.util.Locale.US, "%.2f", target.dailyWageCents / 100.0) else "") }
        var phoneText by remember { mutableStateOf(target?.phone ?: "") }

        Dialog(onDismissRequest = { isAdding = false; editingStaff = null }) {
            Card(modifier = Modifier.width(400.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(if (target == null) "Add Staff Member" else "Edit Staff Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

                    SquareField(value = nameText, onValueChange = { nameText = it }, label = "Staff Full Name")
                    SquareField(value = roleText, onValueChange = { roleText = it }, label = "Role / Position (e.g., Cashier, Manager)")
                    SquareField(value = wageText, onValueChange = { wageText = it }, label = "Daily Wage (Rands)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    SquareField(value = phoneText, onValueChange = { phoneText = it }, label = "Phone Number", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isAdding = false; editingStaff = null }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val cents = wageText.toCents() ?: 0L
                                if (nameText.isNotBlank()) {
                                    val staff = target?.copy(name = nameText, role = roleText, dailyWageCents = cents, phone = phoneText)
                                        ?: Staff(name = nameText, role = roleText, dailyWageCents = cents, phone = phoneText)
                                    repository.saveStaff(staff)
                                    isAdding = false
                                    editingStaff = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                        ) {
                            Text("Save Staff")
                        }
                    }
                }
            }
        }
    }
}
