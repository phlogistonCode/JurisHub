package dev.phlogiston.jurishub.core

internal enum class ComparisonMode(val label: String) {
    LINES("По строкам"),
    WORDS("По словам целиком"),
    CHARACTERS("По символам")
}
