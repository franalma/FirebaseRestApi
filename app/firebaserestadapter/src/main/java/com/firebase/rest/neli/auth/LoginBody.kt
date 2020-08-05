package com.firebase.rest.neli.auth

import com.google.gson.annotations.SerializedName


internal data class LoginBody(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("return_secure_token")
    val returnSecureToken: Boolean
)