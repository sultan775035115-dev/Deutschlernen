package com.deutscherinnerungen.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.deutscherinnerungen.app.ui.components.WordCardItem
import com.deutscherinnerungen.app.ui.viewmodel.CardFilter
import com.deutscherinnerungen.app.ui.viewmodel.CardSortOrder
import com.deutscherinnerungen.app.ui.viewmodel.CardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CardsViewModel,
    onNavigateToAddCard: () -> Unit,
    onNavigateToCardDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onSpeakWord: ((String) -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    var isSearchVisible by remember { mutableStateOf(false) }
    var isSortMenuVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تثبيت الكلمات",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // زر البحث
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث"
                        )
                    }

                    // قائمة الترتيب
                    Box {
                        IconButton(onClick = { isSortMenuVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "ترتيب البطاقات"
                            )
                        }

                        DropdownMenu(
                            expanded = isSortMenuVisible,
                            onDismissRequest = { isSortMenuVisible = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("حسب موعد المراجعة القادمة") },
                                onClick = {
                                    viewModel.onSortOrderChange(CardSortOrder.NEXT_REVIEW)
                                    isSortMenuVisible = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الأحدث إضافة") },
                                onClick = {
                                    viewModel.onSortOrderChange(CardSortOrder.DATE_ADDED_DESC)
                                    isSortMenuVisible = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الأقدم إضافة") },
                                onClick = {
                                    viewModel.onSortOrderChange(CardSortOrder.DATE_ADDED_ASC)
                                    isSortMenuVisible = false
                                }
                            )
                        }
                    }

                    // زر الإعدادات
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCard,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة بطاقة جديدة"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // شريط البحث المنسدل
            if (isSearchVisible) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("ابحث عن كلمة، معنى، أو ملاحظة...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // خيارات التصفية (فلترة)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filter == CardFilter.ALL,
                    onClick = { viewModel.onFilterChange(CardFilter.ALL) },
                    label = { Text("الكل (${state.cards.size})") },
                    colors = FilterChipDefaults.filterChipColors()
                )

                FilterChip(
                    selected = state.filter == CardFilter.DUE_TODAY,
                    onClick = { viewModel.onFilterChange(CardFilter.DUE_TODAY) },
                    label = { Text("للمراجعة (${state.dueCardsCount})") }
                )

                FilterChip(
                    selected = state.filter == CardFilter.FAVORITES,
                    onClick = { viewModel.onFilterChange(CardFilter.FAVORITES) },
                    label = { Text("المفضلة") }
                )
            }

            // عرض المحتوى أو حالة التحميل أو الحالة الفارغة
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (state.searchQuery.isNotBlank()) "لم يتم العثور على نتائج للبحث" else "لا توجد بطاقات مضافة حتى الآن",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اضغط على زر (+) لإضافة أول كلمة أو عبارة وتثبيتها بالذاكرة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.cards,
                        key = { it.id }
                    ) { card ->
                        WordCardItem(
                            card = card,
                            onClick = { onNavigateToCardDetail(card.id) },
                            onFavoriteToggle = { viewModel.toggleFavorite(card) },
                            onSpeak = if (onSpeakWord != null) { { onSpeakWord(card.word) } } else null
                        )
                    }
                }
            }
        }
    }
}
