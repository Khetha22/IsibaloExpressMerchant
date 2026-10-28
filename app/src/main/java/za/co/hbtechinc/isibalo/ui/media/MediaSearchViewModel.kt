package za.co.hbtechinc.isibalo.ui.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.ai.OnDeviceRagEngine
import za.co.hbtechinc.isibalo.data.KnowledgeDoc
import za.co.hbtechinc.isibalo.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaItem(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "PHOTO" or "VIDEO_CLIP"
    val timestamp: String = "Today, 14:30"
)

class MediaSearchViewModel(private val repo: Repository) : ViewModel() {

    private val sampleMedia = listOf(
        MediaItem("1", "Albany Brown Bread 700g", "Stock shelf photo showing fresh Albany brown bread loafs", "PHOTO", "Today, 08:15"),
        MediaItem("2", "Sunflower Cooking Oil 750ml", "Inventory photo of Golden Cloud and Sunfoil cooking oil bottles on rack", "PHOTO", "Today, 09:30"),
        MediaItem("3", "Cashier opening till drawer for change", "Security video clip showing cashier opening till for R50 change", "VIDEO_CLIP", "Today, 11:20"),
        MediaItem("4", "Stock delivery truck unload", "Video moment of supplier driver unloading 5 crates of soft drinks", "VIDEO_CLIP", "Today, 13:45"),
        MediaItem("5", "Airtime scratch cards shelf", "Photo of Vodacom and MTN airtime cards behind counter glass", "PHOTO", "Yesterday, 16:10")
    )

    val searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow(sampleMedia)
    val searchResults = _searchResults.asStateFlow()

    fun searchMedia(query: String) {
        searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = sampleMedia
            return
        }

        viewModelScope.launch {
            // Perform on-device semantic embedding / text matching over media descriptions
            val docs = sampleMedia.map {
                KnowledgeDoc(id = it.id.toLong(), title = "${it.title} [${it.type}]", content = "${it.description} (${it.timestamp})")
            }
            val ragHits = OnDeviceRagEngine.search(query, docs, topK = 5)
            val matchedIds = ragHits.map { it.doc.id.toString() }

            _searchResults.value = if (matchedIds.isNotEmpty()) {
                sampleMedia.filter { it.id in matchedIds }
            } else {
                sampleMedia.filter {
                    it.title.contains(query.trim(), ignoreCase = true) ||
                        it.description.contains(query.trim(), ignoreCase = true)
                }
            }
        }
    }
}
