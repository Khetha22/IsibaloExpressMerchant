package za.co.hbtechinc.isibalo.ai

import java.io.File

/**
 * Compose Multiplatform abstraction interface for on-device AI generation.
 * Allows Android (MediaPipe) and iOS/Desktop to implement platform-specific ML runtimes.
 */
interface PlatformAiEngine {
    val activeModel: AiModelType
    fun selectModel(type: AiModelType)
    fun isModelPresent(type: AiModelType = activeModel): Boolean
    fun modelFile(type: AiModelType = activeModel): File
    suspend fun generate(prompt: String, context: String): String
    fun reset()
}
