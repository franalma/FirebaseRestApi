package com.firebase.rest.neli

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalStateException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class FirebaseRestApi(private val database: String, private val apiKey: String) {
    internal data class LoginBody(
        val email: String,
        val password: String,
        val returnSecureToken: Boolean
    )

    internal data class AccessTokenBody(val grantType: String, val refreshToken: String)
    internal data class AnonymousSignIn(val returnSecureToken: Boolean)

    private var accessToken: AccessTokenResponse? = null
    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            this.level = HttpLoggingInterceptor.Level.BODY
        } else {
            this.level = HttpLoggingInterceptor.Level.NONE
        }
    }
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

    suspend fun doLogin(user: String, pass: String, returnSecureToken: Boolean): LoginResponse =
        suspendCoroutine { continuation ->
            val url = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/"
            val service = getRetroFitInstance(url).create(FirebaseRestApiService::class.java)
            val loginBody = LoginBody(user, pass, returnSecureToken)
            service.doLogin(apiKey, loginBody).enqueue(object :
                Callback<LoginResponse> {
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    response.body()?.let { continuation.resume(it) }
                }
            })
        }

    fun isLogged():Boolean {
        Log.d("FirebaseRestApi", "isLogged")
        return this.accessToken != null
    }

    private suspend fun getAccessToken(refreshToken: String): AccessTokenResponse =
        suspendCoroutine { continuation ->
            Log.d("FirebaseRestApi", "getAccessToken")
            val url = "https://securetoken.googleapis.com/v1/"
            val service = getRetroFitInstance(url).create(FirebaseRestApiService::class.java)
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
                        }
                    }
                })
        }

    suspend fun get(databasePath: String): String =
        accessToken?.let {
            suspendCoroutine<String> { continuation ->
                Log.d("FirebaseRestApi", "get")
                val url = "$database$databasePath/"
                val service = getRetroFitInstance(url).create(FirebaseRestApiService::class.java)
                service.getFromDatabase(url, it.accessToken).enqueue(object : Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        continuation.resume(response.body().toString())
                    }
                })
            }
        } ?: throw IllegalStateException("Access token is null")

    private suspend fun signInAnonymousInternal(returnSecureToken: Boolean = true): AnonymousSignInResponse =
        suspendCoroutine { continuation ->
            Log.d("FirebaseRestApi", "signInAnonymousInternal")
            val url = "https://identitytoolkit.googleapis.com/v1/"
            val fullUrl = url + "accounts:signUp"
            val service = getRetroFitInstance(url).create(FirebaseRestApiService::class.java)
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

}

