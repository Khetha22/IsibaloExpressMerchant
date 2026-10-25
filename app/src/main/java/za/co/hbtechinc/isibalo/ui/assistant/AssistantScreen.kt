package za.co.hbtechinc.isibalo.ui.assistant

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.ui.components.SquareChoice

private val prompts = listOf("Summarise today", "What should I restock?", "How can I sell more?")

@Composable
fun AssistantScreen(vm: AssistantViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val msgs by vm.msgs.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val ready by vm.modelReady.collectAsStateWithLifecycle()
    val importing by vm.importing.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val cs = MaterialTheme.colorScheme

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::importModel)
    }
    LaunchedEffect(msgs.size, busy) { listState.animateScrollToItem(msgs.size) }

    Column(Modifier.fillMaxSize()) {
        if (!ready) {
            SquareCard(Modifier.fillMaxWidth().padding(12.dp), color = cs.primaryContainer) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Install the offline AI model", fontWeight = FontWeight.Bold, color = cs.onPrimaryContainer)
                    Text(
                        "Download Gemma 3 1B (int4, .task) once, then import it. After that the assistant works with no data or Wi-Fi.",
                        color = cs.onPrimaryContainer, style = MaterialTheme.typography.bodyMedium
                    )
                    SquareButton(
                        if (importing) "IMPORTING..." else "IMPORT MODEL",
                        { picker.launch(arrayOf("*/*")) }, Modifier.fillMaxWidth(), enabled = !importing
                    )
                }
            }
        }

        LazyColumn(
            Modifier.weight(1f), state = listState,
            contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(msgs) { m ->
                Box(Modifier.fillMaxWidth(), contentAlignment = if (m.fromUser) Alignment.CenterEnd else Alignment.CenterStart) {
                    SquareCard(
                        Modifier.fillMaxWidth(0.85f),
                        color = if (m.fromUser) cs.primary else cs.surfaceVariant
                    ) {
                        Text(m.text, Modifier.padding(12.dp),
                            color = if (m.fromUser) cs.onPrimary else cs.onSurface)
                    }
                }
            }
            if (busy) item { Text("Thinking...", color = cs.onSurfaceVariant) }
        }

        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(prompts) { p -> SquareChoice(p, false, { vm.ask(p) }, Modifier.padding(vertical = 4.dp).padding(horizontal = 0.dp)) }
        }

        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about your shop") }, singleLine = true, shape = RectangleShape
            )
            SquareButton("ASK", { vm.ask(input); input = "" }, enabled = !busy && input.isNotBlank())
        }
    }
}
