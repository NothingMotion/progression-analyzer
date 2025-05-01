package com.nothingmotion.brawlprogressionanalyzer.domain.model


enum class RarityData {
    COMMON,
    RARE,
    SUPER_RARE,
    EPIC,
    MYTHIC,
    LEGENDARY
}

fun RarityData.getRarityColor(): Int {
    return when (this) {
        RarityData.COMMON -> 0xFF00FF00.toInt() // Green
        RarityData.RARE -> 0xFF0000FF.toInt() // Blue
        RarityData.SUPER_RARE -> 0xFFFF0000.toInt() // Red
        RarityData.EPIC -> 0xFFFF00FF.toInt() // Magenta
        RarityData.MYTHIC -> 0xFFFFFF00.toInt() // Yellow
        RarityData.LEGENDARY -> 0xFFFFA500.toInt() // Orange
    }
}
fun RarityData.getPersianName(): String {
    return when (this) {
        RarityData.COMMON -> "معمولی"
        RarityData.RARE -> "نادر"
        RarityData.SUPER_RARE -> "فوق نادر"
        RarityData.EPIC -> "حماسی"
        RarityData.MYTHIC -> "افسانه ای"
        RarityData.LEGENDARY -> "افسانه ای"
    }
}