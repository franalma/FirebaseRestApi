package com.firebase.rest.neli.auth

import android.util.Log
import com.firebase.rest.neli.AccessTokenResponse
import com.firebase.rest.neli.BuildConfig
import com.firebase.rest.neli.FirebaseRestApiService
import com.firebase.rest.neli.LoginResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class FirebaseRestAuth(private val apiKey: String) {

    private val TAG = javaClass.simpleName

    internal var accessToken: AccessTokenResponse? = null
    private var expiredTime: Long = 0
    private val interceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                this.level = HttpLoggingInterceptor.Level.BODY
            } else {
                this.level = HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
        }.build()
    }

    private fun getRetroFitInstance(url: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

     suspend fun getAccessToken(refreshToken: String): AccessTokenResponse =
        suspendCoroutine { continuation ->
            Log.d(TAG, "getAccessToken")
            val accessTokenBody = AccessTokenBody(
                    "refresh_token",
                    refreshToken
                )
            Log.d(TAG, "getAccessToken: $accessTokenBody")
            firebaseRestApiService(ACCESS_TOKEN_URL).getAccessToken(apiKey, accessTokenBody)
                .enqueue(continuation) { _, response ->
                    response.body()?.let {
                        expiredTime = System.currentTimeMillis() + it.expiresIn * 1000
                        continuation.resume(it)
                    }
                }
        }

    suspend fun doLogin(user: String, pass: String, returnSecureToken: Boolean): LoginResponse =
        suspendCoroutine { continuation ->
            val loginBody = LoginBody(
                    user,
                    pass,
                    returnSecureToken
                )
            firebaseRestApiService(LOGIN_URL).doLogin(apiKey, loginBody).enqueue(continuation) {
                _, response -> response.body()?.let { continuation.resume(it) }
            }
        }

    fun isLogged(): Boolean = run {
        Log.d(TAG, "isLogged")
        accessToken != null
    }

    private suspend fun signInAnonymousInternal(returnSecureToken: Boolean = true): AnonymousSignInResponse =
        suspendCoroutine { continuation ->
            Log.d(TAG, "signInAnonymousInternal")
            val fullUrl = LOGIN_URL_ANON + "accounts:signUp"
            val body = AnonymousSignIn(returnSecureToken)
            firebaseRestApiService(LOGIN_URL_ANON).doSignInAnonymous(fullUrl, apiKey, body)
                .enqueue(continuation) { _, response ->
                    Log.d(TAG, "signInAnonymousInternal: ${response.body()}")
                    response.body()?.let { continuation.resume(it) }
                }
        }

    private fun firebaseRestApiService(url: String): FirebaseRestApiService =
        getRetroFitInstance(url).create(FirebaseRestApiService::class.java)

    suspend fun doLoginWithAccessToken(user: String, pass: String, returnSecureToken: Boolean) {
        Log.d(TAG, "doLoginWithAccessToken")
        val loginResult = doLogin(user, pass, returnSecureToken)
        accessToken = getAccessToken(loginResult.refreshToken)
    }

    suspend fun signInAnonymous(returnSecureToken: Boolean = true) {
        Log.d(TAG, "signInAnonymous")
        val loginResult = signInAnonymousInternal(returnSecureToken)
        Log.d(TAG, "signInAnonymous: $loginResult")
        accessToken = getAccessToken(loginResult.refreshToken)

    }

    private fun isTokenExpired(): Boolean = System.currentTimeMillis() > expiredTime

    suspend fun refreshTokenIfNeeded() {
        if (isTokenExpired()) {
            Log.d(TAG, "Refreshing token")
            accessToken = accessToken?.let { getAccessToken(it.refreshToken) }
        }
    }

    private inline fun <T> Call<T>.enqueue(
        continuation: Continuation<T>,
        crossinline success: (Call<T>, response: Response<T>) -> Unit
    ) {
        enqueue(
            object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        success(call, response)
                    } else {
                        continuation.resumeWithException(IOException("Request failed with error code ${response.code()}"))
                    }
                }
            }
        )
    }

    companion object {
        const val LOGIN_URL = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/"
        const val LOGIN_URL_ANON = "https://identitytoolkit.googleapis.com/v1/"
        const val ACCESS_TOKEN_URL = "https://securetoken.googleapis.com/v1/"
    }

}