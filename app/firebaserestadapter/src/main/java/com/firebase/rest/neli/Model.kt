package com.firebase.rest.neli

import com.google.gson.annotations.SerializedName

object Model {
    data class LoginBody (val email:String, val password:String, val returnSecureToken:Boolean)
    data class LoginResponse(
                        @SerializedName("kind")
                        val kind:String,
                        @SerializedName("localId")
                        val localId:String,
                        @SerializedName("email")
                        val email:String,
                        @SerializedName("displayName")
                        val displayName:String,
                        @SerializedName("idToken")
                        val idToken:String,
                        @SerializedName("registered")
                        val registered:String,
                        @SerializedName("refreshToken")
                        val refreshToken:String,
                        @SerializedName("expiresIn")
                        val expiresIn:String)

    data class AccessTokenBody(val grantType:String, val refreshToken:String)
    data class AccessTokenResponse(@SerializedName("access_token")
                                   val accessToken:String,
                                   @SerializedName("expires_in")
                                   val expiresIn: String,
                                   @SerializedName("token_type")
                                   val tokenType:String,
                                   @SerializedName("refresh_token")
                                   val refreshToken: String,
                                   @SerializedName("id_token")
                                   val idToken:String,
                                   @SerializedName("user_id")
                                   val userId:String,
                                   @SerializedName("project_id")
                                   val projectId:String
                                   )

    data class DatabaseResponse(val response:String)
    data class AnonymousSignIn (val returnSecureToken:Boolean)
    data class AnonymousSignInResponse(@SerializedName("kind") val kind:String,
                                       @SerializedName("idToken")
                                       val idToken:String,
                                       @SerializedName("refreshToken")
                                       val refreshToken: String,
                                       @SerializedName("expiresIn")
                                       val expiresIn:String,
                                       @SerializedName("localId")
                                       val localId: String
                                       )

}