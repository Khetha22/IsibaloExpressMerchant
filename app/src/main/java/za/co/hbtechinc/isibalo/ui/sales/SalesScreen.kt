package za.co.hbtechinc.isibalo.ui.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.PaymentMethod
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.util.rands
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesScreen(vm: SalesViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val sales by vm.sales.collectAsStateWithLifecycle()
    val totals by vm.totals.collectAsStateWithLifecycle()
    val pending by vm.pending.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val fmt = SimpleDateFormat("HH:mm", Locale.ROOT)
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search sales (method, amount)...") },
                singleLine = true,
                shape = RectangleShape
            )
        }
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primary) {
                Column(Modifier.padding(16.dp)) {
                    Text("Today", color = cs.onPrimary, style = MaterialTheme.typography.labelLarge)
                    Text(sales.sumOf { it.totalCents }.rands(), color = cs.onPrimary,
                        style = MaterialTheme.typography.headlineMedium)
                    Text("${sales.size} sales", color = cs.onPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { m ->
                    SquareCard(Modifier.weight(1f)) {
                        Column(Modifier.padding(10.dp)) {
                            Text(m.label, style = MaterialTheme.typography.labelSmall)
                            Text((totals.firstOrNull { it.method == m }?.total ?: 0L).rands(),
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (pending == 0) "All sales backed up" else "$pending sales waiting to sync",
                        Modifier.weight(1f), fontWeight = FontWeight.SemiBold
                    )
                    SquareButton("SYNC NOW", vm::syncNow, enabled = pending > 0)
                }
            }
        }
        if (sales.isEmpty()) {
            item { Text("No sales matching your search.", color = cs.onSurfaceVariant) }
        }
        items(sales, key = { it.id }) { s ->
            SquareCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.totalCents.rands(), fontWeight = FontWeight.Bold)
                        Text("${fmt.format(Date(s.createdAt))}  ${s.method.label}",
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text(if (s.synced) "Synced" else "Pending",
                        color = if (s.synced) cs.primary else cs.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
