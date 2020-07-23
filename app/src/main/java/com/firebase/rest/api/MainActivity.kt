package com.firebase.rest.api

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.rest.neli.FirebaseRestApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    val API_KEY = "AIzaSyBWD9_bS4mgvHw_6OPCEx_I_AI8N6DIREk"
//    val DATABASE_NAME = "los40new19-nbbddmodel"
    val DATABASE_NAME = "testdtse"
    val firebaseRestApi = FirebaseRestApi(API_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test002()

    }

    fun test002(){
        GlobalScope.launch {
            delay(1000)
            try{
                val result = firebaseRestApi.doLoginWithAccessToken("test@user.com",
                    "123456", true)
                println("accessToken: ${result.accessToken}")

                val queryResult = firebaseRestApi.getFromDatabase(DATABASE_NAME,
                    result.accessToken,".json")
                println("Database: $queryResult")
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun test001(){
        GlobalScope.launch {
            try{
                delay(1000)
                val result = firebaseRestApi.signInAnonymous(API_KEY, true)
                println("Token: ${result.idToken}")

                val loginResult = firebaseRestApi.doLogin("test@user.com", "123456", true)
                println("Login result: ${loginResult.idToken}")

                val accessToken = firebaseRestApi.getAccessToken(loginResult.refreshToken)
                println("Access token: ${accessToken.accessToken}")

                val queryResult = firebaseRestApi.getFromDatabase(DATABASE_NAME,
                    accessToken.accessToken,".json")
                println("Database: $queryResult")


            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}
