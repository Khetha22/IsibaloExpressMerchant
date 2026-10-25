package za.co.hbtechinc.isibalo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import za.co.hbtechinc.isibalo.R

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(Color(0xFF0A3AA8)), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.logo_wordmark),
            contentDescription = "Isibalo Express Merchant",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            contentScale = ContentScale.Fit
        )
    }
}
