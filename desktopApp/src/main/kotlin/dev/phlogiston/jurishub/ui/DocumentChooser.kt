package dev.phlogiston.jurishub.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
internal fun DocumentChooser(
    title: String,
    file: File?,
    onChoose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(file?.name ?: "Файл не выбран", maxLines = 1)
            Button(onClick = onChoose) { Text("Выбрать файл") }
        }
    }
}
