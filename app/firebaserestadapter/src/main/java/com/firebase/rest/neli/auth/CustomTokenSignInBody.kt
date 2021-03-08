package com.firebase.rest.neli.auth

import com.google.gson.annotations.SerializedName

internal data class CustomTokenSignInBody(
    @SerializedName("token")
    val token: String,
    @SerializedName("returnSecureToken")
    val returnSecureToken: Boolean

)