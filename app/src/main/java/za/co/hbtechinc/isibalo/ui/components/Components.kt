package za.co.hbtechinc.isibalo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SquareButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    if (outlined) {
        OutlinedButton(
            onClick = onClick, modifier = modifier.height(48.dp), enabled = enabled,
            shape = RectangleShape, border = BorderStroke(1.5.dp, cs.primary)
        ) { Text(text, fontWeight = FontWeight.Bold) }
    } else {
        Button(
            onClick = onClick, modifier = modifier.height(48.dp), enabled = enabled,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
        ) { Text(text, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun SquareIconButton(icon: ImageVector, desc: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick, modifier = modifier.size(40.dp), shape = RectangleShape,
        color = cs.primary, contentColor = cs.onPrimary
    ) { Box(contentAlignment = Alignment.Center) { Icon(icon, desc) } }
}

@Composable
fun SquareCard(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = RectangleShape, color = color, border = border) {
            Column(content = content)
        }
    } else {
        Surface(modifier = modifier, shape = RectangleShape, color = color, border = border) {
            Column(content = content)
        }
    }
}

/** Square toggle used for payment method selection and quick prompts. */
@Composable
fun SquareChoice(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick, modifier = modifier, shape = RectangleShape,
        color = if (selected) cs.primary else cs.surface,
        contentColor = if (selected) cs.onPrimary else cs.onSurface,
        border = BorderStroke(1.5.dp, cs.primary)
    ) {
        Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
            Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}
