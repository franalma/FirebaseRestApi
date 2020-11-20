package com.firebase.rest.neli

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
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

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            this.level = HttpLoggingInterceptor.Level.BODY
        } else {
            this.level = HttpLoggingInterceptor.Level.NONE
        }
    }
    private var auth:FirebaseRestAuth
    private var retrofit:Retrofit? = null

    init {
        val client: OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
        }.build()
        auth = FirebaseRestAuth(apiKey)
        if (database.isNotBlank()){
            retrofit = Retrofit.Builder()
                .baseUrl(database)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    suspend fun doLogin(user: String, pass: String, returnSecureToken: Boolean): LoginResponse =
            auth.doLogin(user, pass,returnSecureToken)

    fun isLogged():Boolean= auth.isLogged()

    suspend fun doLoginWithAccessToken(user: String, pass: String, returnSecureToken: Boolean) {
        auth.doLoginWithAccessToken(user, pass, returnSecureToken)
    }

    suspend fun doLoginWithAccessToken(accessToken: String):AccessTokenResponse =
        auth.getAccessToken(accessToken)

    suspend fun signInAnonymous(returnSecureToken: Boolean = true){
     auth.signInAnonymous(returnSecureToken)
    }



    suspend fun get(databasePath: String): String =
        auth.accessToken.let {
            auth.refreshTokenIfNeeded()
            suspendCoroutine<String> { continuation ->
                val url = "$database$databasePath/.json/"
                Log.d("FirebaseRestApi", "get::url $url")
                val service = retrofit?.create(FirebaseRestApiService::class.java)
                service?.getFromDatabase(url, it!!.accessToken)?.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("FirebaseRestApi", "get error::",t)
                        continuation.resumeWithException(t)
                    }
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        Log.d("FirebaseRestApi", "url: $url get::${response.body()!!::class.java}")
                        val result = response.body()!!.string()
                        continuation.resume(result)
                    }
                })
            }
        } ?: throw IllegalStateException("Access token is null")

    suspend fun set(databasePath: String, data:Any){
        auth.refreshTokenIfNeeded()
        auth.accessToken?.let {
            suspendCoroutine <String>{ continuation->
                val url = "$database$databasePath/.json/"
                Log.d("FirebaseRestApi", "set data: $data")
                Log.d("FirebaseRestApi", "set::url $url")
                val service = retrofit?.create(FirebaseRestApiService::class.java)
                service?.setInDatabase(url, it.accessToken,data)?.enqueue(object :Callback<ResponseBody>{
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("FirebaseRestApi", "set error::",t)
                        continuation.resumeWithException(t)
                    }
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        response.body().let {
                            Log.d("FirebaseRestApi", "url: $url set::${response.body()!!::class.java}")
                            continuation.resume(response.body()!!.string())
                        }
                    }
                })
            }
        }
    }
}

