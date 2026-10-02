package dev.phlogiston.jurishub

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

internal enum class ComparisonMode(val label: String) {
    LINES("По строкам"),
    WORDS("По словам целиком"),
    CHARACTERS("По символам")
}

internal fun createInlineDiff(left: String, right: String, mode: ComparisonMode): Pair<AnnotatedString, AnnotatedString> {
    val leftParts = tokenize(left, mode)
    val rightParts = tokenize(right, mode)
    val (leftChanged, rightChanged) = markChanges(leftParts, rightParts)

    fun annotated(parts: List<String>, changed: BooleanArray, color: Color) = buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            if (changed[index]) withStyle(SpanStyle(background = color)) { append(part) } else append(part)
        }
    }
    return annotated(leftParts, leftChanged, Color(0xFFFFCDD2)) to
        annotated(rightParts, rightChanged, Color(0xFFC8E6C9))
}

private fun markChanges(left: List<String>, right: List<String>): Pair<BooleanArray, BooleanArray> {
    val rows = left.size + 1
    val columns = right.size + 1
    val lcs = Array(rows) { IntArray(columns) }
    for (i in left.indices.reversed()) {
        for (j in right.indices.reversed()) {
            lcs[i][j] = if (left[i] == right[j]) lcs[i + 1][j + 1] + 1 else maxOf(lcs[i + 1][j], lcs[i][j + 1])
        }
    }
    val leftChanged = BooleanArray(left.size)
    val rightChanged = BooleanArray(right.size)
    var i = 0
    var j = 0
    while (i < left.size && j < right.size) {
        if (left[i] == right[j]) {
            i++
            j++
        } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
            leftChanged[i++] = true
        } else {
            rightChanged[j++] = true
        }
    }
    while (i < left.size) leftChanged[i++] = true
    while (j < right.size) rightChanged[j++] = true
    return leftChanged to rightChanged
}

private fun tokenize(text: String, mode: ComparisonMode): List<String> = when (mode) {
    ComparisonMode.LINES -> Regex("[^\\n]*\\n|[^\\n]+$").findAll(text).map { it.value }.toList()
    ComparisonMode.WORDS -> Regex("\\s+|[^\\s\\p{L}\\p{N}_]+|[\\p{L}\\p{N}_]+").findAll(text).map { it.value }.toList()
    ComparisonMode.CHARACTERS -> text.map(Char::toString)
}
