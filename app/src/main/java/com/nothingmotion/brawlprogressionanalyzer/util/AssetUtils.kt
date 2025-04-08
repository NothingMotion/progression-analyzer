package com.nothingmotion.brawlprogressionanalyzer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Utility class to handle asset operations
 */
object AssetUtils {

    /**
     * loads image from assets directory and returns bitmap
     * @param context Application context
     * @param fileName filename to be load
     */
    fun loadImage(context: Context, fileName: String): Bitmap? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Loads an image from assets asynchronously
     * @param context Application context
     * @param fileName Name of the file in assets folder (with path if in subfolder)
     * @return Bitmap or null if loading failed
     */
    suspend fun loadImageAsync(context: Context, fileName: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext loadImage(context, fileName)
    }
    
    /**
     * Lists all files in an assets directory
     * @param context Application context
     * @param directory Directory path within assets
     * @return Array of file names or empty array if directory doesn't exist
     */
    fun listAssetFiles(context: Context, directory: String): Array<String> {
        return try {
            context.assets.list(directory) ?: emptyArray()
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyArray()
        }
    }
} 