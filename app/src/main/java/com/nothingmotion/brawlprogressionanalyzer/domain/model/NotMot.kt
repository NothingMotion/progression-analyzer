package com.nothingmotion.brawlprogressionanalyzer.domain.model

import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID


data class Track(
    val name: String = "Progression-Analyzer",
    val uuid: UUID = UUID.randomUUID(),
    val joinedSince: Date = Date(),
    val version: Long? = 0,
    val versionString: String? = "",
    val date: Date = Date()
)
fun Track.toJson() : String{
    return GsonBuilder().create().toJson(this)
}
fun String.toTrack() : Track? {
    return GsonBuilder().create().fromJson(this,Track::class.java)
}
fun nowString() : String{
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTCC")
    return sdf.format(Date())
}