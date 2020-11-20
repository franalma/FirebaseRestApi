package com.firebase.rest.neli

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class FirebaseRestAuth(private val apiKey: String) {

    companion object {
        const val  LOGIN_URL = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/"
        const val  LOGIN_URL_ANON = "https://identitytoolkit.googleapis.com/v1/"
        const val  ACCESS_TOKEN_URL = "https://securetoken.googleapis.com/v1/"
    }

    var accessToken: AccessTokenResponse? = null
    private var expiredTime:Long = 0
    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            this.level = HttpLoggingInterceptor.Level.BODY
        } else {
            this.level = HttpLoggingInterceptor.Level.NONE
        }
    }

    internal data class LoginBody(
        val email: String,
        val password: String,
        val returnSecureToken: Boolean
    )

    internal data class AccessTokenBody(val grantType: String, val refreshToken: String)
    internal data class AnonymousSignIn(val returnSecureToken: Boolean)

    private var client: OkHttpClient = OkHttpClient.Builder().apply {
        this.addInterceptor(interceptor)
    }.build()

    private fun getRetroFitInstance(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

     suspend fun getAccessToken(refreshToken: String): AccessTokenResponse =
        suspendCoroutine { continuation ->
            Log.d("FirebaseRestApi", "getAccessToken")
            val service = getRetroFitInstance(ACCESS_TOKEN_URL).create(FirebaseRestApiService::class.java)
            val accessTokenBody = AccessTokenBody("refresh_token", refreshToken)
            service.getAccessToken(apiKey, accessTokenBody)
                .enqueue(object : Callback<AccessTokenResponse> {
                    override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }
                    override fun onResponse(
                        call: Call<AccessTokenResponse>,
                        response: Response<AccessTokenResponse>
                    ) {
                        response.body()?.let {
                            continuation.resume(it)
                            expiredTime = System.currentTimeMillis() + it.expiresIn*1000
                        }
                    }
                })
        }

    suspend fun doLogin(user: String, pass: String, returnSecureToken: Boolean): LoginResponse =
        suspendCoroutine { continuation ->

            val service = getRetroFitInstance(LOGIN_URL).create(FirebaseRestApiService::class.java)
            val loginBody = LoginBody(user, pass, returnSecureToken)
            service.doLogin(apiKey, loginBody).enqueue(object :
                Callback<LoginResponse> {
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.d("FirebaseRestAuth", "doLogin failed: $t")
                    continuation.resumeWithException(t)
                }

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {

                    response.body()?.let {
                        Log.d("FirebaseRestAuth", "doLogin response: $it")
                        continuation.resume(it) }
                }
            })
        }

    fun isLogged():Boolean {
        Log.d("FirebaseRestApi", "isLogged")
        return this.accessToken != null
    }


    private suspend fun signInAnonymousInternal(returnSecureToken: Boolean = true): AnonymousSignInResponse =
        suspendCoroutine { continuation ->
            Log.d("FirebaseRestApi", "signInAnonymousInternal")

            val fullUrl = LOGIN_URL_ANON + "accounts:signUp"
            val service = getRetroFitInstance(LOGIN_URL_ANON).create(FirebaseRestApiService::class.java)
            val body = AnonymousSignIn(returnSecureToken)
            service.doSignInAnonymous(fullUrl, apiKey, body)
                .enqueue(object : Callback<AnonymousSignInResponse> {
                    override fun onFailure(call: Call<AnonymousSignInResponse>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }

                    override fun onResponse(
                        call: Call<AnonymousSignInResponse>,
                        response: Response<AnonymousSignInResponse>
                    ) {
                        response.body()?.let { continuation.resume(it) }
                    }
                })
        }

    suspend fun doLoginWithAccessToken(user: String, pass: String, returnSecureToken: Boolean) {
        Log.d("FirebaseRestApi", "doLoginWithAccessToken")
        val loginResult = this.doLogin(user, pass, returnSecureToken)
        accessToken = getAccessToken(loginResult.refreshToken)
    }



    suspend fun signInAnonymous(returnSecureToken: Boolean = true){
        Log.d("FirebaseRestApi", "signInAnonymous")
        val loginResult = this.signInAnonymousInternal(returnSecureToken)
        accessToken = getAccessToken(loginResult.refreshToken)

    }

    fun isTokenExpired():Boolean =
        System.currentTimeMillis() > expiredTime

    suspend fun refreshTokenIfNeeded(){
        if (isTokenExpired()){
            Log.d("FirebaseAuth", "Refreshing token")
            accessToken = accessToken.let { getAccessToken(it!!.refreshToken) }
        }
    }

}