package com.itsdhruvbhardwaj.picme.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itsdhruvbhardwaj.picme.ui.components.*
import com.itsdhruvbhardwaj.picme.ui.theme.Background

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStyleClick: (String) -> Unit,
    onSeeAllClick: (String?) -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = Background,
        topBar = {
            // Added statusBarsPadding to handle the safe area since the root Scaffold no longer provides it
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PicMeLogo(fontSize = 28)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CreditBadge(credits = 120)
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6)),
                        onClick = onProfileClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "JD",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadHomeData() }) {
                            Text("Retry")
                        }
                    }
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Search Bar
                        item {
                            PicMeSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }

                        // Categories
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(state.categories) { category ->
                                    CategoryChip(
                                        name = category,
                                        isSelected = selectedCategory == category,
                                        onClick = { 
                                            selectedCategory = category
                                        }
                                    )
                                }
                            }
                        }

                        // Trending Styles
                        item {
                            SectionHeader(
                                title = "Trending Styles",
                                onSeeAllClick = { onSeeAllClick("Trending") },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.trendingStyles) { style ->
                                    StyleCard(
                                        name = style.name,
                                        thumbnailUrl = style.thumbnail,
                                        onClick = { onStyleClick(style.id) }
                                    )
                                }
                            }
                        }

                        // Popular Section (All Styles for now)
                        item {
                            SectionHeader(
                                title = "Popular",
                                onSeeAllClick = { onSeeAllClick(null) },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.popularStyles) { style ->
                                    StyleCard(
                                        name = style.name,
                                        thumbnailUrl = style.thumbnail,
                                        onClick = { onStyleClick(style.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
