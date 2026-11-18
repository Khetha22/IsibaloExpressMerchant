package za.co.hbtechinc.isibalo.ui.cashup

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.CashUpRecord
import za.co.hbtechinc.isibalo.ui.components.SquareField
import za.co.hbtechinc.isibalo.util.rands
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Consistent Light Blue palette matching Assistant, Dashboard, Jobs, Prices, Reports, Settings, and AI Setup screens
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@Composable
fun CashUpScreen(vm: CashUpViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val history by vm.cashUpHistory.collectAsStateWithLifecycle()

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
                // Left Column: Expected Totals & Reconciliation Inputs
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CashUpHeaderCard()
                    ExpectedTotalsCard(state = state)
                    ReconciliationCard(vm = vm, state = state)
                }

                // Right Column: Cash-Up History Log
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "history_header_landscape") {
                        Text(
                            text = "Cash-Up History Log",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (history.isEmpty()) {
                        item(key = "empty_history_landscape") {
                            Text(
                                "No previous cash-up records saved yet.",
                                color = LightBlueOnContainer.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(history, key = { it.id }) { record ->
                            CashUpRecordItem(record)
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
                item(key = "cashup_header") {
                    CashUpHeaderCard()
                }

                item(key = "expected_totals") {
                    ExpectedTotalsCard(state = state)
                }

                item(key = "reconciliation_card") {
                    ReconciliationCard(vm = vm, state = state)
                }

                item(key = "history_header") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Cash-Up History Log",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (history.isEmpty()) {
                    item(key = "empty_history") {
                        Text(
                            "No previous cash-up records saved yet.",
                            color = LightBlueOnContainer.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(history, key = { it.id }) { record ->
                        CashUpRecordItem(record)
                    }
                }
            }
        }
    }
}

@Composable
private fun CashUpHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = LightBlueContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.PointOfSale,
                            contentDescription = null,
                            tint = LightBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "End of Day Cash-Up",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueOnContainer
                )
            }
            Text(
                text = "Reconcile cash in drawer against system sales.",
                style = MaterialTheme.typography.bodyMedium,
                color = LightBlueOnContainer.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun ExpectedTotalsCard(state: CashUpUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightBluePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Today's Expected System Totals",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Expected Cash Sales:", color = Color.White.copy(alpha = 0.9f))
                Text(state.expectedCashCents.rands(), fontWeight = FontWeight.Bold, color = Color.White)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Card Sales:", color = Color.White.copy(alpha = 0.9f))
                Text(state.cardTotalCents.rands(), color = Color.White)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mobile / Speedpoint:", color = Color.White.copy(alpha = 0.9f))
                Text(state.mobileTotalCents.rands(), color = Color.White)
            }
        }
    }
}

@Composable
private fun ReconciliationCard(vm: CashUpViewModel, state: CashUpUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cash Reconciliation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightBlueOnContainer
            )

            SquareField(
                value = state.floatText,
                onValueChange = vm::onFloatChanged,
                label = "Opening Till Float (R)"
            )

            SquareField(
                value = state.actualCashText,
                onValueChange = vm::onActualCashChanged,
                label = "Actual Cash Counted in Drawer (R)"
            )

            val variance = state.varianceCents
            val varianceColor = when {
                variance == 0L -> Color(0xFF2E7D32)
                variance > 0L -> Color(0xFF1565C0)
                else -> Color(0xFFC62828)
            }
            val varianceStatus = when {
                variance == 0L -> "Balanced Exact"
                variance > 0L -> "Over by ${variance.rands()}"
                else -> "Short by ${(-variance).rands()}"
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = varianceColor.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Calculated Variance", style = MaterialTheme.typography.labelMedium, color = varianceColor)
                    Text(
                        text = varianceStatus,
                        style = MaterialTheme.typography.titleMedium,
                        color = varianceColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            SquareField(
                value = state.notesText,
                onValueChange = vm::onNotesChanged,
                label = "Shift Notes / Explanation"
            )

            Button(
                onClick = vm::saveCashUp,
                enabled = state.actualCashText.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBluePrimary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.isSavedSuccess) "CASH-UP RECORDED!" else "SAVE CASH-UP RECORD",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CashUpRecordItem(record: CashUpRecord) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ROOT).format(Date(record.createdAt))

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cashup_row_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dateStr, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightBlueOnContainer)
                val statusText = when {
                    record.varianceCents == 0L -> "Balanced"
                    record.varianceCents > 0L -> "+${record.varianceCents.rands()}"
                    else -> "-${(-record.varianceCents).rands()}"
                }
                Text(
                    text = statusText,
                    fontWeight = FontWeight.Bold,
                    color = if (record.varianceCents < 0) Color(0xFFC62828) else LightBluePrimary
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Expected Cash: ${record.expectedCashCents.rands()}", style = MaterialTheme.typography.bodyMedium, color = LightBlueOnContainer.copy(alpha = 0.7f))
                Text("Actual Cash: ${record.actualCashCents.rands()}", style = MaterialTheme.typography.bodyMedium, color = LightBlueOnContainer.copy(alpha = 0.7f))
            }
            if (record.notes.isNotBlank()) {
                Text(
                    text = "Notes: ${record.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightBlueOnContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}