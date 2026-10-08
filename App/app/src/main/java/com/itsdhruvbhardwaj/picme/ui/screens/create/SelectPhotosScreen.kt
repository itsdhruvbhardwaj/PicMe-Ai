package com.itsdhruvbhardwaj.picme.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsdhruvbhardwaj.picme.ui.components.EmptyPhotoSlot
import com.itsdhruvbhardwaj.picme.ui.components.PicMeButton
import com.itsdhruvbhardwaj.picme.ui.components.SelectedPhotoCard
import com.itsdhruvbhardwaj.picme.ui.components.TipsCard
import com.itsdhruvbhardwaj.picme.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPhotosScreen(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    // Mock state for selected photos
    var selectedPhotos by remember { mutableStateOf(listOf<String>("photo1")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                PicMeButton(
                    text = "Continue",
                    onClick = onContinueClick,
                    enabled = selectedPhotos.isNotEmpty()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Select Your Photos",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            
            Text(
                text = "Choose 1 to 5 clear photos for the best results",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(selectedPhotos) { photo ->
                    SelectedPhotoCard(onRemove = { selectedPhotos = selectedPhotos - photo })
                }
                if (selectedPhotos.size < 5) {
                    item {
                        EmptyPhotoSlot(onClick = { /* Trigger image picker */ })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TipsCard(
                tips = listOf(
                    "Use clear, front-facing photos",
                    "Good lighting works best",
                    "Avoid sunglasses or heavy filters"
                )
            )
        }
    }
}
