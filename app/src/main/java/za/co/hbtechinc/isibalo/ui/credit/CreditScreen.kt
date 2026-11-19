package za.co.hbtechinc.isibalo.ui.credit

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ReceiptLong
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.CustomerDebt
import za.co.hbtechinc.isibalo.ui.components.SquareField
import za.co.hbtechinc.isibalo.util.rands

// Consistent Light Blue palette matching Isibalo design system
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@Composable
fun CreditScreen(vm: CreditViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val debts by vm.debts.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }

    val totalOutstanding = debts.filter { !it.settled }.sumOf { it.amountCents }

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
                // Left Column: Total Outstanding Summary & Record Tab Action
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CreditHeroCard(totalOutstanding = totalOutstanding)
                    Button(
                        onClick = { adding = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("RECORD CUSTOMER TAB", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }

                // Right Column: Customer Tabs Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "credit_header_landscape") {
                        Text(
                            text = "Customer Tabs (${debts.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (debts.isEmpty()) {
                        item(key = "empty_credit_landscape") {
                            Text(
                                "No customer credit tabs recorded yet.",
                                color = LightBlueOnContainer.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(debts, key = { it.id }) { debt ->
                            DebtItem(
                                debt = debt,
                                onSettle = { vm.settle(debt.id) },
                                onDelete = { vm.delete(debt) }
                            )
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
                item(key = "credit_hero") {
                    CreditHeroCard(totalOutstanding = totalOutstanding)
                }

                item(key = "record_tab_btn") {
                    Button(
                        onClick = { adding = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("RECORD CUSTOMER TAB", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }

                item(key = "credit_list_header") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Customer Tabs (${debts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (debts.isEmpty()) {
                    item(key = "empty_credit") {
                        Text(
                            "No customer credit tabs recorded yet.",
                            color = LightBlueOnContainer.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(debts, key = { it.id }) { debt ->
                        DebtItem(
                            debt = debt,
                            onSettle = { vm.settle(debt.id) },
                            onDelete = { vm.delete(debt) }
                        )
                    }
                }
            }
        }
    }

    if (adding) {
        AddDebtDialog(
            onDismiss = { adding = false },
            onSave = { name, phone, amount, note ->
                vm.addDebt(name, phone, amount, note)
                adding = false
            }
        )
    }
}

@Composable
private fun CreditHeroCard(totalOutstanding: Long) {
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
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Customer Credit Tabs (Isikwelete)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = totalOutstanding.rands(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Track customer credit balances and tab settlements",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun DebtItem(debt: CustomerDebt, onSettle: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "debt_item_scale"
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = debt.customerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightBlueOnContainer
                )
                if (debt.phone.isNotBlank()) {
                    Text(
                        text = "Phone: ${debt.phone}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LightBlueOnContainer.copy(alpha = 0.7f)
                    )
                }
                if (debt.note.isNotBlank()) {
                    Text(
                        text = "Note: ${debt.note}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightBlueOnContainer.copy(alpha = 0.8f)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (debt.settled) "SETTLED" else "OWES: ${debt.amountCents.rands()}",
                    fontWeight = FontWeight.Bold,
                    color = if (debt.settled) LightBluePrimary else Color(0xFFC62828),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!debt.settled) {
                    Button(
                        onClick = onSettle,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = "Settle Tab", modifier = Modifier.size(18.dp))
                    }
                }
                Button(
                    onClick = onDelete,
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDAD6), contentColor = Color(0xFF410002)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete Record", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AddDebtDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f).padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Record Customer Tab (Isikwelete)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueOnContainer
                )

                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SquareField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Customer Name"
                    )
                    SquareField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        keyboardType = KeyboardType.Phone
                    )
                    SquareField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Amount Owed (R)",
                        keyboardType = KeyboardType.Decimal
                    )
                    SquareField(
                        value = note,
                        onValueChange = { note = it },
                        label = "Items / Note (e.g. 2x Bread, 1x Milk)"
                    )
                }

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { if (name.isNotBlank() && amount.isNotBlank()) onSave(name.trim(), phone.trim(), amount.trim(), note.trim()) },
                    enabled = name.isNotBlank() && amount.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Save Customer Tab", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
