package com.itsdhruvbhardwaj.picme.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsdhruvbhardwaj.picme.ui.theme.BlueAccent
import com.itsdhruvbhardwaj.picme.ui.theme.PurpleAccent

@Composable
fun PicMeLogo(
    modifier: Modifier = Modifier,
    showTagline: Boolean = false,
    fontSize: Int = 48
) {
    val navyColor = Color(0xFF0F172A) // Deep Navy Typography
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = navyColor,
                        fontWeight = FontWeight.Black
                    )
                ) {
                    append("Pic")
                }
                withStyle(
                    style = SpanStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PurpleAccent, BlueAccent)
                        ),
                        fontWeight = FontWeight.Black
                    )
                ) {
                    append("Me")
                }
            },
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = fontSize.sp,
                letterSpacing = (-2.5).sp, // Tight spacing
                textAlign = TextAlign.Center
            )
        )
        if (showTagline) {
            Text(
                text = "Turn Your Photos\nInto Amazing Creations",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold, // Bold modern typography
                    fontSize = 18.sp
                ),
                color = navyColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
