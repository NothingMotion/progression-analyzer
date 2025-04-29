package com.nothingmotion.brawlprogressionanalyzer.util

import java.net.URLEncoder

object EnvUtils {
    fun encodeUrl(url: String): String{
        return URLEncoder.encode(url, "UTF-8")
    }
    fun decodeUrl(url: String): String{
        return URLEncoder.encode(url, "UTF-8")
    }
}