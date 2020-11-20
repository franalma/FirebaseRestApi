package com.firebase.rest.neli.auth

import com.google.gson.annotations.SerializedName
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