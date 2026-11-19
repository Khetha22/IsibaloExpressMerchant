package za.co.hbtechinc.isibalo.ui.ai_setup

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.ai.AiModelType
import za.co.hbtechinc.isibalo.data.KnowledgeDoc
import za.co.hbtechinc.isibalo.ui.components.SquareField
import kotlin.collections.isNotEmpty

// Consistent Light Blue palette matching Assistant, Dashboard, Jobs, Prices, Reports, and Settings screens
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@Composable
fun AiSetupScreen(vm: AiSetupViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val activeModel by vm.selectedModel.collectAsStateWithLifecycle()
    val isReady by vm.isModelReady.collectAsStateWithLifecycle()
    val importing by vm.isImporting.collectAsStateWithLifecycle()
    val docs by vm.knowledgeDocs.collectAsStateWithLifecycle()
    val ragQuery by vm.ragQuery.collectAsStateWithLifecycle()
    val searchResults by vm.ragSearchResults.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Detect orientation for adaptive landscape/portrait split layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var docTitle by remember { mutableStateOf("") }
    var docCategory by remember { mutableStateOf("General") }
    var docContent by remember { mutableStateOf("") }
    var showAddDocDialog by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::importModelFile)
    }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::importPdfDocument)
    }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::importVideoDocument)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBlueSurface
    ) {
        if (isLandscape) {
            // Adaptive Two-Column Split for Landscape Mode
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: AI Model Selection & Edge Gallery Hub
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AiHeaderCard()
                    ModelSelectorCard(
                        activeModel = activeModel,
                        isReady = isReady,
                        importing = importing,
                        onSelectModel = vm::selectModelType,
                        onBrowse = {
                            val url = if (activeModel == AiModelType.MEDIAPIPE_GEMMA3_1B)
                                "https://huggingface.co/litert-community/Gemma3-1B-IT"
                            else
                                "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-onnx"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        onImport = { picker.launch(arrayOf("*/*")) }
                    )
                    EdgeGalleryEmbeddingsCard(
                        onExplore = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://huggingface.co/models?other=tflite")))
                        }
                    )
                }

                // Right Column: RAG Vector Search & Knowledge Base Index Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "rag_actions_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOCAL RAG & KNOWLEDGE BASE",
                                style = MaterialTheme.typography.titleSmall,
                                color = LightBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showAddDocDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Doc", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item(key = "import_doc_row") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { pdfPicker.launch(arrayOf("application/pdf", "text/*", "*/*")) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Upload PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { videoPicker.launch(arrayOf("video/*")) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Import Video", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item(key = "vector_search_card") {
                        VectorSearchCard(ragQuery = ragQuery, searchResults = searchResults, onQueryChanged = vm::searchRag)
                    }

                    item(key = "knowledge_index_title") {
                        Text(
                            text = "Knowledge Base Index (${docs.size} Documents)",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightBluePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(docs, key = { it.id }) { doc ->
                        KnowledgeDocItem(doc, onDelete = { vm.deleteKnowledgeDoc(doc) })
                    }
                }
            }
        } else {
            // Standard Single-Column Vertical Feed for Portrait Mode
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "ai_header") {
                    AiHeaderCard()
                }

                item(key = "model_selector_card") {
                    ModelSelectorCard(
                        activeModel = activeModel,
                        isReady = isReady,
                        importing = importing,
                        onSelectModel = vm::selectModelType,
                        onBrowse = {
                            val url = if (activeModel == AiModelType.MEDIAPIPE_GEMMA3_1B)
                                "https://huggingface.co/litert-community/Gemma3-1B-IT"
                            else
                                "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-onnx"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        onImport = { picker.launch(arrayOf("*/*")) }
                    )
                }

                item(key = "edge_gallery_card") {
                    EdgeGalleryEmbeddingsCard(
                        onExplore = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://huggingface.co/models?other=tflite")))
                        }
                    )
                }

                item(key = "rag_actions_header_portrait") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOCAL RAG & KNOWLEDGE BASE",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showAddDocDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Doc", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item(key = "import_doc_row_portrait") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { pdfPicker.launch(arrayOf("application/pdf", "text/*", "*/*")) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Upload PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { videoPicker.launch(arrayOf("video/*")) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Import Video", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item(key = "vector_search_card_portrait") {
                    VectorSearchCard(ragQuery = ragQuery, searchResults = searchResults, onQueryChanged = vm::searchRag)
                }

                item(key = "knowledge_index_title_portrait") {
                    Text(
                        text = "Knowledge Base Index (${docs.size} Documents)",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(docs, key = { it.id }) { doc ->
                    KnowledgeDocItem(doc, onDelete = { vm.deleteKnowledgeDoc(doc) })
                }
            }
        }
    }

    if (showAddDocDialog) {
        AddKnowledgeDialog(
            docTitle = docTitle,
            onTitleChanged = { docTitle = it },
            docCategory = docCategory,
            onCategoryChanged = { docCategory = it },
            docContent = docContent,
            onContentChanged = { docContent = it },
            onDismiss = { showAddDocDialog = false },
            onSave = { title, content, cat ->
                vm.addKnowledgeDoc(title, content, cat)
                docTitle = ""
                docContent = ""
                showAddDocDialog = false
            }
        )
    }
}

