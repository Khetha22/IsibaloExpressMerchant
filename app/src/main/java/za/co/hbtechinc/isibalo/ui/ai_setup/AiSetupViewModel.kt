package za.co.hbtechinc.isibalo.ui.ai_setup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.ai.AiModelType
import za.co.hbtechinc.isibalo.ai.LocalAi
import za.co.hbtechinc.isibalo.ai.OnDeviceRagEngine
import za.co.hbtechinc.isibalo.data.KnowledgeDoc
import za.co.hbtechinc.isibalo.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AiSetupViewModel(
    private val app: Application,
    private val repo: Repository,
    private val ai: LocalAi
) : ViewModel() {

    val selectedModel = MutableStateFlow(ai.activeModel)
    val isModelReady = MutableStateFlow(ai.isModelPresent())
    val isImporting = MutableStateFlow(false)

    val knowledgeDocs: StateFlow<List<KnowledgeDoc>> = repo.knowledgeDocs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ragQuery = MutableStateFlow("")
    val ragSearchResults = MutableStateFlow<List<OnDeviceRagEngine.SearchResult>>(emptyList())

    fun selectModelType(type: AiModelType) {
        ai.selectModel(type)
        selectedModel.value = type
        isModelReady.value = ai.isModelPresent(type)
    }

    fun importModelFile(uri: Uri) {
        viewModelScope.launch {
            isImporting.value = true
            try {
                withContext(Dispatchers.IO) {
                    val dest = ai.modelFile(selectedModel.value)
                    val tmp = File(dest.parentFile, "model_upload.part")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        tmp.outputStream().use { input.copyTo(it) }
                    }
                    ai.reset()
                    dest.delete()
                    tmp.renameTo(dest)
                }
            } catch (_: Exception) { }
            isModelReady.value = ai.isModelPresent(selectedModel.value)
            isImporting.value = false
        }
    }

    fun addKnowledgeDoc(title: String, content: String, category: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repo.saveKnowledgeDoc(KnowledgeDoc(title = title, content = content, category = category))
        }
    }

    fun importPdfDocument(uri: Uri) {
        viewModelScope.launch {
            try {
                val extractedText = withContext(Dispatchers.IO) {
                    val sb = StringBuilder()
                    app.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val cleaned = bytes.map { if ((it in 32..126) || it.toInt() == 10 || it.toInt() == 13) it.toInt().toChar() else ' ' }.joinToString("")
                        sb.append(cleaned)
                    }
                    sb.toString().replace(Regex("\\s+"), " ").trim()
                }
                if (extractedText.isNotBlank()) {
                    val title = uri.lastPathSegment ?: "Imported PDF Document"
                    val chunks = extractedText.chunked(2000)
                    chunks.forEachIndexed { index, chunk ->
                        val chunkTitle = if (chunks.size > 1) "$title (Part ${index + 1})" else title
                        repo.saveKnowledgeDoc(KnowledgeDoc(title = chunkTitle, content = chunk, category = "PDF / Document"))
                    }
                }
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
        viewModelScope.launch { repo.deleteKnowledgeDoc(doc) }
    }

    fun searchRag(query: String) {
        ragQuery.value = query
        if (query.isBlank()) {
            ragSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val docs = repo.allKnowledgeDocs()
            ragSearchResults.value = OnDeviceRagEngine.search(query, docs, topK = 3)
        }
    }
}
