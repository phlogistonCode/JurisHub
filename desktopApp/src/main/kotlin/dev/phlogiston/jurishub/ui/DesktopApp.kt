package dev.phlogiston.jurishub.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.phlogiston.jurishub.core.ComparisonMode
import dev.phlogiston.jurishub.core.chooseFile
import dev.phlogiston.jurishub.core.createInlineDiff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DesktopApp() {
    var leftFile by remember { mutableStateOf<File?>(null) }
    var rightFile by remember { mutableStateOf<File?>(null) }
    var comparison by remember { mutableStateOf<Pair<String, String>?>(null) }
    var comparisonMode by remember { mutableStateOf(ComparisonMode.LINES) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var selectedFont by remember { mutableStateOf("Monospace") }
    var wrapLines by remember { mutableStateOf(true) }
    val screenScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var highlighted by remember { mutableStateOf<Pair<AnnotatedString, AnnotatedString>?>(null) }
    var isHighlighting by remember { mutableStateOf(false) }

    LaunchedEffect(comparison, comparisonMode) {
        val texts = comparison
        if (texts == null) {
            highlighted = null
            isHighlighting = false
        } else {
            isHighlighting = true
            val newHighlights = withContext(Dispatchers.Default) {
                createInlineDiff(texts.first, texts.second, comparisonMode)
            }
            highlighted = newHighlights
            isHighlighting = false
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (showSettings) {
                SettingsScreen(
                    selectedFont = selectedFont,
                    onFontSelected = { selectedFont = it },
                    wrapLines = wrapLines,
                    onWrapLinesChanged = { wrapLines = it },
                    onBack = { showSettings = false }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(screenScrollState).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(onClick = { showSettings = true }, modifier = Modifier.align(Alignment.End)) { Text("Настройки") }
                        Text("Сравнение юридических документов", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DocumentChooser(
                                title = "Документ 1",
                                file = leftFile,
                                onChoose = {
                                    leftFile = chooseFile()
                                    error = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                            DocumentChooser(
                                title = "Документ 2",
                                file = rightFile,
                                onChoose = {
                                    rightFile = chooseFile()
                                    error = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text("Способ сравнения", style = MaterialTheme.typography.titleSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ComparisonMode.entries.forEach { mode ->
                                    Button(onClick = { comparisonMode = mode }, enabled = comparisonMode != mode, modifier = Modifier.weight(1f)) {
                                        Text(mode.label)
                                    }
                                }
                            }
                            if (isHighlighting) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                        Button(
                            enabled = leftFile != null && rightFile != null && !isLoading,
                            onClick = {
                                val left = requireNotNull(leftFile)
                                val right = requireNotNull(rightFile)
                                isLoading = true
                                loadingProgress = 0.05f
                                error = null
                                scope.launch {
                                    try {
                                        comparison = withContext(Dispatchers.IO) {
                                            compareDocuments(left, right) { progress ->
                                                launch { loadingProgress = progress }
                                            }
                                        }
                                    } catch (exception: Exception) {
                                        comparison = null
                                        error = exception.message ?: "Не удалось прочитать документы"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) { Text("Сравнить документы") }
                        if (isLoading) {
                            AlertDialog(
                                onDismissRequest = {},
                                title = { Text("Загрузка документов") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        LinearProgressIndicator(progress = { loadingProgress }, modifier = Modifier.fillMaxWidth())
                                        Text("Извлечение текста и подготовка сравнения… ${(loadingProgress * 100).toInt()}%")
                                    }
                                },
                                confirmButton = {}
                            )
                        }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        if (comparison != null && highlighted != null) {
                            SideBySideDiffView(requireNotNull(comparison), requireNotNull(highlighted), selectedFont, wrapLines)
                        } else {
                            Text("Выберите два файла в форматах PDF, TXT, DOC или DOCX")
                        }
                    }
                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(screenScrollState),
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        style = ScrollbarStyle(
                            minimalHeight = 32.dp,
                            thickness = 8.dp,
                            shape = MaterialTheme.shapes.small,
                            hoverDurationMillis = 150,
                            unhoverColor = Color(0xFFB0B5BD),
                            hoverColor = Color(0xFF6F7782)
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DesktopPreview() {
    DesktopApp()
}



