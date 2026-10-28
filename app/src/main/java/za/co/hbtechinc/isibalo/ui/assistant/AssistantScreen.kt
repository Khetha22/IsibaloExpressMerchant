package za.co.hbtechinc.isibalo.ui.assistant

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.util.Locale

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

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            input = spokenText
            vm.ask(spokenText)
        }
    }

    LaunchedEffect(msgs.size, busy) { listState.animateScrollToItem(msgs.size) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().widthIn(max = 900.dp),
            state = listState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Assistant & Voice Prompt", fontWeight = FontWeight.Bold, color = cs.onBackground)
                    IconButton(onClick = vm::clearChat) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear Chat History", tint = cs.error)
                    }
                }
            }

            if (!ready) {
                item {
                    SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Install the offline AI model", fontWeight = FontWeight.Bold, color = cs.onPrimaryContainer)
                            Text(
                                "Download Gemma 3 1B (int4, .task) once, then import it. After that the assistant works with no data or Wi-Fi.",
                                color = cs.onPrimaryContainer, style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "💡 To download: Search 'litert-community/Gemma3-1B-IT' on Hugging Face for gemma3-1b-it-int4.task.",
                                color = cs.onPrimaryContainer.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall
                            )
                            SquareButton(
                                if (importing) "IMPORTING..." else "IMPORT MODEL .TASK",
                                { picker.launch(arrayOf("*/*")) }, Modifier.fillMaxWidth(), enabled = !importing
                            )
                        }
                    }
                }
            }

            item {
                Text("Conversation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(msgs, key = { it.text.hashCode() + it.fromUser.hashCode() }) { m ->
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

            if (busy) {
                item {
                    Text("Thinking...", color = cs.onSurfaceVariant)
                }
            }

            item {
                Text("Quick Prompts", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(prompts) { p -> SquareChoice(p, false, { vm.ask(p) }) }
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask or tap microphone...") }, singleLine = true, shape = RectangleShape
                    )
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                               putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your question to Isibalo...")
                            }
                            try {
                                speechRecognizerLauncher.launch(intent)
                            } catch (_: Exception) { }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice Input", tint = cs.primary)
                    }
                    SquareButton("ASK", { vm.ask(input); input = "" }, enabled = !busy && input.isNotBlank())
                }
            }
        }
    }
}
