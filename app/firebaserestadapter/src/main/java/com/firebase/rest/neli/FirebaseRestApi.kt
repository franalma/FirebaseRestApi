package com.firebase.rest.neli

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FirebaseRestApi {
    companion object{
        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG){
                this.level = HttpLoggingInterceptor.Level.BODY
            }else{
                this.level = HttpLoggingInterceptor.Level.NONE
            }
        }
        var client:OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
        }.build()
    }

    private fun getRetroFitInstance(url:String):Retrofit{
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun doLogin(apiKey:String, user:String, pass:String, returnSecureToken:Boolean,
                delegate: FirebaseDelegate){
        val url = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/"
        val service = getRetroFitInstance(url).create(FirebaseRestApiService::class.java)
        val loginBody = Model.LoginBody(user,pass, returnSecureToken)
        service.doLogin(apiKey,loginBody).enqueue(object:
            Callback<Model.LoginResponse>{
            override fun onFailure(call: Call<Model.LoginResponse>, t: Throwable) {
                delegate.onLoginError()
            }

            override fun onResponse(call: Call<Model.LoginResponse>,
                                    response: Response<Model.LoginResponse>) {
                response.body()?.let { delegate.onLoginSuccess(it) }
            }
        })
    }

    fun getAccessToken(apiKey:String, refreshToken:String, delegate: FirebaseDelegate){
        val url = "https://securetoken.googleapis.com/v1/"
        val service = getRetroFitInstance(url).create(FirebaseRestApiService::class.java)
        val accessTokenBody = Model.AccessTokenBody("refresh_token", refreshToken)
        service.getAccessToken(apiKey, accessTokenBody).enqueue(object : Callback<Model.AccessTokenResponse>{
            override fun onFailure(call: Call<Model.AccessTokenResponse>, t: Throwable) {
                delegate.onAccessTokenError()
            }

            override fun onResponse(call: Call<Model.AccessTokenResponse>, response: Response<Model.AccessTokenResponse>) {
                response.body()?.let { delegate.onAccessTokenSuccess(it) }
            }

        })
    }

    fun getFromDatabase(databaseName:String, accessToken: String, databasePath:String,
                        delegate: FirebaseDelegate){
        val url = "https://$databaseName.firebaseio.com/$databasePath/"
        val service = getRetroFitInstance(url).create(FirebaseRestApiService::class.java)
        service.getFromDatabase(url, accessToken).enqueue(object :Callback<Any>{
            override fun onFailure(call: Call<Any>, t: Throwable) {
            }

            override fun onResponse(call: Call<Any>,
                response: Response<Any>
            ) {
                val value = JSONObject(response.body().toString())
                delegate.onDatabaseRequestResponse(value)
            }
        })
    }

    fun signInAnonymous(apiKey:String, returnSecureToken:Boolean, delegate: FirebaseDelegate){
        val url = "https://identitytoolkit.googleapis.com/v1/"
        val fullUrl = url + "accounts:signUp"
        val service = getRetroFitInstance(url).create(FirebaseRestApiService::class.java)
        val body = Model.AnonymousSignIn(returnSecureToken)
        service.doSignInAnonymous(fullUrl,apiKey,body).enqueue(object : Callback<Model.AnonymousSignInResponse>{
            override fun onFailure(call: Call<Model.AnonymousSignInResponse>, t: Throwable) {
                delegate.onAnonymousSignInFailure()
            }

            override fun onResponse(
                call: Call<Model.AnonymousSignInResponse>,
                response: Response<Model.AnonymousSignInResponse>
            ) {
                response.body()?.let { delegate.onAnonymousSignIn(it) }
            }

        })
    }
}

