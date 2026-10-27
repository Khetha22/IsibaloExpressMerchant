package za.co.hbtechinc.isibalo.ui.ai_setup

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.ai.AiModelType
import za.co.hbtechinc.isibalo.data.KnowledgeDoc
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.ui.components.SquareChoice

@Composable
fun AiSetupScreen(vm: AiSetupViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val activeModel by vm.selectedModel.collectAsStateWithLifecycle()
    val isReady by vm.isModelReady.collectAsStateWithLifecycle()
    val importing by vm.isImporting.collectAsStateWithLifecycle()
    val docs by vm.knowledgeDocs.collectAsStateWithLifecycle()
    val ragQuery by vm.ragQuery.collectAsStateWithLifecycle()
    val searchResults by vm.ragSearchResults.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    var docTitle by remember { mutableStateOf("") }
    var docCategory by remember { mutableStateOf("General") }
    var docContent by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::importModelFile)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("On-Device AI & Edge Gallery Hub", style = MaterialTheme.typography.titleLarge)
            Text(
                "Download MediaPipe LLMs and Gemma Embeddings from Edge Gallery for offline RAG.",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Active AI Engine", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiModelType.entries.forEach { model ->
                            SquareChoice(
                                text = if (model == AiModelType.MEDIAPIPE_GEMMA3_1B) "Gemma 3 1B" else "Phi-3 Mini",
                                selected = model == activeModel,
                                onClick = { vm.selectModelType(model) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(activeModel.description, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)

                    val downloadUrl = if (activeModel == AiModelType.MEDIAPIPE_GEMMA3_1B)
                        "https://huggingface.co/litert-community/Gemma3-1B-IT"
                    else
                        "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-onnx"

                    SquareButton(
                        text = "1. BROWSE EDGE GALLERY / HUGGING FACE",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        outlined = true
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isReady) "Status: Model Installed ✅" else "Status: Model Missing ❌",
                            fontWeight = FontWeight.Bold,
                            color = if (isReady) cs.primary else cs.error
                        )
                        SquareButton(
                            text = if (importing) "IMPORTING..." else "2. IMPORT .TASK FILE",
                            onClick = { picker.launch(arrayOf("*/*")) },
                            enabled = !importing
                        )
                    }
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.surfaceVariant) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edge Gallery & Gemma Embeddings Hub", fontWeight = FontWeight.Bold, color = cs.primary)
                    Text(
                        "Isibalo Express supports MediaPipe on-device inference and vector embeddings for high-accuracy local RAG without cloud latency or data costs.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    SquareButton(
                        text = "EXPLORE EDGE GALLERY MODELS",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://huggingface.co/litert-community"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        outlined = true
                    )
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Document to Local RAG Knowledge Base", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Title / Topic (e.g. Return Policy)") },
                        singleLine = true,
                        shape = RectangleShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = docCategory,
                        onValueChange = { docCategory = it },
                        label = { Text("Category (e.g. Policy, Supplier, FAQ)") },
                        singleLine = true,
                        shape = RectangleShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = docContent,
                        onValueChange = { docContent = it },
                        label = { Text("Content / Knowledge Snippet") },
                        shape = RectangleShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    SquareButton(
                        text = "ADD TO KNOWLEDGE BASE",
                        onClick = {
                            vm.addKnowledgeDoc(docTitle, docContent, docCategory)
                            docTitle = ""
                            docContent = ""
                        },
                        enabled = docTitle.isNotBlank() && docContent.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Test Vector Embedding Search (RAG)", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = ragQuery,
                        onValueChange = vm::searchRag,
                        label = { Text("Query Knowledge Base...") },
                        singleLine = true,
                        shape = RectangleShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (searchResults.isNotEmpty()) {
                        Text("Retrieved Context Snippets:", style = MaterialTheme.typography.labelLarge)
                        searchResults.forEach { res ->
                            SquareCard(Modifier.fillMaxWidth().padding(vertical = 2.dp), color = cs.surfaceVariant) {
                                Column(Modifier.padding(8.dp)) {
                                    Text("${res.doc.title} (${(res.score * 100).toInt()}% match)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(res.doc.content, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Knowledge Base Index (${docs.size} Documents)", style = MaterialTheme.typography.titleMedium)
        }

        items(docs) { doc ->
            KnowledgeDocItem(doc, onDelete = { vm.deleteKnowledgeDoc(doc) })
        }
    }
}

@Composable
fun KnowledgeDocItem(doc: KnowledgeDoc, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SquareCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("${doc.title} [${doc.category}]", fontWeight = FontWeight.Bold)
                Text(doc.content, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Document", tint = cs.error)
            }
        }
    }
}
