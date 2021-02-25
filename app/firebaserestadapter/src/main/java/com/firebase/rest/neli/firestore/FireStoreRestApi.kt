package com.firebase.rest.neli.firestore

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.firebase.rest.neli.auth.FirebaseRestAuth
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FireStoreRestApi(private val context: Context,
                       private val projectId:String,
                       private val apiKey:String,
                       private val  databaseName:String ="(default)") {

    private var baseUrl = "https://firestore.googleapis.com/v1/projects/"
    private var url =  "${this.baseUrl}$projectId/databases/$databaseName/documents/"
    private val queue =  Volley.newRequestQueue(this.context)
    private var auth = FirebaseRestAuth(apiKey)


    private fun removeTypeInfo(value:JSONObject):JSONObject{
        val joc = JSONObject()
        val keys = value.keys()
        while(keys.hasNext()){
            val key = keys.next()
            val item = value.getJSONObject(key)
            joc.put(key, item.getString(item.keys().next()))
        }
        return joc
    }

    private fun includeTypeInfo(value:JSONObject):JSONObject{
        val joc = JSONObject()
        val keys = value.keys()
        while(keys.hasNext()){
            val key = keys.next()
            val aux = JSONObject()
            aux.put("stringValue", value.getString(key))
            joc.put(key, aux)
        }

        return JSONObject().put("fields", joc);
    }

    suspend fun get(path:String):JSONObject = run {
        auth.refreshTokenIfNeeded()
        suspendCoroutine {continuation ->
            val localUrl = "${this.url}$path?key=${auth.accessToken}"
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, localUrl, null,
                { response ->
                    val joc = JSONObject(response.toString())
                    if (joc.has("fields")) {
                        var result = JSONObject(joc.getString("fields"))
                        result = removeTypeInfo(result)
                        continuation.resume(result)
                    } else if (joc.has("documents")) {
                        val jarray = joc.getJSONArray("documents")
                        val jarrayResult = JSONArray()
                        for (i in 0 until jarray.length()) {
                            val result = removeTypeInfo(jarray.getJSONObject(i).getJSONObject("fields"))
                            var itemName = jarray.getJSONObject(i).getString("name")
                            itemName = itemName.substring(itemName.lastIndexOf("/")+1)
                            jarrayResult.put(JSONObject().put(itemName, result))
                        }
                        continuation.resume(JSONObject().put("documents", jarrayResult))
                    }
                },
                { error ->
                    continuation.resumeWithException(error)
                }
            )
            queue.add(jsonObjectRequest)
        }
    }

    suspend fun set(path: String, value:JSONObject):String = run{
        auth.refreshTokenIfNeeded()
        suspendCoroutine { continuation ->
            val localUrl = "${this.url}$path?key=${auth.accessToken}"
            val jocPayload = includeTypeInfo(value)
            val jsonObjectRequest = JsonObjectRequest(Request.Method.PATCH, localUrl,jocPayload,{
                response->
                continuation.resume("")
                },
            {
                error->
                continuation.resumeWithException(error)
            })
            queue.add(jsonObjectRequest)
        }
    }

}