package com.firebase.rest.neli.auth

import com.google.gson.annotations.SerializedName

data class AccessTokenResponse(
    @SerializedName("access_token")
    val accessToken:String,
    @SerializedName("expires_in")
    val expiresIn: Long,
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