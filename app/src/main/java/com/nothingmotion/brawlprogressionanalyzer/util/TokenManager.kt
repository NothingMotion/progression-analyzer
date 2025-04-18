package com.nothingmotion.brawlprogressionanalyzer.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.data.remote.ProgressionAnalyzerAPI
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.TokenRepository
import java.util.Date
import javax.inject.Inject

class TokenManager constructor(){
    @Inject lateinit var repository: TokenRepository
    @Inject lateinit var prefsManager: PreferencesManager
    fun generate(userId: String) : String{
        val algorithm = Algorithm.HMAC256(BuildConfig.APPLICATION_FRONTEND_API_KEY)
        val builder = JWT.create()
            .withIssuer("progression-analyzer")
            .withClaim("userId",userId)
            .withIssuedAt(java.util.Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 day
        return builder.sign(algorithm)
    }
    fun decode(token: String) : DecodedJWT{
        val algorithm = Algorithm.HMAC256(BuildConfig.APPLICATION_FRONTEND_API_KEY)
        return JWT.require(algorithm)
            .withIssuer("progression-analyzer")
            .build()
            .verify(token)
    }
    suspend fun generateAccessToken(frontEndToken: String): String{
        return when (val result = repository.getAccessToken(frontEndToken)){
            is Result.Error -> ""
            is Result.Loading -> ""
            is Result.Success -> result.data.token
        }
    }
    suspend fun validateAccessToken(accessToken: String) : Boolean {
        return when(val result = repository.validateAccessToken(accessToken)){
            is Result.Error -> false
            is Result.Loading -> false
            is Result.Success -> true
        }
    }
    suspend fun getAccessToken(userId: String) : String{
        // Check if access token already exists
        val accessToken = prefsManager.accessToken
        val frontEndToken = prefsManager.frontEndToken
        if (accessToken != null){
            // Validate access token and if was ok return it
            if(validateAccessToken(accessToken)) return accessToken
            else {
                // If access token is not valid, generate a new one
                // Check if frontEndToken exists
                if (frontEndToken != null) {
                    // Decode the frontEndToken to check if it is valid
                    val decodedToken = decode(frontEndToken)
                    val decodedUserId = decodedToken.getClaim("userId").asString()
                    if (decodedUserId != userId) {
                        // If the user ID does not match, generate a new frontEndToken
                        val newFrontEndToken = generate(userId)
                        prefsManager.frontEndToken = newFrontEndToken
                    }
                } else {
                    // If frontEndToken does not exist, generate a new one
                    prefsManager.frontEndToken = generate(userId)
                }
                val newAccessToken = generateAccessToken(frontEndToken!!)
                prefsManager.accessToken = newAccessToken
                return newAccessToken
            }
        }else {
            // If access token does not exist, generate a new one
            // Check if frontEndToken exists
            if (frontEndToken != null) {
                // Decode the frontEndToken to check if it is valid
                val decodedToken = decode(frontEndToken)
                val decodedUserId = decodedToken.getClaim("userId").asString()
                if (decodedUserId != userId) {
                    // If the user ID does not match, generate a new frontEndToken
                    val newFrontEndToken = generate(userId)
                    prefsManager.frontEndToken = newFrontEndToken
                }
            } else {
                // If frontEndToken does not exist, generate a new one
                prefsManager.frontEndToken = generate(userId)

            }
            val newAccessToken = generateAccessToken(frontEndToken!!)
            prefsManager.accessToken = newAccessToken
            return newAccessToken
        }
    }
}