package za.co.hbtechinc.isibalo.ui.statements

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.util.PdfReportGenerator
import za.co.hbtechinc.isibalo.util.rands

// Consistent Light Blue palette matching Isibalo design system
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@Composable
fun FinancialStatementScreen(vm: FinancialStatementViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                // Left Column: Hero Banner & PDF Generation Action
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StatementHeroCard(state = state)
                    GeneratePdfButton(state = state, context = context)
                }

                // Right Column: Profit & Loss and Balance Sheet Cards
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(key = "pnl_landscape") {
                        ProfitAndLossCard(state = state)
                    }
                    item(key = "balance_sheet_landscape") {
                        BalanceSheetCard(state = state)
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
                item(key = "statement_hero") {
                    StatementHeroCard(state = state)
                }

                item(key = "generate_pdf_btn") {
                    GeneratePdfButton(state = state, context = context)
                }

                item(key = "pnl_statement") {
                    ProfitAndLossCard(state = state)
                }

                item(key = "balance_sheet_statement") {
                    BalanceSheetCard(state = state)
                }
            }
        }
    }
}

@Composable
private fun StatementHeroCard(state: StatementState) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Official Financial Statements",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Text(
                text = "Net Income: ${state.netProfit.rands()}",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Total Assets: ${state.totalAssetBase.rands()}  •  Stock Value: ${state.stockValuation.rands()}",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GeneratePdfButton(state: StatementState, context: Context) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "generate_pdf_scale"
    )

    Button(
        onClick = {
            val pdfFile = PdfReportGenerator.generateFinancialStatementPdf(
                context = context,
                totalRevenueCents = state.totalRevenue,
                totalExpensesCents = state.totalExpenses,
                netProfitCents = state.netProfit,
                stockValuationCents = state.stockValuation,
                outstandingTabsCents = state.outstandingTabs,
                paymentTotals = state.paymentTotals
            )
            if (pdfFile != null) {
                PdfReportGenerator.sharePdf(context, pdfFile)
            }
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LightBlueContainer,
            contentColor = LightBlueOnContainer
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.PictureAsPdf,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "GENERATE & SHARE PDF STATEMENT",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfitAndLossCard(state: StatementState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Statement of Profit & Loss (P&L)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = LightBlueOnContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gross Sales Revenue:", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text(state.totalRevenue.rands(), fontWeight = FontWeight.Bold, color = LightBluePrimary)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Operating Expenses:", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text("-${state.totalExpenses.rands()}", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            }
            HorizontalDivider(color = LightBlueSurface, thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Net Profit / Income:", fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                Text(
                    text = state.netProfit.rands(),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (state.netProfit >= 0) LightBluePrimary else Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
private fun BalanceSheetCard(state: StatementState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Balance Sheet Summary",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = LightBlueOnContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Inventory Assets (Retail Value):", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text(state.stockValuation.rands(), fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Outstanding Customer Tabs (Isikwelete):", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text(state.outstandingTabs.rands(), fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            }
            HorizontalDivider(color = LightBlueSurface, thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Estimated Asset Base:", fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                Text(state.totalAssetBase.rands(), fontWeight = FontWeight.ExtraBold, color = LightBluePrimary)
            }
        }
    }
}