@Composable
private fun AiHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = LightBlueContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = LightBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "On-Device AI & Edge Gallery Hub",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueOnContainer
                )
            }
            Text(
                text = "Download MediaPipe LLMs and Gemma Embeddings from Edge Gallery for ultra-fast, zero-cost offline RAG.",
                style = MaterialTheme.typography.bodyMedium,
                color = LightBlueOnContainer.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun ModelSelectorCard(
    activeModel: AiModelType,
    isReady: Boolean,
    importing: Boolean,
    onSelectModel: (AiModelType) -> Unit,
    onBrowse: () -> Unit,
    onImport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Active AI Engine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightBlueOnContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiModelType.entries.forEach { model ->
                    val isSelected = model == activeModel
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectModel(model) },
                        shape = CircleShape,
                        color = if (isSelected) LightBluePrimary else LightBlueSurface
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (model == AiModelType.MEDIAPIPE_GEMMA3_1B) "Gemma 3 1B" else "Phi-3 Mini",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else LightBlueOnContainer
                            )
                        }
                    }
                }
            }

            Text(
                text = activeModel.description,
                style = MaterialTheme.typography.bodyMedium,
                color = LightBlueOnContainer.copy(alpha = 0.8f)
            )

            Button(
                onClick = onBrowse,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("1. Browse Edge Gallery / Hugging Face", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (isReady) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                        contentDescription = null,
                        tint = if (isReady) LightBluePrimary else Color(0xFFB3261E),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isReady) "Model Installed ✅" else "Model Missing ❌",
                        fontWeight = FontWeight.Bold,
                        color = if (isReady) LightBluePrimary else Color(0xFFB3261E),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = onImport,
                    enabled = !importing,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (importing) "Importing..." else "2. Import .TASK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EdgeGalleryEmbeddingsCard(onExplore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlueContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Rounded.Hub, contentDescription = null, tint = LightBluePrimary, modifier = Modifier.size(22.dp))
                Text(
                    text = "Edge Gallery & Gemma Embeddings Hub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueOnContainer
                )
            }
            Text(
                text = "Isibalo Express supports MediaPipe on-device inference and vector embeddings for high-accuracy local RAG without cloud latency or data costs.",
                style = MaterialTheme.typography.bodyMedium,
                color = LightBlueOnContainer.copy(alpha = 0.85f)
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "💡 Recommended Embedding Model: Download 'universal_sentence_encoder.tflite' or 'all-MiniLM-L6-v2.tflite' from Hugging Face Edge Gallery.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = LightBlueOnContainer
                )
            }
            Button(
                onClick = onExplore,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LightBluePrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Explore Edge Gallery Embeddings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VectorSearchCard(
    ragQuery: String,
    searchResults: List<za.co.hbtechinc.isibalo.ai.OnDeviceRagEngine.SearchResult>,
    onQueryChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Test Vector Embedding Search (RAG)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightBlueOnContainer
            )
            SquareField(
                value = ragQuery,
                onValueChange = onQueryChanged,
                label = "Query Knowledge Base..."
            )

            if (searchResults.isNotEmpty()) {
                Text(
                    text = "Retrieved Context Snippets:",
                    style = MaterialTheme.typography.labelLarge,
                    color = LightBluePrimary,
                    fontWeight = FontWeight.Bold
                )
                searchResults.forEach { res ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = LightBlueSurface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${res.doc.title} (${(res.score * 100).toInt()}% match)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = LightBluePrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = res.doc.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightBlueOnContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun KnowledgeDocItem(doc: KnowledgeDoc, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "doc_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${doc.title} [${doc.category}]",
                    fontWeight = FontWeight.Bold,
                    color = LightBlueOnContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = doc.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightBlueOnContainer.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = onDelete,
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDAD6), contentColor = Color(0xFF410002)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete Document", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AddKnowledgeDialog(
    docTitle: String,
    onTitleChanged: (String) -> Unit,
    docCategory: String,
    onCategoryChanged: (String) -> Unit,
    docContent: String,
    onContentChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Add Knowledge Snippet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LightBlueOnContainer
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SquareField(docTitle, onTitleChanged, "Title / Topic (e.g. Return Policy)")
                SquareField(docCategory, onCategoryChanged, "Category (e.g. Policy, Supplier, FAQ)")
                SquareField(docContent, onContentChanged, "Content / Knowledge Snippet")
            }
        },
        confirmButton = {
            Button(
                onClick = { if (docTitle.isNotBlank() && docContent.isNotBlank()) onSave(docTitle.trim(), docContent.trim(), docCategory.trim()) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        }
    )
}