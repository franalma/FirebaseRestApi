package com.firebase.rest.neli.auth

import com.google.gson.annotations.SerializedName

internal data class AnonymousSignIn(
    @SerializedName("return_secure_token")
    val returnSecureToken: Boolean
)