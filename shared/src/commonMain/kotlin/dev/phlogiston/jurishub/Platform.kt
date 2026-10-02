package dev.phlogiston.jurishub

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform