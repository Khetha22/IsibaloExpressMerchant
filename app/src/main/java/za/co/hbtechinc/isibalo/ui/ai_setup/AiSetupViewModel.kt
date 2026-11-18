package za.co.hbtechinc.isibalo.ui.ai_setup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.hbtechinc.isibalo.ai.AiModelType
import za.co.hbtechinc.isibalo.ai.LocalAi
import za.co.hbtechinc.isibalo.ai.OnDeviceRagEngine
import za.co.hbtechinc.isibalo.data.KnowledgeDoc
import za.co.hbtechinc.isibalo.data.Repository
import java.io.File

class AiSetupViewModel(
    private val app: Application,
    private val repo: Repository,
    private val ai: LocalAi
) : ViewModel() {

    private val _selectedModel = MutableStateFlow(ai.activeModel)
    val selectedModel: StateFlow<AiModelType> = _selectedModel.asStateFlow()

    private val _isModelReady = MutableStateFlow(ai.isModelPresent())
    val isModelReady: StateFlow<Boolean> = _isModelReady
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ai.isModelPresent()
        )

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    val knowledgeDocs: StateFlow<List<KnowledgeDoc>> = repo.knowledgeDocs()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _ragQuery = MutableStateFlow("")
    val ragQuery: StateFlow<String> = _ragQuery.asStateFlow()

    private val _ragSearchResults = MutableStateFlow<List<OnDeviceRagEngine.SearchResult>>(emptyList())
    val ragSearchResults: StateFlow<List<OnDeviceRagEngine.SearchResult>> = _ragSearchResults.asStateFlow()

    fun selectModelType(type: AiModelType) {
        ai.selectModel(type)
        _selectedModel.value = type
        _isModelReady.value = ai.isModelPresent(type)
    }

    fun importModelFile(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                withContext(Dispatchers.IO) {
                    val dest = ai.modelFile(_selectedModel.value)
                    val tmp = File(dest.parentFile, "model_upload.part")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        tmp.outputStream().use { output -> input.copyTo(output) }
                    }
                    ai.reset()
                    if (dest.exists()) dest.delete()
                    tmp.renameTo(dest)
                }
            } catch (_: Exception) { }
            _isModelReady.value = ai.isModelPresent(_selectedModel.value)
            _isImporting.value = false
        }
    }

    fun addKnowledgeDoc(title: String, content: String, category: String) {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        if (trimmedTitle.isBlank() || trimmedContent.isBlank()) return

        viewModelScope.launch {
            repo.saveKnowledgeDoc(
                KnowledgeDoc(
                    title = trimmedTitle,
                    content = trimmedContent,
                    category = category.trim().ifBlank { "General" }
                )
            )
        }
    }

    fun importPdfDocument(uri: Uri) {
        viewModelScope.launch {
            try {
                val extractedText = withContext(Dispatchers.IO) {
                    val sb = StringBuilder()
                    app.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val textOnly = bytes.map {
                            if ((it in 32..126) || it.toInt() == 10 || it.toInt() == 13) it.toInt().toChar() else ' '
                        }.joinToString("")

                        val cleaned = textOnly
                            .replace(Regex("/[A-Za-z0-9]+"), " ")
                            .replace(Regex("obj|endobj|stream|endstream|xref|trailer|[\\[\\]<>]"), " ")
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        sb.append(cleaned)
                    }
                    sb.toString()
                }

                val title = uri.lastPathSegment ?: "Imported PDF Document"
                val content = if (extractedText.length > 60) {
                    extractedText.take(3000)
                } else {
                    "Uploaded PDF Document: $title. Contains store policies and operational guidelines."
                }

                repo.saveKnowledgeDoc(
                    KnowledgeDoc(
                        title = title,
                        content = content,
                        category = "PDF / Document"
                    )
                )
            } catch (_: Exception) { }
        }
    }

    fun importVideoDocument(uri: Uri) {
        viewModelScope.launch {
            val fileName = uri.lastPathSegment ?: "Store Security Video Clip"
            repo.saveKnowledgeDoc(
                KnowledgeDoc(
                    title = "Video Recording: $fileName",
                    content = "Local store security camera footage / video recording indexed for Video Moment Finder. URI: $uri",
                    category = "Video / CCTV"
                )
            )
        }
    }

    fun deleteKnowledgeDoc(doc: KnowledgeDoc) {
        viewModelScope.launch {
            repo.deleteKnowledgeDoc(doc)
        }
    }

    fun searchRag(query: String) {
        _ragQuery.value = query
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _ragSearchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            val docs = withContext(Dispatchers.IO) { repo.allKnowledgeDocs() }
            _ragSearchResults.value = OnDeviceRagEngine.search(trimmed, docs, topK = 3)
        }
    }
}