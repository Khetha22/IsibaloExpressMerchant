package za.co.hbtechinc.isibalo.ui.media

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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.ui.components.SquareCard

@Composable
fun MediaSearchScreen(vm: MediaSearchViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val mediaList by vm.searchResults.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Instant Media & Video Moment Finder", color = cs.onPrimaryContainer, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Search local product photos and security video moments using plain text queries. Powered 100% locally by MediaPipe on-device embeddings.",
                        color = cs.onPrimaryContainer.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = vm::searchMedia,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search photos or video clips (e.g. 'bread', 'cashier opening till')...") },
                singleLine = true,
                shape = RectangleShape
            )
        }

        item {
            Text("Store Media Index (${mediaList.size} items)", style = MaterialTheme.typography.titleMedium)
        }

        if (mediaList.isEmpty()) {
            item {
                Text("No matching photos or video moments found.", color = cs.onSurfaceVariant)
            }
        }

        items(mediaList, key = { it.id }) { item ->
            SquareCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (item.type == "PHOTO") Icons.Filled.Image else Icons.Filled.Videocam,
                        contentDescription = item.type,
                        tint = cs.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(item.description, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        Text("${item.type}  •  ${item.timestamp}", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
