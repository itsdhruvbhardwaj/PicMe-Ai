package com.itsdhruvbhardwaj.picme.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.itsdhruvbhardwaj.picme.ui.components.PicMeButton
import com.itsdhruvbhardwaj.picme.ui.components.TipsCard
import com.itsdhruvbhardwaj.picme.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleDetailsScreen(
    viewModel: StyleDetailsViewModel,
    onBackClick: () -> Unit,
    onTryStyleClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Favorite action */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (uiState is StyleDetailsUiState.Success) {
                val style = (uiState as StyleDetailsUiState.Success).style
                Box(modifier = Modifier.padding(24.dp)) {
                    PicMeButton(
                        text = "Try This Style",
                        onClick = { onTryStyleClick(style.id) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is StyleDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is StyleDetailsUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is StyleDetailsUiState.Success -> {
                    val style = state.style
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        item {
                            // Hero Image
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(style.thumbnail)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = style.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .background(Color(0xFFF3F4F6)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Title
                            Text(
                                text = style.name,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Description
                            Text(
                                text = style.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                            
                            // Dynamic Preview Images
                            if (style.previewImages.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(style.previewImages.take(4)) { imageUrl ->
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(imageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .background(Color(0xFFF3F4F6)),
                                            contentScale = ContentScale.Crop
                                        )
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
                            
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}
