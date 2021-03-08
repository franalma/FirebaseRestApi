package com.firebase.rest.neli.firestore

import android.content.Context
import androidx.annotation.Nullable
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import com.firebase.rest.neli.auth.FirebaseRestAuth
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FireStoreRestApi(private val context: Context,
                       private val projectId:String,
                       private val auth:FirebaseRestAuth,
                       private val  databaseName:String ="(default)") {

    private var baseUrl = "https://firestore.googleapis.com/v1/projects/"
    private var url =  "${this.baseUrl}$projectId/databases/$databaseName/documents/"
    private val queue =  Volley.newRequestQueue(this.context)


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
            val localUrl = this.url+path
            val responseListener = Response.Listener<JSONObject> {
                val joc = JSONObject(it.toString())
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
            }
            val errorListener = Response.ErrorListener {
                continuation.resumeWithException(it)
            }
            val jsonObjectRequest = CustomRequest(Request.Method.GET, localUrl,
                JSONObject(),responseListener,errorListener)
            jsonObjectRequest.addHeader("Authorization ", "Bearer ${auth.accessToken?.accessToken}")
            queue.add(jsonObjectRequest)
        }
    }

    suspend fun set(path: String, value:JSONObject):String = run{
        auth.refreshTokenIfNeeded()

        suspendCoroutine { continuation ->
            val localUrl = "${this.url}$path"
            val jocPayload = includeTypeInfo(value)
            val responseListener = Response.Listener<JSONObject> {
                continuation.resume("")
            }
            var errorListener = Response.ErrorListener {
                continuation.resumeWithException(it)
            }

            val jsonObjectRequest = CustomRequest(Request.Method.PATCH, localUrl,jocPayload,
                responseListener,errorListener)

            jsonObjectRequest.addHeader("Authorization","Bearer ${auth.accessToken?.accessToken}" )
            queue.add(jsonObjectRequest)
        }
    }

}

    class CustomRequest(
        method: Int,
        url: String,
        @Nullable jsonRequest: JSONObject,
        listener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener
    ) : JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
        val map =  HashMap<String, String>()

        fun addHeader(key:String, value:String){
            map.put(key, value)
        }

        override fun getHeaders(): MutableMap<String, String> {
            return map
        }

    }