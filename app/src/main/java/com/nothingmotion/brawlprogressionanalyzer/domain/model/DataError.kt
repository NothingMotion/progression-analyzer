package com.nothingmotion.brawlprogressionanalyzer.domain.model


sealed interface DataError : Error {
    enum class NetworkError : DataError {
        NO_INTERNET_CONNECTION,
        NETWORK_ERROR,
        NOT_FOUND,
        TOO_MANY_REQUESTS,
        SERVER_ERROR,
        TIMEOUT,
        UNKNOWN,
        SSL_EXCEPTION,
        UNAUTHORIZED,


        BAD_REQUEST,
        UNPROCESSABLE_ENTITY,
        FORBIDDEN
    }




    enum class DatabaseError: DataError {
        NO_DATA,
        FAILED_TO_SAVE,
        DATABASE_ERROR,
        UNKNOWN,
        DUPLICATE_ENTRY,
        ABORT_TRANSACTION,
        DISK_IO,

        DATABASE_CONSTRAINT
    }
}