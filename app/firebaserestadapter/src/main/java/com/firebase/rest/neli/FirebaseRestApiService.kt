package com.firebase.rest.neli

import retrofit2.Call
import retrofit2.http.*

interface FirebaseRestApiService {

    @Headers("Content-Type:application/json")
    @POST("verifyPassword")
    fun doLogin(@Query("key") key:String, @Body loginBody:Model.LoginBody): Call<Model.LoginResponse>

    @Headers("Content-Type:application/json")
    @POST("token")
    fun getAccessToken(@Query("key") key:String,
                       @Body accessTokenBody:Model.AccessTokenBody): Call<Model.AccessTokenResponse>

    @GET
    fun getFromDatabase(@Url url:String, @Query("auth")accessToken:String): Call<Any>

    @Headers("Content-Type:application/json")
    @POST
    fun doSignInAnonymous(@Url url:String, @Query("key") apiKey:String,
                          @Body body:Model.AnonymousSignIn): Call<Model.AnonymousSignInResponse>
}