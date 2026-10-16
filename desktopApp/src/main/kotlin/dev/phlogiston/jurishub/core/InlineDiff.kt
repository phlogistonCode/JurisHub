package dev.phlogiston.jurishub.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

internal data class AlignedDiffRow(val left: AnnotatedString?, val right: AnnotatedString?)

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

internal fun createAlignedLineDiff(left: String, right: String): List<AlignedDiffRow> {
    fun splitLines(text: String) = if (text.isEmpty()) emptyList() else text.split('\n')
    val leftLines = splitLines(left)
    val rightLines = splitLines(right)
    val rows = leftLines.size + 1
    val columns = rightLines.size + 1
    val lcs = Array(rows) { IntArray(columns) }
    for (i in leftLines.indices.reversed()) {
        for (j in rightLines.indices.reversed()) {
            lcs[i][j] = if (leftLines[i] == rightLines[j]) lcs[i + 1][j + 1] + 1
            else maxOf(lcs[i + 1][j], lcs[i][j + 1])
        }
    }

    val aligned = mutableListOf<AlignedDiffRow>()
    var i = 0
    var j = 0
    while (i < leftLines.size || j < rightLines.size) {
        if (i < leftLines.size && j < rightLines.size && leftLines[i] == rightLines[j]) {
            aligned += AlignedDiffRow(AnnotatedString(leftLines[i++]), AnnotatedString(rightLines[j++]))
            continue
        }

        val removed = mutableListOf<String>()
        val added = mutableListOf<String>()
        while (i < leftLines.size && j < rightLines.size && leftLines[i] != rightLines[j]) {
            if (lcs[i + 1][j] >= lcs[i][j + 1]) removed += leftLines[i++] else added += rightLines[j++]
        }
        while (i < leftLines.size && (j == rightLines.size || lcs[i + 1][j] >= lcs[i][j + 1])) removed += leftLines[i++]
        while (j < rightLines.size && (i == leftLines.size || lcs[i][j + 1] > lcs[i + 1][j])) added += rightLines[j++]

        repeat(maxOf(removed.size, added.size)) { index ->
            aligned += AlignedDiffRow(
                removed.getOrNull(index)?.let(::AnnotatedString),
                added.getOrNull(index)?.let(::AnnotatedString)
            )
        }
    }
    return aligned
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
