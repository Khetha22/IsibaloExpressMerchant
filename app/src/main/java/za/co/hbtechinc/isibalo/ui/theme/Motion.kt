package za.co.hbtechinc.isibalo.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Expressive Motion Theming specifications using Material 3 spring physics and directional transitions.
 */
object IsibaloMotion {
    val enterTransition: EnterTransition = fadeIn(
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    ) + slideInHorizontally(
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        initialOffsetX = { fullWidth -> fullWidth / 10 }
    )

    val exitTransition: ExitTransition = fadeOut(
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    ) + slideOutHorizontally(
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        targetOffsetX = { fullWidth -> -fullWidth / 10 }
    )
}
