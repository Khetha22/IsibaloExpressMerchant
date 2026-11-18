package za.co.hbtechinc.isibalo.ui.assistant

import android.content.Intent
import android.content.res.Configuration
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.util.Locale

private val prompts = listOf(
    "Summarise today",
    "What should I restock?",
    "How can I sell more?"
)

// Consistent Light Blue palette matching Isibalo design system
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)
private val LightBlueBubbleUser = Color(0xFF1B6EF3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(vm: AssistantViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val msgs by vm.msgs.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val ready by vm.modelReady.collectAsStateWithLifecycle()
    val importing by vm.importing.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Detect orientation for adaptive landscape/portrait split layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::importModel)
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            input = spokenText
            vm.ask(spokenText)
        }
    }

    // Smooth spring-driven scroll when new messages arrive or thinking state toggles
    LaunchedEffect(msgs.size, busy) {
        val targetIndex = (msgs.size + if (busy) 1 else 0).coerceAtLeast(0)
        if (targetIndex > 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Scaffold(
        containerColor = LightBlueSurface,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBlueSurface,
                    titleContentColor = LightBlueOnContainer
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                        Column {
                            Text(
                                text = "Isibalo AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueOnContainer
                            )
                            Text(
                                text = if (ready) "Offline Gemma 3 Ready" else "Model Setup Required",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightBluePrimary
                            )
                        }
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = msgs.isNotEmpty(),
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        FilledTonalIconButton(
                            onClick = vm::clearChat,
                            shape = RoundedCornerShape(14.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFFFDAD6),
                                contentColor = Color(0xFF410002)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Clear Chat History"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            ExpressiveInputDock(
                input = input,
                onInputChange = { input = it },
                busy = busy,
                onPromptClick = { vm.ask(it) },
                onSendClick = {
                    if (input.isNotBlank() && !busy) {
                        vm.ask(input.trim())
                        input = ""
                    }
                },
                onMicClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your question to Isibalo...")
                    }
                    try {
                        speechRecognizerLauncher.launch(intent)
                    } catch (_: Exception) {
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLandscape) {
                // Adaptive Two-Column Split for Landscape Mode
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight()
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedVisibility(visible = !ready) {
                            ModelSetupCard(importing = importing, onImportClick = { picker.launch(arrayOf("*/*")) })
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = LightBlueContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "💡 Quick Tips",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueOnContainer
                                )
                                Text(
                                    text = "Ask questions in English or isiZulu about daily cashups, invoices, or inventory performance.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LightBlueOnContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight(),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = msgs,
                            key = { index, m -> "${index}_${m.fromUser}_${m.text.hashCode()}" }
                        ) { _, m ->
                            ExpressiveChatBubble(text = m.text, fromUser = m.fromUser)
                        }
                        if (busy) {
                            item(key = "thinking_indicator_landscape") {
                                ExpressiveThinkingBubble()
                            }
                        }
                    }
                }
            } else {
                // Standard Vertical Feed for Portrait Mode
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 900.dp),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "model_setup_banner") {
                        AnimatedVisibility(
                            visible = !ready,
                            enter = expandVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) + fadeIn(),
                            exit = shrinkVertically(
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            ) + fadeOut()
                        ) {
                            ModelSetupCard(importing = importing, onImportClick = { picker.launch(arrayOf("*/*")) })
                        }
                    }

                    itemsIndexed(
                        items = msgs,
                        key = { index, m -> "${index}_${m.fromUser}_${m.text.hashCode()}" }
                    ) { _, m ->
                        ExpressiveChatBubble(
                            text = m.text,
                            fromUser = m.fromUser,
                            modifier = Modifier.animateItem(
                                fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        )
                    }

                    if (busy) {
                        item(key = "thinking_indicator") {
                            ExpressiveThinkingBubble(
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelSetupCard(importing: Boolean, onImportClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlueContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Install Offline AI Engine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = LightBlueOnContainer
            )
            Text(
                text = "Download Gemma 3 1B (int4, .task) once, then import it. After that the assistant works completely offline with zero data.",
                style = MaterialTheme.typography.bodyMedium,
                color = LightBlueOnContainer
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "Search 'litert-community/Gemma3-1B-IT' on Hugging Face for gemma3-1b-it-int4.task",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = LightBlueOnContainer
                )
            }
            Button(
                onClick = onImportClick,
                enabled = !importing,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBluePrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (importing) "Importing Model..." else "Import Model (.task)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExpressiveChatBubble(
    text: String,
    fromUser: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleShape = if (fromUser) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 24.dp)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (fromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 750.dp)
                .fillMaxWidth(if (fromUser) 0.84f else 0.88f),
            shape = bubbleShape,
            color = if (fromUser) LightBlueBubbleUser else Color.White,
            tonalElevation = if (fromUser) 0.dp else 1.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (fromUser) Color.White else LightBlueOnContainer
            )
        }
    }
}

@Composable
private fun ExpressiveThinkingBubble(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "thinking_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp),
        color = LightBlueContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = LightBluePrimary.copy(alpha = alpha),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Thinking...",
                style = MaterialTheme.typography.labelLarge,
                color = LightBlueOnContainer.copy(alpha = alpha)
            )
        }
    }
}

@Composable
private fun ExpressiveInputDock(
    input: String,
    onInputChange: (String) -> Unit,
    busy: Boolean,
    onPromptClick: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit
) {
    val sendInteraction = remember { MutableInteractionSource() }
    val sendPressed by sendInteraction.collectIsPressedAsState()

    val sendScale by animateFloatAsState(
        targetValue = if (sendPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "send_scale"
    )
    val sendCorner by animateDpAsState(
        targetValue = if (sendPressed) 12.dp else 24.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "send_corner"
    )

    Surface(
        color = Color.White,
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(top = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(prompts) { _, prompt ->
                    FilterChip(
                        selected = false,
                        onClick = { if (!busy) onPromptClick(prompt) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = LightBlueContainer,
                            labelColor = LightBlueOnContainer
                        ),
                        border = null,
                        label = {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape),
                    placeholder = {
                        Text(
                            text = "Ask Isibalo...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightBlueSurface,
                        unfocusedContainerColor = LightBlueSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = LightBlueOnContainer,
                        unfocusedTextColor = LightBlueOnContainer
                    )
                )

                FilledTonalIconButton(
                    onClick = onMicClick,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = LightBlueContainer,
                        contentColor = LightBluePrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Voice Input"
                    )
                }

                FilledIconButton(
                    onClick = onSendClick,
                    enabled = !busy && input.isNotBlank(),
                    interactionSource = sendInteraction,
                    shape = RoundedCornerShape(sendCorner),
                    modifier = Modifier
                        .size(50.dp)
                        .graphicsLayer {
                            scaleX = sendScale
                            scaleY = sendScale
                        },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = LightBluePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send Prompt"
                    )
                }
            }
        }
    }
}