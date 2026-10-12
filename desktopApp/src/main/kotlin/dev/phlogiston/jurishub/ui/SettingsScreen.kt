package dev.phlogiston.jurishub.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SettingsScreen(selectedFont: String, onFontSelected: (String) -> Unit, wrapLines: Boolean, onWrapLinesChanged: (Boolean) -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Настройки", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onBack) { Text("Назад") }
        }
        Text("Шрифт отображаемых документов", style = MaterialTheme.typography.titleMedium)
        Text("Перенос строк", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = wrapLines, onClick = { onWrapLinesChanged(true) })
                Text("Включён")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !wrapLines, onClick = { onWrapLinesChanged(false) })
                Text("Выключен (горизонтальная прокрутка)")
            }
        }
        listOf("Monospace", "Sans Serif", "Serif").forEach { font ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(font, fontFamily = resolveFont(font), fontSize = 16.sp)
                RadioButton(selected = selectedFont == font, onClick = { onFontSelected(font) })
            }
        }
    }
}

internal fun resolveFont(name: String): FontFamily = when (name) {
    "Sans Serif" -> FontFamily.SansSerif
    "Serif" -> FontFamily.Serif
    else -> FontFamily.Monospace
}
