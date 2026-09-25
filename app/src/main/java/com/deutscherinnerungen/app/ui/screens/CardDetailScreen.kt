package com.deutscherinnerungen.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deutscherinnerungen.app.srs.SpacedRepetitionEngine
import com.deutscherinnerungen.app.ui.components.AudioPlayerView
import com.deutscherinnerungen.app.ui.components.ConfirmationDialog
import com.deutscherinnerungen.app.ui.theme.LevelAmber
import com.deutscherinnerungen.app.ui.theme.LevelBlue
import com.deutscherinnerungen.app.ui.theme.LevelGreen
import com.deutscherinnerungen.app.ui.viewmodel.CardDetailViewModel
import com.deutscherinnerungen.app.utils.DateTimeUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    viewModel: CardDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.reviewSuccessMessage) {
        state.reviewSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSuccessMessage()
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "حذف البطاقة",
            message = "هل أنت متأكد من رغبتك في حذف هذه البطاقة وجميع ملفات الوسائط الخاصة بها؟",
            confirmText = "حذف",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteCard()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showSnoozeDialog) {
        AlertDialog(
            onDismissRequest = { showSnoozeDialog = false },
            title = { Text("أعد التذكير لاحقاً") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اختر الوقت المناسب لإعادة التذكير بمراجعة هذه البطاقة:")
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.snooze(SpacedRepetitionEngine.SnoozeOption.AFTER_10_MINUTES)
                            showSnoozeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بعد 10 دقائق")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.snooze(SpacedRepetitionEngine.SnoozeOption.AFTER_1_HOUR)
                            showSnoozeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بعد ساعة واحدة")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.snooze(SpacedRepetitionEngine.SnoozeOption.TOMORROW)
                            showSnoozeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("غداً")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSnoozeDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل البطاقة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    state.card?.let { card ->
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (card.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "المفضلة",
                                tint = if (card.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { onNavigateToEdit(card.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل البطاقة"
                            )
                        }

                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف البطاقة",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.card == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("لم يتم العثور على البطاقة")
            }
        } else {
            val card = state.card!!
            val levelColor = when {
                card.reviewLevel >= 6 -> LevelGreen
                card.reviewLevel >= 3 -> LevelBlue
                else -> LevelAmber
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // بطاقة الكلمة والمعنى الرئيسية
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // شارة المستوى وموعد المراجعة
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(levelColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = SpacedRepetitionEngine.getLevelTitleArabic(card.reviewLevel),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = levelColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DateTimeUtils.formatRelativeTime(card.nextReviewAt),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // الكلمة الأساسية
                        Text(
                            text = card.word,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // المعنى / الترجمة
                        Text(
                            text = card.meaning,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // الملاحظات الإضافية
                        if (card.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "ملاحظات:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = card.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // عرض الصورة إذا كانت مرفقة
                if (!card.imagePath.isNullOrBlank() && File(card.imagePath).exists()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = File(card.imagePath),
                            contentDescription = "صورة توضيحية للكلمة",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // مشغل الصوت إذا كان مرفقاً
                if (!card.audioPath.isNullOrBlank() && File(card.audioPath).exists()) {
                    AudioPlayerView(audioPath = card.audioPath)
                }

                // مشغل / رابط الفيديو إذا كان مرفقاً
                if (!card.videoPath.isNullOrBlank() && File(card.videoPath).exists()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مقطع فيديو توضيحي مرفق",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Button(
                                onClick = {
                                    val videoUri = Uri.parse(card.videoPath)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(videoUri, "video/*")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("مشاهدة الفيديو")
                            }
                        }
                    }
                }

                // تفاصيل التذكير
                if (card.reminderEnabled) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val reminderText = if (card.reminderType == com.deutscherinnerungen.app.data.entity.ReminderType.DAILY) {
                                "تذكير يومي في تمام الساعة ${DateTimeUtils.formatTime(card.reminderHour, card.reminderMinute)}"
                            } else {
                                "تذكير متكرر كل ${card.reminderIntervalHours} ساعات"
                            }
                            Text(
                                text = reminderText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // زرا التحكم الأساسيان للتكرار المتباعد (Spaced Repetition)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // زر "راجعتها"
                    Button(
                        onClick = { viewModel.markReviewed() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "راجعتها",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // زر "أعد التذكير لاحقاً"
                    OutlinedButton(
                        onClick = { showSnoozeDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "أعد التذكير لاحقاً",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
