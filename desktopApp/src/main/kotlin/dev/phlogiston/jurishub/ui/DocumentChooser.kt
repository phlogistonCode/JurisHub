package dev.phlogiston.jurishub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun DocumentChooser(
    title: String,
    file: File?,
    onChoose: () -> Unit,
    onFileDropped: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragOver by remember { mutableStateOf(false) }
    val target = remember(onFileDropped) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isDragOver = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDragOver = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val transferable = event.awtTransferable
                if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false
                val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                val droppedFile = files?.firstOrNull() as? File ?: return false
                if (droppedFile.extension.lowercase() !in setOf("pdf", "txt", "doc", "docx")) return false
                onFileDropped(droppedFile)
                isDragOver = false
                return true
            }
        }
    }
    Card(
        modifier = modifier
            .border(
                BorderStroke(2.dp, if (isDragOver) MaterialTheme.colorScheme.primary else Color.Transparent),
                MaterialTheme.shapes.medium
            )
            .dragAndDropTarget(
                shouldStartDragAndDrop = { it.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) },
                target = target
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(file?.name ?: "Перетащите PDF, TXT, DOC или DOCX сюда", maxLines = 1)
            Button(onClick = onChoose) { Text("Выбрать файл") }
        }
    }
}
