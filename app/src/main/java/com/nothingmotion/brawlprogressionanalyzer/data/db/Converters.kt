package com.nothingmotion.brawlprogressionanalyzer.data.db

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Gadget
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Gear
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPower
import java.text.SimpleDateFormat
import java.util.Date

class Converters {
    @TypeConverter
    fun fromGadgetsList(value: List<Gadget>?): String? {

        return GsonBuilder().create().toJson(value)
    }

    @TypeConverter
    fun toGadgetsList(value: String?): List<Gadget>? {
        return GsonBuilder().create().fromJson(value, Array<Gadget>::class.java)?.toList()
    }

    @TypeConverter
    fun fromStarPowersList(value: List<StarPower>?): String? {
        return GsonBuilder().create().toJson(value)
    }

    @TypeConverter
    fun toStarPowersList(value: String?): List<StarPower>? {
        return GsonBuilder().create().fromJson(value, Array<StarPower>::class.java)?.toList()
    }

    @TypeConverter
    fun fromGearsList(value: List<Gear>?): String? {
        return GsonBuilder().create().toJson(value)
    }

    @TypeConverter
    fun toGearsList(value: String?): List<Gear>? {
        return GsonBuilder().create().fromJson(value, Array<Gear>::class.java)?.toList()
    }


    @TypeConverter
    fun fromDate(value: Date): String? {
        val sdf = SimpleDateFormat.getDateInstance()
        return sdf.format(value)
    }

    @TypeConverter
    fun toDate(value: String): Date? {
        val sdf = SimpleDateFormat.getDateInstance()
        return sdf.parse(value)
    }
}