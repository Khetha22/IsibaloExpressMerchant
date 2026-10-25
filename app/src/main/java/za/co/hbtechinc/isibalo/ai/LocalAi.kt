package za.co.hbtechinc.isibalo.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Offline LLM via MediaPipe LLM Inference. No network needed once model.task is on the phone.
 * Recommended: Gemma 3 1B IT int4 (.task), about 550 MB.
 */
class LocalAi(private val context: Context) {
    private var engine: LlmInference? = null
    private val lock = Mutex()

    fun modelFile() = File(context.getExternalFilesDir(null) ?: context.filesDir, MODEL_NAME)
    fun isModelPresent() = modelFile().let { it.exists() && it.length() > 0 }

    suspend fun generate(prompt: String): String = lock.withLock {
        withContext(Dispatchers.Default) {
            val e = engine ?: LlmInference.createFromOptions(
                context,
                LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile().absolutePath)
                    .setMaxTokens(1024)
                    .build()
            ).also { engine = it }
            e.generateResponse(prompt)
        }
    }

    fun reset() { engine?.close(); engine = null }

    companion object { const val MODEL_NAME = "model.task" }
}
