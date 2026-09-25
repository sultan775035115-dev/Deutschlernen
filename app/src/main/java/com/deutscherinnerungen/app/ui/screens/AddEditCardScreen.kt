package com.deutscherinnerungen.app.ui.screens

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deutscherinnerungen.app.data.entity.ReminderType
import com.deutscherinnerungen.app.media.MediaStorageManager
import com.deutscherinnerungen.app.ui.viewmodel.AddEditCardViewModel
import com.deutscherinnerungen.app.utils.DateTimeUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    viewModel: AddEditCardViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // إطلاق منتقي الصور
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.attachMedia(it, MediaStorageManager.MediaType.IMAGE) }
    }

    // إطلاق منتقي الفيديو
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.attachMedia(it, MediaStorageManager.MediaType.VIDEO) }
    }

    // إطلاق منتقي الملفات الصوتية
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.attachMedia(it, MediaStorageManager.MediaType.AUDIO) }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditMode) "تعديل البطاقة" else "إضافة بطاقة جديدة")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveCard() },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "حفظ البطاقة",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // حقل الكلمة أو العبارة
            OutlinedTextField(
                value = state.word,
                onValueChange = { viewModel.onWordChange(it) },
                label = { Text("الكلمة أو العبارة *") },
                placeholder = { Text("مثال: Serendipity أو Guten Tag") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // حقل المعنى / الترجمة
            OutlinedTextField(
                value = state.meaning,
                onValueChange = { viewModel.onMeaningChange(it) },
                label = { Text("المعنى / الترجمة *") },
                placeholder = { Text("مثال: صدفة جميلة وغير متوقعة") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // حقل الملاحظات الإضافية (اختياري)
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                label = { Text("ملاحظات إضافية (أمثلة، سياق، أصل الكلمة)") },
                placeholder = { Text("مثال: تستخدم في الأدب، جمعها...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            // قسم الوسائط (صورة، صوت، فيديو)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "الوسائط التوضيحية",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // 1. الصورة
                    if (!state.imagePath.isNullOrBlank() && File(state.imagePath!!).exists()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = File(state.imagePath!!),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "تم إرفاق صورة",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "استبدال الصورة")
                            }
                            IconButton(onClick = { viewModel.removeMedia(MediaStorageManager.MediaType.IMAGE) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الصورة", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إضافة صورة من الهاتف")
                        }
                    }

                    // 2. الملف الصوتي
                    if (!state.audioPath.isNullOrBlank() && File(state.audioPath!!).exists()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "تم إرفاق تسجيل صوتي",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { audioPickerLauncher.launch("audio/*") }) {
                                Icon(Icons.Default.Refresh, contentDescription = "استبدال الصوت")
                            }
                            IconButton(onClick = { viewModel.removeMedia(MediaStorageManager.MediaType.AUDIO) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الصوت", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إضافة ملف صوتي / نطق الكلمة")
                        }
                    }

                    // 3. الفيديو
                    if (!state.videoPath.isNullOrBlank() && File(state.videoPath!!).exists()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "تم إرفاق مقطع فيديو",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "استبدال الفيديو")
                            }
                            IconButton(onClick = { viewModel.removeMedia(MediaStorageManager.MediaType.VIDEO) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الفيديو", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إضافة مقطع فيديو")
                        }
                    }
                }
            }

            // قسم إعدادات التذكير
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تفعيل التذكير للبطاقة",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Switch(
                            checked = state.reminderEnabled,
                            onCheckedChange = { viewModel.onReminderToggle(it) }
                        )
                    }

                    if (state.reminderEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // اختيار نوع التذكير (يومي في وقت محدد أو تكرار كل عدد ساعات)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.reminderType == ReminderType.DAILY,
                                onClick = { viewModel.onReminderTypeChange(ReminderType.DAILY) }
                            )
                            Text(
                                text = "تذكير يومي في وقت محدد",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (state.reminderType == ReminderType.DAILY) {
                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute -> viewModel.onReminderTimeChange(hour, minute) },
                                        state.reminderHour,
                                        state.reminderMinute,
                                        false
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "وقت التذكير: ${DateTimeUtils.formatTime(state.reminderHour, state.reminderMinute)}"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.reminderType == ReminderType.INTERVAL,
                                onClick = { viewModel.onReminderTypeChange(ReminderType.INTERVAL) }
                            )
                            Text(
                                text = "تذكير متكرر كل عدد ساعات",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (state.reminderType == ReminderType.INTERVAL) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(2, 4, 6, 8, 12).forEach { hours ->
                                    val isSelected = state.reminderIntervalHours == hours
                                    Button(
                                        onClick = { viewModel.onReminderIntervalChange(hours) },
                                        colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("$hours س")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // زر الحفظ النهائي
            Button(
                onClick = { viewModel.saveCard() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (state.isEditMode) "حفظ التعديلات" else "إضافة البطاقة",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
