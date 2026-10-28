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
import kotlinx.coroutines.flow.asStateFlow
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
                val text = withContext(Dispatchers.IO) {
                    val sb = StringBuilder()
                    app.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                        sb.append(reader.readText())
                    }
                    sb.toString()
                }
                if (text.isNotBlank()) {
                    val title = uri.lastPathSegment ?: "Imported PDF Document"
                    repo.saveKnowledgeDoc(KnowledgeDoc(title = title, content = text.take(4000), category = "PDF / Document"))
                }
            } catch (_: Exception) { }
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
