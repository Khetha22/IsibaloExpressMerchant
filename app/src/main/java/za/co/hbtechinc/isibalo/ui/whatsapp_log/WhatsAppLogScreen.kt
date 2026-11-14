package za.co.hbtechinc.isibalo.ui.whatsapp_log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.WhatsAppShareLog
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WhatsAppLogScreen(vm: WhatsAppLogViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val logs by vm.logs.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("WhatsApp Share Activity & Monitoring", color = cs.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                    Text("Total Shares Dispatched: ${logs.size}", color = cs.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium)
                    Text("Audit log tracking every receipt, invoice, and report sent via WhatsApp", color = cs.onPrimaryContainer.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (logs.isNotEmpty()) {
            item {
                SquareButton("CLEAR AUDIT LOG", { vm.clearLogs() }, Modifier.fillMaxWidth(), outlined = true)
            }
        }

        if (logs.isEmpty()) {
            item {
                Text("No WhatsApp shares logged yet. Share a receipt or report to track engagement.", color = cs.onSurfaceVariant)
            }
        }

        items(logs, key = { it.id }) { log ->
            WhatsAppLogItem(log = log)
        }
    }
}

@Composable
fun WhatsAppLogItem(log: WhatsAppShareLog) {
    val cs = MaterialTheme.colorScheme
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.ROOT).format(Date(log.sharedAt))

    SquareCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    androidx.compose.material3.Icon(Icons.Filled.Share, contentDescription = null, tint = cs.primary, modifier = Modifier.padding(bottom = 2.dp))
                    Text(log.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text(log.summary, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                Text("Dispatched: $dateStr", style = MaterialTheme.typography.labelSmall, color = cs.primary)
            }
        }
    }
}
