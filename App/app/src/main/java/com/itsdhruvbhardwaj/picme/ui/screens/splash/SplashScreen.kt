package com.itsdhruvbhardwaj.picme.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsdhruvbhardwaj.picme.R
import com.itsdhruvbhardwaj.picme.ui.components.PicMeLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Logo and Tagline centered in the upper-middle area
        // Large clean whitespace above as per reference
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PicMeLogo(showTagline = true)
        }

        // Abstract glass artwork concentrated toward the LOWER portion
        Image(
            painter = painterResource(id = R.drawable.translucent_pastel_glass_waves),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.5f), // Occupy the lower half
            contentScale = ContentScale.Crop
        )
    }
}
