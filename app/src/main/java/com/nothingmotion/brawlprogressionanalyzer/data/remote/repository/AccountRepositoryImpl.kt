package com.nothingmotion.brawlprogressionanalyzer.data.remote.repository

import android.database.sqlite.SQLiteAbortException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteException
import com.nothingmotion.brawlprogressionanalyzer.data.db.ApplicationDatabase
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.AccountEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.CacheEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.PlayerEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressEntity
import com.nothingmotion.brawlprogressionanalyzer.data.db.models.ProgressType
import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers.toAccount
import com.nothingmotion.brawlprogressionanalyzer.data.remote.mappers.toPlayer
import com.nothingmotion.brawlprogressionanalyzer.data.remote.model.APIHistory
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Account
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import com.nothingmotion.brawlprogressionanalyzer.util.DataErrorUtils
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Progress
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.AccountRepository
import com.nothingmotion.brawlprogressionanalyzer.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.Date
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(

) : AccountRepository {

    @Inject
    lateinit var api: ProgressionAnalyzerAPI

    @Inject
    lateinit var tokenManager: TokenManager
    @Inject
    lateinit var db: ApplicationDatabase
    override suspend fun getAccount(tag: String, token: String): Result<Account, DataError> {
        try {

            if (isValidCache("#$tag")) {
                Timber.tag("AccountRepositoryImpl").d("Cache is invalid, refreshing account")
                return getCachedAccount("#$tag")
            }
            val account = api.getAccount(tag, "Bearer $token").toAccount()

            try {
                val history = api.getAccountHistory(tag, 10, 0, "Bearer $token")
                account.history = history.history.map { it.toPlayer() }
            }catch(e:Exception){
                Timber.tag("AccountRepositoryImpl").e(e)

            }

            try {
                insertCachedAccount(account)
            }
            catch(e:Exception){
                Timber.tag("AccountRepositoryImpl").e(e)
                return Result.Error(DataErrorUtils.handleDatabaseException(e, "insertCachedAccount"))
            }
            return Result.Success(account)
        } catch (e: Exception) {
            Timber.tag("AccountRepositoryImpl").e(e)
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun getAccountHistory(
        tag: String,
        token: String,
        limit: Int?,
        offset: Int?
    ): Flow<Result<List<APIHistory>, DataError.NetworkError>> {
        return flow {
            try {
//                val token = tokenManager.getAccessToken("")
                emit(
                    Result.Success(
                        api.getAccountHistory(
                            tag,
                            limit,
                            offset,
                            "Bearer $token"
                        ).history
                    )
                )
            } catch (e: IOException) {
                emit(Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION))
            } catch (e: HttpException) {
                emit(
                    when (e.code()) {
                        400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                        401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                        403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                        429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                        500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                        else -> Result.Error(DataError.NetworkError.UNKNOWN)
                    }
                )
            } catch (e: Exception) {
                emit(Result.Error(DataError.NetworkError.UNKNOWN))
            }
        }
    }

    override suspend fun refreshAccount(
        tag: String,
        token: String
    ): Result<Account, DataError.NetworkError> {
        try {
//            val token = tokenManager.getAccessToken("")
            return Result.Success(api.refreshAccount(tag, "Bearer $token").toAccount())
        } catch (e: Exception) {
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    override suspend fun getAllAccounts(token: String): Flow<Result<List<Account>, DataError.NetworkError>> {
        return flow {
            try {
                val expiredAccounts = mutableListOf<String>()
                val caches = db.cacheDao().getAllCaches()
                val accounts = mutableListOf<Account>()

                caches.forEach {
                    if (it.validFor < Date()) {
                        expiredAccounts.add(it.playerTag)
                    }
                }
                getCachedAccounts().collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Timber.tag("AccountRepositoryImpl").e(result.error.name)
                            emit(Result.Error(DataError.NetworkError.UNKNOWN))
                        }

                        is Result.Loading -> {
                            Timber.tag("AccountRepositoryImpl").d("Loading accounts")
                        }

                        is Result.Success -> {
                            result.data.forEach { account ->
                                if (expiredAccounts.contains(account.account.tag)) {
                                    //
                                } else {
                                    accounts.add(account)
                                }
                            }
                        }
                    }
                }


                expiredAccounts.forEach {
                    withContext(Dispatchers.IO){
                    val account = api.refreshAccount(it, token)
                    accounts.add(account.toAccount())
                    db.cacheDao()
                        .insertCache(
                            CacheEntity(
                                id=0,
                                playerTag = it,
                                validFor = Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24),
                                createdAt = Date()
                            )
                        )
                }}.let {
                    emit(Result.Success(accounts.toList()))
                }

            } catch (e: IOException) {
                emit(Result.Error(DataError.NetworkError.NO_INTERNET_CONNECTION))
            } catch (e: HttpException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                emit(
                    when (e.code()) {
                        400 -> Result.Error(DataError.NetworkError.NETWORK_ERROR)
                        401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
                        403 -> Result.Error(DataError.NetworkError.FORBIDDEN)
                        429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
                        500 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
                        else -> Result.Error(DataError.NetworkError.UNKNOWN)
                    }
                )
            } catch (e: Exception) {
                Timber.tag("AccountRepositoryImpl").e(e)
                emit(Result.Error(DataError.NetworkError.UNKNOWN))
            }
        }
    }

    override suspend fun refreshAccounts(token: String): Result<Unit, DataError.NetworkError> {
        try {
//            val token = tokenManager.getAccessToken("")
            api.refreshAccounts("Bearer $token")
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Error(DataErrorUtils.handleHttpException(e))
        }
    }

    /**
     * Helper method to handle database exceptions and map them to appropriate DataError types
     */

    override suspend fun getCachedAccount(tag: String): Result<Account, DataError.DatabaseError> {
        try {
            val cache = db.accountDao().getAccountWithRelationsByPlayerTag(tag)

            cache?.let {
                val account = it.account
                val player = it.player.toDomain()

                // Get current progress from progressHistories
                val currentProgress = it.progressHistories
                    .firstOrNull { prog -> prog.type == ProgressType.CURRENT }
                    ?.toDomain() ?: Progress(
                        coins = 0,
                        powerPoints = 0,
                        credits = 0,
                        gadgets = 0,
                        starPowers = 0,
                        gears = 0,
                        brawlers = 0,
                        averageBrawlerPower = 0,
                        averageBrawlerTrophies = 0,
                        isBoughtPass = false,
                        isBoughtPassPlus = false,
                        isBoughtRankedPass = false,
                        duration = Date()
                    )
                    
                val previousProgress =
                    it.progressHistories.filter { prog -> prog.type == ProgressType.PREVIOUS }
                        .map { prog -> prog.toDomain() }
                        
                return Result.Success(
                    Account(
                        account = player,
                        currentProgress = currentProgress,
                        previousProgresses = previousProgress,
                        futureProgresses = emptyList(),
                        history = emptyList(),
                        updatedAt = account.updatedAt,
                        createdAt = account.createdAt
                    )
                )
            } ?: run {
                return Result.Error(DataError.DatabaseError.NO_DATA)
            }
        } catch (e: SQLiteDiskIOException) {
            Timber.tag("AccountRepositoryImpl").e(e)
            return Result.Error(DataError.DatabaseError.DISK_IO)
        } catch (e: SQLiteAbortException) {
            Timber.tag("AccountRepositoryImpl").e(e)
            return Result.Error(DataError.DatabaseError.ABORT_TRANSACTION)
        } catch (e: SQLiteConstraintException) {
            Timber.tag("AccountRepositoryImpl").e(e)
            return Result.Error(DataError.DatabaseError.DATABASE_CONSTRAINT)
        } catch (e: SQLiteException) {
            Timber.tag("AccountRepositoryImpl").e(e)
            return Result.Error(DataError.DatabaseError.DATABASE_ERROR)
        } catch (e: Exception) {
            return Result.Error(DataErrorUtils.handleDatabaseException(e, "getCachedAccount"))
        }
    }

    override suspend fun getCachedAccounts(): Flow<Result<List<Account>, DataError.DatabaseError>> {
        return flow {
            try {
                val accounts = mutableListOf<Account>()
                db.cacheDao().getAllCaches().forEach {
                    db.accountDao().getAccountWithRelationsByPlayerTag(it.playerTag)
                        ?.let { account ->
                            val player = account.player.toDomain()

                            // Get current progress from progressHistories
                            val currentProgress = account.toDomain().currentProgress
                                
                            val previousProgress =
                                account.progressHistories.filter { prog -> prog.type == ProgressType.PREVIOUS }
                                    .map { prog -> prog.toDomain() }
                                    
                            accounts.add(
                                Account(
                                    account = player,
                                    currentProgress = currentProgress,
                                    previousProgresses = previousProgress,
                                    futureProgresses = emptyList(),
                                    history = emptyList(),
                                    updatedAt = account.account.updatedAt,
                                    createdAt = account.account.createdAt
                                )
                            )
                        }
                }.let {
                    emit(Result.Success(accounts))
                }
            } catch (e: SQLiteDiskIOException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                emit(Result.Error(DataError.DatabaseError.DISK_IO))
            } catch (e: SQLiteAbortException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                emit(Result.Error(DataError.DatabaseError.ABORT_TRANSACTION))
            } catch (e: SQLiteConstraintException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                emit(Result.Error(DataError.DatabaseError.DATABASE_CONSTRAINT))
            } catch (e: SQLiteException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                emit(Result.Error(DataError.DatabaseError.DATABASE_ERROR))
            } catch (e: Exception) {
                emit(Result.Error(DataErrorUtils.handleDatabaseException(e, "getCachedAccounts")))
            }
        }
    }

    override suspend fun insertCachedAccount(account: Account): Result<Unit, DataError.DatabaseError> {
        return withContext(Dispatchers.IO) {
            try {
                // Insert the account first to establish the primary entity
                db.accountDao().insertAccount(AccountEntity(
                    playerTag = account.account.tag,
                    createdAt = account.createdAt,
                    updatedAt = account.updatedAt
                ))

                // Insert the player entity second
                db.playerDao().insertPlayer(PlayerEntity.fromDomain(account.account))

                // Insert the cache entry 
                db.cacheDao().insertCache(
                    com.nothingmotion.brawlprogressionanalyzer.data.db.models.CacheEntity(
                        id=0,
                        playerTag = account.account.tag,
                        createdAt = Date(System.currentTimeMillis()),
                        validFor = Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)
                    )
                )
                
                // Insert current progress
                db.progressDao().insertProgress(
                    ProgressEntity.fromDomain(account.currentProgress,
                        account.account.tag).apply {
                        type = ProgressType.CURRENT
                    }
                )
                
                // Insert previous progresses if any
                account.previousProgresses?.map {
                    ProgressEntity.fromDomain(it, account.account.tag).apply {
                        type = ProgressType.PREVIOUS
                    }
                }?.let { progress ->
                    db.progressDao().insertProgresses(
                        progress
                    )
                }
                
                return@withContext Result.Success(Unit)
            } catch (e: SQLiteDiskIOException) {
                Timber.tag("AccountRepositoryImpl").e("Disk I/O error during insert: ${e.message}", e)
                return@withContext Result.Error(DataError.DatabaseError.DISK_IO)
            } catch (e: SQLiteAbortException) {
                Timber.tag("AccountRepositoryImpl").e("Transaction aborted during insert: ${e.message}", e)
                return@withContext Result.Error(DataError.DatabaseError.ABORT_TRANSACTION)
            } catch (e: SQLiteConstraintException) {
                Timber.tag("AccountRepositoryImpl").e("Constraint violation during insert: ${e.message}", e)
                if (e.message?.contains("UNIQUE constraint failed") == true) {
                    return@withContext Result.Error(DataError.DatabaseError.DUPLICATE_ENTRY)
                } else if (e.message?.contains("FOREIGN KEY constraint failed") == true) {
                    // Try to provide more context about which entity caused the foreign key violation
                    val errorMessage = "Foreign key constraint failed. Check if referenced entities exist: ${e.message}"
                    Timber.tag("AccountRepositoryImpl").e(errorMessage)
                    return@withContext Result.Error(DataError.DatabaseError.DATABASE_CONSTRAINT)
                } else {
                    return@withContext Result.Error(DataError.DatabaseError.DATABASE_CONSTRAINT)
                }
            } catch (e: SQLiteException) {
                Timber.tag("AccountRepositoryImpl").e("SQLite error during insert: ${e.message}", e)
                return@withContext Result.Error(DataError.DatabaseError.DATABASE_ERROR)
            } catch (e: Exception) {
                return@withContext Result.Error(DataErrorUtils.handleDatabaseException(e, "insertCachedAccount"))
            }
        }
    }

    override suspend fun insertCachedAccounts(accounts: List<Account>): Result<Unit, DataError.DatabaseError> {
        return withContext(Dispatchers.IO) {
            try {
                accounts.forEach { account ->
                    val result = insertCachedAccount(account)
                    if (result is Result.Error) {
                        // If any insert fails, return the error
                        Timber.tag("AccountRepositoryImpl").e("Failed to insert account ${account.account.tag}: ${result.error}")
                        return@withContext result
                    }
                }
                return@withContext Result.Success(Unit)
            } catch (e: SQLiteDiskIOException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                return@withContext Result.Error(DataError.DatabaseError.DISK_IO)
            } catch (e: SQLiteAbortException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                return@withContext Result.Error(DataError.DatabaseError.ABORT_TRANSACTION)
            } catch (e: SQLiteConstraintException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                return@withContext Result.Error(DataError.DatabaseError.DATABASE_CONSTRAINT)
            } catch (e: SQLiteException) {
                Timber.tag("AccountRepositoryImpl").e(e)
                return@withContext Result.Error(DataError.DatabaseError.DATABASE_ERROR)
            } catch (e: Exception) {
                return@withContext Result.Error(DataErrorUtils.handleDatabaseException(e, "insertCachedAccounts"))
            }
        }
    }

    override suspend fun deleteCachedAccount(tag: String): Result<Unit, DataError.DatabaseError> {
        return withContext(Dispatchers.IO) {
            try {
                // Using transaction to ensure atomicity
                    db.accountDao().deleteAccountWithRelationsByPlayerTag(tag)
                    db.cacheDao().deleteCache(tag)
                return@withContext Result.Success(Unit)
            } catch (e: SQLiteDiskIOException) {
                Timber.tag("AccountRepositoryImpl").e("Disk I/O error during delete: ${e.message}", e)
                return@withContext Result.Error(DataError.DatabaseError.DISK_IO)
            } catch (e: SQLiteAbortException) {
                Timber.tag("AccountRepositoryImpl").e("Transaction aborted during delete: ${e.message}", e)
                return@withContext Result.Error(DataError.DatabaseError.ABORT_TRANSACTION)
            } catch (e: SQLiteConstraintException) {
                Timber.tag("AccountRepositoryImpl").e("Constraint violation during delete: ${e.message}", e)
                return@withContext Result.Error(DataError.DatabaseError.DATABASE_CONSTRAINT)
            } catch (e: SQLiteException) {
                Timber.tag("AccountRepositoryImpl").e("SQLite error during delete: ${e.message}", e)
                return@withContext Result.Error(DataError.DatabaseError.DATABASE_ERROR)
            } catch (e: Exception) {
                return@withContext Result.Error(DataErrorUtils.handleDatabaseException(e, "deleteCachedAccount"))
            }
        }
    }

    override suspend fun isValidCache(tag: String): Boolean {
        return try {
            db.cacheDao().getCacheByPlayerTag(tag)?.let {
                return it.validFor > Date()
            } ?: run {
                return@run false
            }
        } catch (e: Exception) {
            Timber.tag("AccountRepositoryImpl").e("Error checking cache validity: ${e.message}", e)
            false // Return false as a safe default if there's any database error
        }
    }
}
