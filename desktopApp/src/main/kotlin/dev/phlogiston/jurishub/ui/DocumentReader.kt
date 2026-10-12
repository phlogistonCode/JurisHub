package dev.phlogiston.jurishub.ui

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal fun compareDocuments(left: File, right: File, onProgress: (Float) -> Unit): Pair<String, String> {
    onProgress(0.1f)
    val leftText = readCachedDocument(left)
    onProgress(0.5f)
    val rightText = readCachedDocument(right)
    onProgress(0.9f)
    return leftText to rightText
}

private val documentCache = ConcurrentHashMap<String, CachedDocument>()

private data class CachedDocument(val modifiedAt: Long, val text: String)

private fun readCachedDocument(file: File): String {
    val key = file.absolutePath
    val modifiedAt = file.lastModified()
    val cached = documentCache[key]
    if (cached?.modifiedAt == modifiedAt) return cached.text
    val text = readDocument(file).joinToString("\n")
    documentCache[key] = CachedDocument(modifiedAt, text)
    return text
}

private fun readDocument(file: File): List<String> = when (file.extension.lowercase()) {
    "txt" -> file.readText().lines()
    "pdf" -> Loader.loadPDF(file).use { PDFTextStripper().getText(it).lines() }
    "doc" -> HWPFDocument(file.inputStream()).use { WordExtractor(it).text.lines() }
    "docx" -> XWPFDocument(file.inputStream()).use { XWPFWordExtractor(it).text.lines() }
    else -> error("Неподдерживаемый формат: .${file.extension}")
}
