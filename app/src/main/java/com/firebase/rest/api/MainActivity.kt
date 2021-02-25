package com.firebase.rest.api

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.rest.neli.firestore.FireStoreRestApi
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testFireStore()

    }
    lateinit var fireStoreRestApi:FireStoreRestApi

    private fun testFireStore(){
        val API_KEY_TEST = "AIzaSyCmkwwpis6tIrVef_-TQzrMthfutuNPHDk"
        fireStoreRestApi = FireStoreRestApi(baseContext,"testdtse",API_KEY_TEST)
//        this.getFromFirestore("mascotas/masc2/")
        this.getFromFirestore("users/")
        val item = JSONObject()
        item.put("name", "Nela")
            .put("date", "2")
    }

    private fun getFromFirestore(path:String){
        GlobalScope.launch {
            val joc =  fireStoreRestApi.get(path)

            if (joc.has("documents")){
                val jarray = joc.getJSONArray("documents")
                for(i in 0 until jarray.length()){
                    val gson = Gson()
                    val jaux = jarray.getJSONObject(i)
                    val user = gson.fromJson(jaux.getJSONObject(jaux.keys().next()).toString(), User::class.java)
                    println("-----user name: ${user.name}")
                }
            }

        }
    }

    private fun setToFirestore(path:String, payload:JSONObject) {
        GlobalScope.launch {
            fireStoreRestApi.set(path, payload)
        }
    }

}
