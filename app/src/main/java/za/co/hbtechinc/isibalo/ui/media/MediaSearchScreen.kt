package za.co.hbtechinc.isibalo.ui.media

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider

// Consistent Light Blue palette matching Isibalo design system
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@Composable
fun MediaSearchScreen(vm: MediaSearchViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val mediaList by vm.searchResults.collectAsStateWithLifecycle()

    // Detect orientation for adaptive landscape/portrait split layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                // Left Column: Hero Summary & Search Bar
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MediaHeroCard()
                    MediaSearchBar(query = query, onQueryChanged = vm::searchMedia)
                }

                // Right Column: Media Index Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "media_header_landscape") {
                        Text(
                            text = "Store Media Index (${mediaList.size} items)",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (mediaList.isEmpty()) {
                        item(key = "empty_media_landscape") {
                            Text(
                                "No matching photos or video moments found.",
                                color = LightBlueOnContainer.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(mediaList, key = { it.id }) { item ->
                            MediaItemCard(item = item)
                        }
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
                item(key = "media_hero") {
                    MediaHeroCard()
                }

                item(key = "media_search") {
                    MediaSearchBar(query = query, onQueryChanged = vm::searchMedia)
                }

                item(key = "media_list_header") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Store Media Index (${mediaList.size} items)",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (mediaList.isEmpty()) {
                    item(key = "empty_media") {
                        Text(
                            "No matching photos or video moments found.",
                            color = LightBlueOnContainer.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(mediaList, key = { it.id }) { item ->
                        MediaItemCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LightBluePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Instant Media & Video Finder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "On-Device AI Embeddings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Search local product photos and security video moments using plain text queries. Powered 100% locally by MediaPipe on-device embeddings.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun MediaSearchBar(query: String, onQueryChanged: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 1.dp
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search Icon",
                    tint = LightBluePrimary
                )
            },
            placeholder = {
                Text(
                    text = "Search photos or video clips (e.g. 'bread', 'cashier opening till')...",
                    color = LightBlueOnContainer.copy(alpha = 0.5f)
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = LightBlueOnContainer,
                unfocusedTextColor = LightBlueOnContainer
            )
        )
    }
}

@Composable
fun MediaItemCard(item: MediaItem) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "media_item_scale"
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
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LightBlueContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (item.type == "PHOTO") Icons.Rounded.Image else Icons.Rounded.Videocam,
                        contentDescription = item.type,
                        tint = LightBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightBlueOnContainer
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightBlueOnContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.type}  •  ${item.timestamp}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LightBluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
