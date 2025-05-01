package com.nothingmotion.brawlprogressionanalyzer.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.nothingmotion.brawlprogressionanalyzer.BuildConfig
import com.nothingmotion.brawlprogressionanalyzer.data.PreferencesManager
import com.nothingmotion.brawlprogressionanalyzer.domain.model.Result
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.TokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class TokenManager @Inject constructor(){
    @Inject lateinit var repository: TokenRepository
    @Inject lateinit var prefsManager: PreferencesManager

    private val _accessTokenState = MutableStateFlow(AccessTokenState())
    val accessTokenState get()= _accessTokenState

    fun generate(userId: String) : String{
        val algorithm = Algorithm.HMAC256(BuildConfig.APPLICATION_FRONTEND_API_KEY)
        val builder = JWT.create()
            .withIssuer("progression-analyzer")
            .withClaim("userId",userId)
            .withIssuedAt(java.util.Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 day
        return builder.sign(algorithm)
    }
    fun decode(token: String) : DecodedJWT?{
        val algorithm = Algorithm.HMAC256(BuildConfig.APPLICATION_FRONTEND_API_KEY)
        return try {
            JWT.require(algorithm)
            .withIssuer("progression-analyzer")


//            .acceptExpiresAt(1000 * 60 * 60 * 24)
            .build()
            .verify(token)

        }
        catch(e: Exception){
            null
        }
    }
    suspend fun generateAccessToken(frontEndToken: String): String?{
        Timber.tag("TokenManager").d("Frontend Token is: ${frontEndToken}")
        return when (val result = repository.getAccessToken(frontEndToken)){
            is Result.Error -> {
                _accessTokenState.update { it.copy(success= null,error= result.error.name, loading = false) }
                Timber.tag("TokenManager").e(result.error.name); null
            }
            is Result.Loading ->{
                null
            }
            is Result.Success -> {
                Timber.tag("TokenManager").d( result.data.response.token?: "null");
                result.data.response.token
            }
        }
    }
    suspend fun validateAccessToken(accessToken: String) : Boolean {
        return when(val result = repository.validateAccessToken(accessToken)){
            is Result.Error -> false
            is Result.Loading -> false
            is Result.Success -> true
        }
    }
    suspend fun collectAccessToken(userId: String){
        // Check if access token already exists
        val accessToken = prefsManager.accessToken
        var frontEndToken = prefsManager.frontEndToken
        if (accessToken != null && accessToken != ""){
            // Validate access token and if was ok return it
            if(validateAccessToken(accessToken)) {
                Timber.tag("TokenManager").d("Access token is valid: $accessToken")
                _accessTokenState.update { it.copy(success= accessToken,error=null,loading= false) }
                return
            }
            else {
                Timber.tag("TokenManager").d("Access token is not valid: $accessToken")
                // If access token is not valid, generate a new one
                // Check if frontEndToken exists
                if (frontEndToken != null && frontEndToken != "") {
                    Timber.tag("TokenManager").d("FrontEnd token is not null")

                    // Decode the frontEndToken to check if it is valid
                    val decodedToken = decode(frontEndToken)
                    val decodedUserId = decodedToken?.getClaim("userId")?.asString()

                    val expirationDate = decodedToken?.expiresAt
                    Timber.tag("TokenManager").d("expiration date is: $expirationDate")
                    if (decodedToken == null || expirationDate == null || expirationDate.before(Date())) {

                        Timber.tag("TokenManager").d("FrontEnd token is not valid")
                        // If the user ID does not match, generate a new frontEndToken
                        val newFrontEndToken = generate(userId)
                        prefsManager.frontEndToken = newFrontEndToken
                    }
                } else {
                    Timber.tag("TokenManager").d("FrontEnd token is null")
                    // If frontEndToken does not exist, generate a new one
                    prefsManager.frontEndToken = generate(userId)
                    frontEndToken = prefsManager.frontEndToken
                }
                Timber.tag("TokenManager").d("Moving to generating access token")
                val newAccessToken = generateAccessToken(frontEndToken!!)
                newAccessToken?.let {token->
                    Timber.tag("TokenManager").d("New access token is: $token")
                    prefsManager.accessToken = token
                    _accessTokenState.update { it.copy(success= token,error=null,loading=false) }
                }
                return;
            }
        }else {
            // If access token does not exist, generate a new one
            // Check if frontEndToken exists
            if (frontEndToken != null) {
                // Decode the frontEndToken to check if it is valid
                val decodedToken = decode(frontEndToken)
                val decodedUserId = decodedToken?.getClaim("userId")?.asString()
                if (decodedUserId != userId) {
                    // If the user ID does not match, generate a new frontEndToken
                    val newFrontEndToken = generate(userId)
                    prefsManager.frontEndToken = newFrontEndToken
                }
            } else {
                // If frontEndToken does not exist, generate a new one
                prefsManager.frontEndToken = generate(userId)
                frontEndToken = prefsManager.frontEndToken
            }
            val newAccessToken = generateAccessToken(frontEndToken!!)



            newAccessToken?.let {token->
                prefsManager.accessToken = token
                _accessTokenState.update { it.copy(success= token,error=null,loading=false) }
            }
        }
    }

    suspend fun getAccessToken(uuid: String) : String? {
        // Check if access token already exists
        val accessToken = prefsManager.accessToken
        var frontEndToken = prefsManager.frontEndToken
        if (accessToken != null){
            // Validate access token and if was ok return it
            if(validateAccessToken(accessToken)) {
                return accessToken
            }
            else {
                // If access token is not valid, generate a new one
                // Check if frontEndToken exists
                if (frontEndToken != null) {
                    // Decode the frontEndToken to check if it is valid
                    val decodedToken = decode(frontEndToken)
                    val expirationDate = decodedToken?.expiresAt
                    val decodedUserId = decodedToken?.getClaim("userId")?.asString()
                    if (decodedUserId != uuid || expirationDate == null || expirationDate.before(Date())) {
                        // If the user ID does not match, generate a new frontEndToken
                        val newFrontEndToken = generate(uuid)
                        prefsManager.frontEndToken = newFrontEndToken
                    }
                } else {
                    // If frontEndToken does not exist, generate a new one
                    prefsManager.frontEndToken = generate(uuid)
                    frontEndToken = prefsManager.frontEndToken
                }
                val newAccessToken = generateAccessToken(frontEndToken!!)
                newAccessToken?.let {token->

                    prefsManager.accessToken = token
                    return token;
                }
            }
        }else {
            // If access token does not exist, generate a new one
            // Check if frontEndToken exists
            if (frontEndToken != null) {
                // Decode the frontEndToken to check if it is valid
                val decodedToken = decode(frontEndToken)
                val decodedUserId = decodedToken?.getClaim("userId")?.asString()
                if (decodedUserId != uuid) {
                    // If the user ID does not match, generate a new frontEndToken
                    val newFrontEndToken = generate(uuid)
                    prefsManager.frontEndToken = newFrontEndToken
                }
            } else {
                // If frontEndToken does not exist, generate a new one
                prefsManager.frontEndToken = generate(uuid)
                frontEndToken = prefsManager.frontEndToken
            }
            val newAccessToken = generateAccessToken(frontEndToken!!)



            newAccessToken?.let {token->
                prefsManager.accessToken = token
                return token
            }
        }
        return null
    }
    data class AccessTokenState(
        val success: String? = null,
        val error: String? = null,
        val loading: Boolean = false
    )
}