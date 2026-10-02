package dev.phlogiston.jurishub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun DesktopApp() {
    var leftFile by remember { mutableStateOf<File?>(null) }
    var rightFile by remember { mutableStateOf<File?>(null) }
    var comparison by remember { mutableStateOf<Pair<String, String>?>(null) }
    var comparisonMode by remember { mutableStateOf(ComparisonMode.LINES) }
    var error by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("JurisHub", style = MaterialTheme.typography.headlineMedium)
                Text("Сравнение юридических документов", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DocumentChooser("Документ 1", leftFile) {
                        leftFile = chooseFile()
                        error = null
                    }
                    DocumentChooser("Документ 2", rightFile) {
                        rightFile = chooseFile()
                        error = null
                    }
                }
                Text("Способ сравнения", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ComparisonMode.entries.forEach { mode ->
                        Button(
                            onClick = { comparisonMode = mode },
                            enabled = comparisonMode != mode
                        ) { Text(mode.label) }
                    }
                }
                Button(
                    enabled = leftFile != null && rightFile != null,
                    onClick = {
                        try {
                            comparison = compareDocuments(requireNotNull(leftFile), requireNotNull(rightFile))
                            error = null
                        } catch (exception: Exception) {
                            comparison = null
                            error = exception.message ?: "Не удалось прочитать документы"
                        }
                    }
                ) { Text("Сравнить документы") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (comparison != null) {
                    SideBySideDiffView(requireNotNull(comparison), comparisonMode)
                } else {
                    Text("Выберите два файла в форматах PDF, TXT, DOC или DOCX")
                }
            }
        }
    }
}

@Composable
private fun DocumentChooser(title: String, file: File?, onChoose: () -> Unit) {
    Card(modifier = Modifier.width(320.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(file?.name ?: "Файл не выбран", maxLines = 1)
            Button(onClick = onChoose) { Text("Выбрать файл") }
        }
    }
}

@Composable
private fun SideBySideDiffView(texts: Pair<String, String>, mode: ComparisonMode) {
    val scrollState = rememberScrollState()
    val highlighted = remember(texts, mode) { createInlineDiff(texts.first, texts.second, mode) }
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Документ 1" to highlighted.first, "Документ 2" to highlighted.second).forEach { (title, text) ->
            Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    Text(text = text, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
        }
    }
}


private fun chooseFile(): File? {
    var selected: File? = null
    val dialog = object : FileDialog(null as Frame?, "Выберите документ", FileDialog.LOAD) {
        override fun setVisible(value: Boolean) {
            super.setVisible(value)
            if (value) selected = files.firstOrNull()
        }
    }
    dialog.isVisible = true
    return selected
}

private fun readDocument(file: File): List<String> = when (file.extension.lowercase()) {
    "txt" -> file.readText().lines()
    "pdf" -> Loader.loadPDF(file).use { PDFTextStripper().getText(it).lines() }
    "doc" -> HWPFDocument(file.inputStream()).use { WordExtractor(it).text.lines() }
    "docx" -> XWPFDocument(file.inputStream()).use { XWPFWordExtractor(it).text.lines() }
    else -> error("Неподдерживаемый формат: .${file.extension}")
}

private fun compareDocuments(left: File, right: File): Pair<String, String> =
    readDocument(left).joinToString("\n") to readDocument(right).joinToString("\n")

