package za.co.hbtechinc.isibalo.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.Staff
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.util.rands
import java.util.Locale

@Composable
fun StaffScreen(vm: StaffViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val staffList by vm.staffList.collectAsStateWithLifecycle()
    var editingStaff by remember { mutableStateOf<Staff?>(null) }
    var adding by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    val totalDailyPayroll = staffList.filter { it.active }.sumOf { it.dailyWageCents }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Staff & Shift Payroll Management", color = cs.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                    Text("Daily Payroll: ${totalDailyPayroll.rands()}", color = cs.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium)
                    Text("Active Staff: ${staffList.count { it.active }} cashiers & floor workers", color = cs.onPrimaryContainer.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            SquareButton("ADD STAFF MEMBER", { adding = true }, Modifier.fillMaxWidth())
        }

        if (staffList.isEmpty()) {
            item {
                Text("No staff members added yet. Add cashiers to track shifts and daily payroll.", color = cs.onSurfaceVariant)
            }
        }

        items(staffList, key = { it.id }) { staff ->
            StaffItem(staff = staff, onEdit = { editingStaff = staff }, onDelete = { vm.deleteStaff(staff) })
        }
    }

    if (adding || editingStaff != null) {
        StaffDialog(
            initial = editingStaff,
            onDismiss = { adding = false; editingStaff = null },
            onSave = { name, role, wage, phone ->
                vm.saveStaff(name, role, wage, phone, editingStaff)
                adding = false
                editingStaff = null
            }
        )
    }
}

@Composable
fun StaffItem(staff: Staff, onEdit: () -> Unit, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme

    SquareCard(Modifier.fillMaxWidth(), onClick = onEdit) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(staff.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Role: ${staff.role}  •  Phone: ${staff.phone.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Text("Daily Wage: ${staff.dailyWageCents.rands()}", fontWeight = FontWeight.Bold, color = cs.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Delete Staff", tint = cs.error)
            }
        }
    }
}

@Composable
private fun StaffDialog(
    initial: Staff?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    fun cents(c: Long) = if (c == 0L) "" else String.format(Locale.ROOT, "%.2f", c / 100.0)
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var role by remember { mutableStateOf(initial?.role ?: "Cashier") }
    var wage by remember { mutableStateOf(cents(initial?.dailyWageCents ?: 0)) }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f).padding(12.dp),
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text(if (initial == null) "Add Staff Member" else "Edit Staff Member", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it }, label = { Text("Full Name") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = role, onValueChange = { role = it }, label = { Text("Role (e.g. Cashier, Supervisor)") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wage, onValueChange = { wage = it }, label = { Text("Daily Wage (R)") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SquareButton("CANCEL", onDismiss, Modifier.weight(1f), outlined = true)
                    SquareButton("SAVE STAFF", { if (name.isNotBlank() && wage.isNotBlank()) onSave(name, role, wage, phone) }, Modifier.weight(1f))
                }
            }
        }
    }
}
