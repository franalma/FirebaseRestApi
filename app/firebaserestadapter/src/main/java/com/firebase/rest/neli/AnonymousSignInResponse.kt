package com.firebase.rest.neli

import com.google.gson.annotations.SerializedName

data class AnonymousSignInResponse(
    @SerializedName("kind")
    val kind: String,
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String?,
    @SerializedName("expiresIn")
    val expiresIn: String,
    @SerializedName("localId")
    val localId: String
)