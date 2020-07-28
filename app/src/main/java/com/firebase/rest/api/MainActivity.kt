package com.firebase.rest.api

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.rest.neli.FirebaseRestApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    val API_KEY_TEST = "AIzaSyBWD9_bS4mgvHw_6OPCEx_I_AI8N6DIREk"
    val DATABASE_NAME_TEST = "https://testdtse.firebaseio.com/"
    val firebaseRestApiTest = FirebaseRestApi(DATABASE_NAME_TEST,API_KEY_TEST)
    val API_KEY = "AIzaSyAsxhcBcT-0ZrXZGS4qBl1YTy5mbqJjl04"
    val DATABASE_NAME = "https://los40new19-nbbddmodel.firebaseio.com/"
    val firebaseRestApi = FirebaseRestApi(DATABASE_NAME,API_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        testAnonymousLos40()
        testAnonymousHuawei()
    }

    fun testAnonymousLos40(){
        GlobalScope.launch {
            delay(1000)
            try{
                firebaseRestApi.signInAnonymous()
                println("------>App: ${firebaseRestApi.get("settings/splash/")}")
                println("------>App: ${firebaseRestApi.get("settings/constants/")}")

            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun testAnonymousHuawei(){
        GlobalScope.launch {
            delay(1000)
            try{
                firebaseRestApiTest.signInAnonymous()
                var response = firebaseRestApiTest.get("")
                println("-----$response")
                val joc = JSONObject()
                joc.put("user6", "test6")
                firebaseRestApiTest.set("",joc.toString() )
                response = firebaseRestApiTest.get("")
                println("----$response")
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}
