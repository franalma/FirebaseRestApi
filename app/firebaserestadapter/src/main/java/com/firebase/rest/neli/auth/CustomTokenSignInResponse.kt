package com.firebase.rest.neli.auth

import com.google.gson.annotations.SerializedName

 data class CustomTokenSignInResponse(
    @SerializedName("kind")
    val kind:String,
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("refreshToken")
    val refreshToken:String,
    @SerializedName("expiresIn")
    val expiresIn:String,
    @SerializedName("isNewUser")
    val isNewUser: Boolean
)