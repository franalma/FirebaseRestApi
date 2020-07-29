package com.firebase.rest.api

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.rest.neli.FirebaseRestApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    val API_KEY_TEST = "AIzaSyBWD9_bS4mgvHw_6OPCEx_I_AI8N6DIREk"
    val DATABASE_NAME_TEST = "https://testdtse.firebaseio.com/"
    val firebaseRestApiTest = FirebaseRestApi(DATABASE_NAME_TEST,API_KEY_TEST)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testAnonymousHuawei()
    }

    fun testAnonymousHuawei(){
        GlobalScope.launch {
            delay(1000)
            try{
                firebaseRestApiTest.signInAnonymous()
                var response = firebaseRestApiTest.get("")
                println("-----$response")
                val map = mapOf("user6" to "test6")
//                firebaseRestApiTest.set("",map)
                firebaseRestApiTest.set("",User("user6"))
                response = firebaseRestApiTest.get("")
                println("----$response")
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    data class User(val name:String)
}
