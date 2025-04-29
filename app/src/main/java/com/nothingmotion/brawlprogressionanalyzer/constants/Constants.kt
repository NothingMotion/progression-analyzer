package com.nothingmotion.brawlprogressionanalyzer.constants

import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.util.EnvUtils

object Constants {
    val PROGRESSION_ANALYZER_API = EnvUtils.decodeUrl(BuildConfig.PROGRESSION_ANALYZER_API)
}