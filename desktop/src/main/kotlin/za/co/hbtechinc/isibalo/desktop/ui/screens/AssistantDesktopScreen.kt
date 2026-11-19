package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import za.co.hbtechinc.isibalo.desktop.data.DesktopRepository
import za.co.hbtechinc.isibalo.desktop.data.KnowledgeDoc
import za.co.hbtechinc.isibalo.desktop.ui.*

data class ChatMessage(val sender: String, val text: String)

@Composable
fun AssistantDesktopScreen(repository: DesktopRepository) {
    val knowledgeDocs by repository.knowledgeDocs.collectAsState()
    var userPrompt by remember { mutableStateOf("") }
    var isAddingDoc by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage("AI", "Hello! I am your Isibalo Express AI Business Assistant. I have full context on your stock levels, sales revenue, customer debts, and store policies. How can I help your business today?")
        )
    }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left Column: AI Assistant Chat Interface
        Card(
            modifier = Modifier.weight(1.3f).fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LightBlueContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = "AI Assistant", tint = LightBluePrimary)
                    Text("AI Business Assistant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                }

                // Chat Messages List
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(messages) { msg ->
                        val isAi = msg.sender == "AI"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isAi) LightBlueContainer else LightBluePrimary,
                                modifier = Modifier.widthIn(max = 500.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(msg.sender, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isAi) LightBluePrimary else Color.White.copy(alpha = 0.8f))
                                    Text(msg.text, style = MaterialTheme.typography.bodyMedium, color = if (isAi) LightBlueOnContainer else Color.White)
                                }
                            }
                        }
                    }
                }

                // Prompt Input
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SquareField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        label = "Ask questions about store sales, stock, or policies...",
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (userPrompt.isNotBlank()) {
                                val prompt = userPrompt
                                messages.add(ChatMessage("Owner", prompt))
                                userPrompt = ""

                                val snapshot = repository.businessSnapshot()
                                val responseText = buildString {
                                    append("Based on your current store snapshot:\n$snapshot\n\n")
                                    if (prompt.contains("stock", ignoreCase = true) || prompt.contains("low", ignoreCase = true)) {
                                        append("Recommendation: Please check low stock items and reorder with your wholesale suppliers.")
                                    } else if (prompt.contains("sales", ignoreCase = true) || prompt.contains("revenue", ignoreCase = true)) {
                                        append("Insight: Sales are currently tracked live in SQLite database. Keep offering card and mobile payment options to boost basket size.")
                                    } else {
                                        append("Your business operations are running smoothly with offline-first persistence.")
                                    }
                                }
                                messages.add(ChatMessage("AI", responseText))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                    ) {
                        Icon(Icons.Rounded.Send, contentDescription = "Send")
                    }
                }
            }
        }

        // Right Column: RAG Knowledge Base Docs
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, LightBlueContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RAG Knowledge Base Docs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                    Button(
                        onClick = { isAddingDoc = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Doc")
                        Spacer(Modifier.width(4.dp))
                        Text("Add Policy")
                    }
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(knowledgeDocs, key = { it.id }) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, LightBlueContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(doc.title, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                                Surface(shape = RoundedCornerShape(8.dp), color = LightBlueContainer) {
                                    Text(doc.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = LightBluePrimary)
                                }
                            }
                            Text(doc.content, style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                        }
                    }
                }
            }
        }
    }

    if (isAddingDoc) {
        var titleText by remember { mutableStateOf("") }
        var categoryText by remember { mutableStateOf("Policy") }
        var contentText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { isAddingDoc = false }) {
            Card(modifier = Modifier.width(420.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Add Knowledge Document", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

                    SquareField(value = titleText, onValueChange = { titleText = it }, label = "Document Title")
                    SquareField(value = categoryText, onValueChange = { categoryText = it }, label = "Category (e.g. Policy, Suppliers, Operations)")
                    SquareField(value = contentText, onValueChange = { contentText = it }, label = "Document Content & Guidelines", singleLine = false)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isAddingDoc = false }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (titleText.isNotBlank() && contentText.isNotBlank()) {
                                    repository.saveKnowledgeDoc(KnowledgeDoc(title = titleText, category = categoryText, content = contentText))
                                    isAddingDoc = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                        ) {
                            Text("Save Document")
                        }
                    }
                }
            }
        }
    }
}
