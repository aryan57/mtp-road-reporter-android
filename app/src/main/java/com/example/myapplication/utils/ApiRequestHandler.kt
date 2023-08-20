package com.example.myapplication.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.example.myapplication.Login
import org.json.JSONObject

class ApiRequestHandler(private val context: Context) {

    private val requestQueue: RequestQueue = RequestQueueSingleton.getInstance(context)

    fun makeApiRequest(
        method: Int,
        url: String,
        jsonRequest: JSONObject? = null,
        onSuccess: (response: JSONObject) -> Unit,
        onError: (errorMessage: String) -> Unit,
        onFinally: () -> Unit
    ) {
        val fullUrl = Constants.SERVER_BASE_URL + Constants.API_VERSION + url
        val request = object : JsonObjectRequest(method, fullUrl, jsonRequest,
            { response ->
                Log.i("TAG", "$url response: $response")
                onSuccess(response)
                onFinally()
            },
            { error ->
                // when token expires we always get 401 Unauthorized status code
                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    refreshToken(
                        onSuccess = {
                            // Retry the API request with the new access token
                            makeApiRequest(method, url, jsonRequest, onSuccess, onError, onFinally)
                        },
                        onError = {
                            // Log the user out
                            logoutUser()
                            onFinally()
                        }
                    )
                } else {
                    val errorMessage = parseErrorMessage(error)
                    Log.i("TAG", "$url error: $errorMessage")
                    onError(errorMessage)
                    onFinally()
                }
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if(TokenManager.getAccessToken()!=null){
                    headers["Authorization"] = "Bearer ${TokenManager.getAccessToken()}"
                }
                return headers
            }
        }

        requestQueue.add(request)
    }

    private fun refreshToken(onSuccess: () -> Unit, onError: () -> Unit) {
        // Make a request to the refresh token endpoint
        // If successful, update the savedAccessToken and call onSuccess()
        // If unsuccessful, call onError()

        val fullUrl = Constants.SERVER_BASE_URL + Constants.API_VERSION + Constants.API_PATH_REFRESH
        val request = object : JsonObjectRequest(Request.Method.POST, fullUrl, null,
            { response ->
                try {
                    TokenManager.setAccessToken(response.getJSONObject("user").getString("access_token"))
                    Log.i("TAG", "refresh token success.")
                    onSuccess()
                }catch (e: Exception){
                    Log.e("TAG", "refresh token error: "+e.message)
                    onError()
                }
            },
            { error ->
                Log.e("TAG","refresh token error: "+parseErrorMessage(error))
                onError()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if(TokenManager.getRefreshToken()!=null){
                    headers["Authorization"] = "Bearer ${TokenManager.getRefreshToken()}"
                }
                return headers
            }
        }
        requestQueue.add(request)
    }

    private fun parseErrorMessage(error: VolleyError): String {
        val defaultErrorMessage = "Some error occurred"
        try {
            val errorJson = JSONObject(String(error.networkResponse.data))
            return errorJson.getString("message")
        }catch (_: Exception){
        }

        return defaultErrorMessage
    }

    fun logoutUser() {
        TokenManager.clearTokens()
        Toast.makeText(context,"Logging out",Toast.LENGTH_SHORT).show()
        val intent = Intent(context, Login::class.java)
        context.startActivity(intent)
        if(context is Activity){
            context.finish()
        }
    }
}
