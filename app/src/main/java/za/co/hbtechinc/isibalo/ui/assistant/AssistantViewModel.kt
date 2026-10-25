package za.co.hbtechinc.isibalo.ui.assistant

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.ai.LocalAi
import za.co.hbtechinc.isibalo.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMsg(val fromUser: Boolean, val text: String)

class AssistantViewModel(
    private val app: Application,
    private val repo: Repository,
    private val ai: LocalAi
) : ViewModel() {

    private val _msgs = MutableStateFlow(
        listOf(ChatMsg(false, "Sawubona! Ask about today's sales, stock or pricing. I run fully offline on this phone."))
    )
    val msgs = _msgs.asStateFlow()
    val busy = MutableStateFlow(false)
    val importing = MutableStateFlow(false)
    val modelReady = MutableStateFlow(ai.isModelPresent())

    fun ask(q: String) {
        if (q.isBlank() || busy.value) return
        _msgs.update { it + ChatMsg(true, q.trim()) }
        if (!modelReady.value) {
            _msgs.update { it + ChatMsg(false, "The AI model is not installed yet. Import model.task first.") }
            return
        }
        busy.value = true
        viewModelScope.launch {
            val reply = try {
                ai.generate(buildPrompt(repo.businessSnapshot(), q.trim()))
            } catch (e: Exception) {
                "AI error: ${e.message ?: "unknown"}"
            }
            _msgs.update { it + ChatMsg(false, reply.trim()) }
            busy.value = false
        }
    }

    fun importModel(uri: Uri) {
        viewModelScope.launch {
            importing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val dest = ai.modelFile()
                    val tmp = java.io.File(dest.parentFile, "model.part")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        tmp.outputStream().use { input.copyTo(it) }
                    }
                    ai.reset()
                    dest.delete()
                    tmp.renameTo(dest)
                }
            } catch (e: Exception) {
                _msgs.update { it + ChatMsg(false, "Import failed: ${e.message}") }
            }
            modelReady.value = ai.isModelPresent()
            importing.value = false
        }
    }

    /** Gemma chat template. */
    private fun buildPrompt(snapshot: String, q: String) = buildString {
        append("<start_of_turn>user\n")
        append("You are Isibalo, a practical assistant for a small shop owner in South Africa. ")
        append("Use rands (R). Reply in simple English, under 120 words. Use only this shop data:\n")
        append(snapshot).append("\nQuestion: ").append(q)
        append("<end_of_turn>\n<start_of_turn>model\n")
    }
}
