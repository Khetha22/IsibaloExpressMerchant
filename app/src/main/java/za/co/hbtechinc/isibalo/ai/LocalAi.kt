package za.co.hbtechinc.isibalo.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

enum class AiModelType(val displayName: String, val fileName: String, val description: String) {
    MEDIAPIPE_GEMMA3_1B(
        displayName = "MediaPipe Gemma 3 1B (int4)",
        fileName = "model.task",
        description = "Lightweight 1B parameter model optimized for mobile devices with MediaPipe."
    ),
    PHI_3_MINI(
        displayName = "Phi-3 Mini 3.8B (On-Device)",
        fileName = "phi3_mini.task",
        description = "Highly capable Microsoft Phi-3 model for complex business reasoning."
    )
}

/**
 * Offline LLM & On-Device RAG engine manager.
 * Supports MediaPipe Gemma 3 and Phi-3 models with zero network dependency.
 */
class LocalAi(private val context: Context) {
    private var engine: LlmInference? = null
    private val lock = Mutex()

    var activeModel: AiModelType = AiModelType.MEDIAPIPE_GEMMA3_1B
        private set

    var maxTokens: Int = 1024

    fun modelFile(model: AiModelType = activeModel) =
        File(context.getExternalFilesDir(null) ?: context.filesDir, model.fileName)

    fun isModelPresent(model: AiModelType = activeModel) =
        modelFile(model).let { it.exists() && it.length() > 500_000 } // Valid task file is > 500KB

    fun selectModel(model: AiModelType) {
        if (activeModel != model) {
            activeModel = model
            reset()
        }
    }

    suspend fun generate(prompt: String): String = lock.withLock {
        withContext(Dispatchers.Default) {
            val file = modelFile()
            if (!file.exists() || file.length() < 500_000) {
                throw IllegalStateException("Model file ${file.name} is missing or invalid. Please import a valid .task model file (ensure download completed successfully).")
            }
            val e = engine ?: try {
                LlmInference.createFromOptions(
                    context,
                    LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(file.absolutePath)
                        .setMaxTokens(maxTokens)
                        .build()
                ).also { engine = it }
            } catch (ex: Exception) {
                reset()
                throw Exception("Failed to initialize AI engine: ${ex.message}. Ensure model.task is compatible with MediaPipe GenAI.")
            }
            e.generateResponse(prompt)
        }
    }

    fun reset() {
        engine?.close()
        engine = null
    }

    companion object {
        const val MODEL_NAME = "model.task"
    }
}
