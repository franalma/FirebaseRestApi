package com.firebase.rest.neli

import org.json.JSONObject

interface FirebaseDelegate {

    fun onLoginSuccess(value:Model.LoginResponse)
    fun onLoginError ()
    fun onAccessTokenSuccess(value:Model.AccessTokenResponse)
    fun onAccessTokenError()
    fun onDatabaseRequestResponse(value:JSONObject)
    fun onDatabaseRequestError()
    fun onAnonymousSignIn(value:Model.AnonymousSignInResponse)
    fun onAnonymousSignInFailure()

}