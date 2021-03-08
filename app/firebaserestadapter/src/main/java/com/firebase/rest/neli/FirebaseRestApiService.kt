package com.firebase.rest.neli

import com.firebase.rest.neli.auth.*
import com.firebase.rest.neli.auth.AccessTokenBody
import com.firebase.rest.neli.auth.AnonymousSignIn
import com.firebase.rest.neli.auth.LoginBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

internal interface FirebaseRestApiService {

    @Headers("Content-Type:application/json")
    @POST("verifyPassword")
    fun doLogin(@Query("key") key:String, @Body loginBody: LoginBody): Call<LoginResponse>

    @Headers("Content-Type:application/json")
    @POST("token")
    fun getAccessToken(@Query("key") key:String,
                       @Body accessTokenBody: AccessTokenBody
    ): Call<AccessTokenResponse>

    @GET
    fun getFromDatabase(@Url url:String, @Query("auth")accessToken:String): Call<ResponseBody>

    @Headers("Content-Type:application/json")
    @POST
    fun doSignInAnonymous(@Url url:String, @Query("key") apiKey:String,
                          @Body body: AnonymousSignIn
    ): Call<AnonymousSignInResponse>

    @Headers("Content-Type:application/json")
    @PATCH
    fun setInDatabase(@Url url: String, @Query("auth")accessToken: String,
                      @Body value:Any): Call<ResponseBody>

    @GET
    fun getFromFireStore(@Url url:String): Call<String>

    @Headers("Content-Type:application/json")
    @POST
    fun doSignWithCustomToken(@Url url:String, @Query("key") apiKey:String,
                          @Body body: CustomTokenSignInBody
    ): Call<CustomTokenSignInResponse>


}