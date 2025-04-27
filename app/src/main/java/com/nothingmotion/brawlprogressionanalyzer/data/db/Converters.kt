package com.nothingmotion.brawlprogressionanalyzer.data.db

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressType
import com.nothingmotion.brawlprogressionanalyzer.domain.model.AbilityDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Brawler
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Gadget
import com.nothingmotion.brawlprogressionanalyzer.domain.model.GadgetDataNinja
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Gear
import com.nothingmotion.brawlprogressionanalyzer.domain.model.StarPower
import java.text.SimpleDateFormat
import java.util.Date

class Converters {
    @TypeConverter
    fun fromBrawlersList(value: List<Brawler>): String?{
        return GsonBuilder().create().toJson(value)
    }

    @TypeConverter
    fun toBrawlersList(value: String): List<Brawler>?{
        return GsonBuilder().create().fromJson(value,Array<Brawler>::class.java)?.toList()
    }
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
    
    @TypeConverter
    fun fromProgressType(value: ProgressType): String {
        return value.name
    }
    
    @TypeConverter
    fun toProgressType(value: String): ProgressType {
        return ProgressType.valueOf(value)
    }

    @TypeConverter
    fun fromGadgetDataNinjaList(value: List<AbilityDataNinja>):String {
        return GsonBuilder().create().toJson(value)
    }
    @TypeConverter
    fun toGadgetDataNinjaList(value: String): List<AbilityDataNinja> {
        return GsonBuilder().create().fromJson(value, Array<AbilityDataNinja>::class.java).toList()
    }
}