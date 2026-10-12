package dev.phlogiston.jurishub.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SideBySideDiffView(
    texts: Pair<String, String>,
    highlighted: Pair<AnnotatedString, AnnotatedString>,
    fontName: String,
    wrapLines: Boolean
) {
    val horizontalScrollState = rememberScrollState()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                Triple("Документ 1", texts.first, highlighted.first),
                Triple("Документ 2", texts.second, highlighted.second)
            ).forEach { (title, source, markedText) ->
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F5F8))) {
                        Text(title, modifier = Modifier.fillMaxWidth().background(Color(0xFFE7EAF0)).padding(12.dp), style = MaterialTheme.typography.titleSmall)
                        HorizontalDivider(color = Color(0xFFD5D9E0))
                        val lines = markedText.text.split('\n')
                        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFAFBFC))) {
                            Column(
                                modifier = if (wrapLines) Modifier.fillMaxWidth() else Modifier.horizontalScroll(horizontalScrollState)
                            ) {
                                lines.forEachIndexed { index, line ->
                                    Row(modifier = Modifier.heightIn(min = 24.dp)) {
                                        Text(
                                            (index + 1).toString(),
                                            modifier = Modifier.width(52.dp).background(Color(0xFFEFF1F4)).padding(horizontal = 8.dp, vertical = 2.dp),
                                            color = Color(0xFF858B95), fontSize = 12.sp,
                                            fontFamily = resolveFont(fontName), lineHeight = 20.sp
                                        )
                                        val start = lines.take(index).sumOf { it.length + 1 }
                                        val end = start + line.length
                                        val styledLine = AnnotatedString.Builder().apply { append(markedText.subSequence(start, end)) }.toAnnotatedString()
                                        Text(
                                            text = styledLine,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontFamily = resolveFont(fontName), fontSize = 13.sp, lineHeight = 20.sp,
                                            softWrap = wrapLines
                                        )
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
