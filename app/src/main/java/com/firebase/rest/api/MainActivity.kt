package com.firebase.rest.api

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.firebase.rest.neli.FirebaseDelegate
import com.firebase.rest.neli.FirebaseRestApi
import com.firebase.rest.neli.Model
import org.json.JSONObject

class MainActivity : AppCompatActivity(), FirebaseDelegate {

    val API_KEY = "AIzaSyBWD9_bS4mgvHw_6OPCEx_I_AI8N6DIREk"
//    val DATABASE_NAME = "los40new19-nbbddmodel"
    val DATABASE_NAME = "testdtse"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Handler().postDelayed(Runnable { FirebaseRestApi().doLogin(API_KEY,
//            "test@user.com","123456", true, this) }
//            ,1000)

        Handler().postDelayed(Runnable { FirebaseRestApi().signInAnonymous(API_KEY, true, this) }
            ,1000)
    }

    override fun onLoginSuccess(value: Model.LoginResponse) {
        println("idToken: "+value.idToken)
        FirebaseRestApi().getAccessToken(API_KEY,value.refreshToken, this)
    }

    override fun onLoginError() {

    }

    override fun onAccessTokenSuccess(value: Model.AccessTokenResponse) {
        println("Access Token: ${value.accessToken}")
        FirebaseRestApi().getFromDatabase(DATABASE_NAME, value.accessToken,
            ".json",this)
    }

    override fun onAccessTokenError() {

    }

    override fun onDatabaseRequestResponse(value: JSONObject) {
        println("-----value: $value")
    }

    override fun onDatabaseRequestError() {

    }

    override fun onAnonymousSignIn(value: Model.AnonymousSignInResponse) {
        println("----onAnonymousSignIn")
        FirebaseRestApi().getAccessToken(API_KEY, value.refreshToken, this)
    }

    override fun onAnonymousSignInFailure() {
        println("---onAnonymousSignInFailure ")
    }
}
