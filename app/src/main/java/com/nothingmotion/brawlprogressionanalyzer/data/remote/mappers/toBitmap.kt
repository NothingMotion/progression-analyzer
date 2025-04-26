package com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers

import android.graphics.Bitmap
import android.graphics.BitmapFactory


fun  ByteArray.toBitmap(): Bitmap?{
    try {
     return  BitmapFactory.decodeByteArray(this, 0, this.size)
    }
    catch(e: OutOfMemoryError){
        return null
    }
    catch(e: Exception){
        return null
    }
}