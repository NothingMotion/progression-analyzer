package com.nothingmotion.brawlprogressionanalyzer.domain.model

import com.google.gson.GsonBuilder
import java.util.Date
import java.util.UUID


data class Track(
    val name: String = "Progression-Analyzer",
    val uuid: UUID = UUID.randomUUID(),
    val joinedSince: Date = Date(),
    val version: Long? = 0,
    val versionString: String? = "",

)
fun Track.toJson() : String{
    return GsonBuilder().create().toJson(this)
}
fun String.toTrack() : Track? {
    return GsonBuilder().create().fromJson(this,Track::class.java)
}