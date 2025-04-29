package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import com.nothingmotion.brawlprogressionanalyzer.data.db.ApplicationDatabase
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.toDomain
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.toEntity
import com.nothingmotion.brawlprogressionanalyzer.data.remote.BrawlifyApi
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIPlayerIcon
import com.nothingmotion.brawlprogressionanalyzer.domain.model.BrawlerData
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class BrawlerRepositoryImpl @Inject constructor() : BrawlerRepository {
    @Inject lateinit var api : BrawlifyApi
    @Inject lateinit var db: ApplicationDatabase
    override suspend fun getBrawler(id: Long): Result<BrawlerData,DataError.NetworkError> {
        try {
            db.brawlDataDao().getBrawlerDataById(id)?.let{
                Timber.tag("BrawlerRepositoryImpl").d("Brawler data found in cache")
                return Result.Success(it.toDomain())
            } ?: run {
                val brawler = api.getBrawler(id)
                Timber.tag("BrawlerRepositoryImpl").d("Brawler data not found in cache")
                db.brawlDataDao().insertBrawlerData(brawler.toEntity())
                return Result.Success(brawler)

            }
        }

        catch(e: Exception){
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun getBrawlers(): Flow<Result<List<BrawlerData>,DataError.NetworkError>> {
        return flow {
            try {




                db.brawlDataDao().getAllBrawlerData()?.let{
                    Timber.tag("BrawlerRepositoryImpl").d("Brawler data found in cache")
                    if(it.isNotEmpty())
                        emit(Result.Success(it.map { it.toDomain() }))
                    else
                        null
                } ?: run {
                    Timber.tag("BrawlerRepositoryImpl").d("Brawler data not found in cache")
                    val brawlers = api.getBrawlers().list
                    db.brawlDataDao().insertBrawlerDataList(brawlers.map { it.toEntity() })
                    emit(Result.Success(brawlers))
                }
            }

            catch(e:Exception){
                emit(Result.Error(DataErrorUtils.handleHttpException(e)))
            }
        }
    }

    override suspend fun getIcon(id: Long): Result<APIPlayerIcon, DataError.NetworkError> {
        try {
            val icons = mutableListOf<APIPlayerIcon>()
            api.getIcons().player.entries.forEach { (key,icon)-> icons.add(icon)}
            val icon = icons.find { it->it.id == id }
            icon?.let{
                return Result.Success(it)
            }
            return Result.Error(DataError.NetworkError.NOT_FOUND)
        }
        catch(e:Exception){

            Timber.tag("BrawlerRepositoryImpl").e(e)
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun getGadget(id: Long): Result<ByteArray, DataError.NetworkError> {
        try {
            return Result.Success(api.getGadget("$id.png" ))
        }
        catch(e: IOException){
            return Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        }
        catch(e: HttpException){
            return when (e.code()){
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> Result.Error(DataError.NetworkError.UNKNOWN)
            }
        }
        catch(e: Exception){
            Timber.tag("BrawlerRepositoryImpl").e(e)
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun getStarPower(id: Long): Result<ByteArray, DataError.NetworkError> {
        try {
            return Result.Success(api.getStarPower("$id.png"))
        }
        catch(e: IOException){
            return Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        }
        catch(e: HttpException){
            return when (e.code()){
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> Result.Error(DataError.NetworkError.UNKNOWN)
            }
        }
        catch(e: Exception){
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun getGear(id: Long): Result<ByteArray, DataError.NetworkError> {
        try {
            return Result.Success(api.getGear("$id.png"))
        }
        catch(e: IOException){
            return Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        }
        catch(e: HttpException){
            return when (e.code()){
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> Result.Error(DataError.NetworkError.UNKNOWN)
            }
        }
        catch(e: Exception){
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }

    override suspend fun getTier(id: Long): Result<ByteArray, DataError.NetworkError> {
        try {
            return Result.Success(api.getTier("${id.toInt()}.png"))
        }
        catch(e: IOException){
            return Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION)
        }
        catch(e: HttpException){
            return when (e.code()){
                400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                else -> Result.Error(DataError.NetworkError.UNKNOWN)
            }
        }
        catch(e: Exception){
            return Result.Error(DataError.NetworkError.UNKNOWN)
        }
    }
}