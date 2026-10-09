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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.itsdhruvbhardwaj.picme.ui.auth.AuthState
import com.itsdhruvbhardwaj.picme.ui.auth.AuthViewModel
import com.itsdhruvbhardwaj.picme.ui.components.*
import com.itsdhruvbhardwaj.picme.ui.theme.Background

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    viewModel: HomeViewModel,
    onStyleClick: (String) -> Unit,
    onSeeAllClick: (String?) -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = Background,
        topBar = {
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
                    // CreditBadge - Logic ready for real backend value
                    // Currently using 0 or empty state as we don't have it in User model yet
                    CreditBadge(credits = 0)
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6)),
                        onClick = onProfileClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val user = (authState as? AuthState.Authenticated)?.user
                            if (user?.profileImage != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(user.profileImage)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initials = user?.name?.take(2)?.uppercase() ?: "U"
                                Text(
                                    text = initials,
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
                        item {
                            PicMeSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }

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

                        item {
                            SectionHeader(
                                title = "Trending Styles",
                                onSeeAllClick = { onSeeAllClick("Trending") },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
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

data class StyleData(
    val name: String,
    val imageUrl: String
)
