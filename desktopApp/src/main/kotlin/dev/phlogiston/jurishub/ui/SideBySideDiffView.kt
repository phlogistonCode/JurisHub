package dev.phlogiston.jurishub.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.phlogiston.jurishub.core.AlignedDiffRow
import dev.phlogiston.jurishub.core.createAlignedLineDiff

@Composable
internal fun SideBySideDiffView(
    texts: Pair<String, String>,
    highlighted: Pair<AnnotatedString, AnnotatedString>,
    fontName: String,
    wrapLines: Boolean
) {
    val horizontalScrollState = rememberScrollState()
    val rowHeights = remember(texts, highlighted, wrapLines) {
        mutableStateListOf<Int>().apply { repeat(createAlignedLineDiff(texts.first, texts.second).size) { add(0) } }
    }
    val alignedRows = remember(texts, highlighted) {
        val rows = createAlignedLineDiff(texts.first, texts.second)
        var leftIndex = 0
        var rightIndex = 0
        rows.map { row ->
            val leftLine = row.left?.let {
                val line = lineAt(highlighted.first, leftIndex++)
                line
            }
            val rightLine = row.right?.let {
                val line = lineAt(highlighted.second, rightIndex++)
                line
            }
            AlignedDiffRow(leftLine, rightLine)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("Документ 1" to alignedRows.map { it.left }, "Документ 2" to alignedRows.map { it.right }).forEach { (title, lines) ->
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F5F8))) {
                        Text(title, modifier = Modifier.fillMaxWidth().background(Color(0xFFE7EAF0)).padding(12.dp), style = MaterialTheme.typography.titleSmall)
                        HorizontalDivider(color = Color(0xFFD5D9E0))
                        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFAFBFC))) {
                            Column(modifier = if (wrapLines) Modifier.fillMaxWidth() else Modifier.horizontalScroll(horizontalScrollState)) {
                                lines.forEachIndexed { index, line ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 24.dp)
                                            .then(if (rowHeights[index] > 0) Modifier.height(with(androidx.compose.ui.platform.LocalDensity.current) { rowHeights[index].toDp() }) else Modifier)
                                            .onGloballyPositioned { coordinates ->
                                                val measured = coordinates.size.height
                                                if (measured > rowHeights[index]) rowHeights[index] = measured
                                            }
                                    ) {
                                        Text(
                                            if (line == null) "" else (if (title == "Документ 1") alignedRows.take(index + 1).count { it.left != null } else alignedRows.take(index + 1).count { it.right != null }).toString(),
                                            modifier = Modifier.width(52.dp).background(Color(0xFFEFF1F4)).padding(horizontal = 8.dp, vertical = 2.dp),
                                            color = Color(0xFF858B95), fontSize = 12.sp,
                                            fontFamily = resolveFont(fontName), lineHeight = 20.sp
                                        )
                                        if (line != null) Text(
                                            text = line,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontFamily = resolveFont(fontName), fontSize = 13.sp, lineHeight = 20.sp,
                                            softWrap = wrapLines
                                        ) else Spacer(modifier = Modifier.width(1.dp).height(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!wrapLines) {
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(horizontalScrollState),
                modifier = Modifier.fillMaxWidth().height(12.dp)
            )
        }
    }
}

private fun lineAt(text: AnnotatedString, index: Int): AnnotatedString {
    var start = 0
    repeat(index) {
        start = text.indexOf('\n', start).let { if (it < 0) text.length else it + 1 }
    }
    val end = text.indexOf('\n', start).let { if (it < 0) text.length else it }
    return text.subSequence(start, end)
}

@Preview
@Composable
private fun SideBySideDiffViewPreview() {
    SideBySideDiffView(
        Pair("asdaaaa", "dsaaaaaaa"),
        Pair(AnnotatedString("asddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasddddddasdddddd"), AnnotatedString("12312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456123124234234534561231242342345345612312423423453456")),
        "Monospace",
        true
    )
}
