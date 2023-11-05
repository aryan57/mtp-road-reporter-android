package com.example.myapplication.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.example.myapplication.Login
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import retrofit2.Response as RetrofitResponse

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
        val request = object : JsonObjectRequest(method, fullUrl, jsonRequest, { response ->
            Log.i(Constants.LOG_TAG_NAME, "$url response: $response")
            onSuccess(response)
            onFinally()
        }, { error ->
            // when token expires we always get 401 Unauthorized status code
            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                refreshToken(onSuccess = {
                    // Retry the API request with the new access token
                    makeApiRequest(method, url, jsonRequest, onSuccess, onError, onFinally)
                }, onError = {
                    // Log the user out
                    logoutUser()
                    onFinally()
                })
            } else {
                val errorMessage = parseErrorMessage(error)
                Log.i(Constants.LOG_TAG_NAME, "$url error: $errorMessage")
                onError(errorMessage)
                onFinally()
            }
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if (TokenManager.getAccessToken() != null) {
                    headers["Authorization"] = "Bearer ${TokenManager.getAccessToken()}"
                }
                return headers
            }
        }

        requestQueue.add(request)
    }

    fun uploadImage(
        file: File,
        onSuccess: (response: JSONObject) -> Unit,
        onError: (errorMessage: String) -> Unit,
        onFinally: () -> Unit
    ) {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val retrofit = Retrofit.Builder().baseUrl(Constants.SERVER_BASE_URL + Constants.API_VERSION)
            .addConverterFactory(GsonConverterFactory.create()).build()

        val apiService = retrofit.create(RetrofitUploadService::class.java)

        var header = ""
        if (TokenManager.getAccessToken() != null) {
            header = "Bearer ${TokenManager.getAccessToken()}"
        }

        val call = apiService.uploadImage(header, filePart)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: RetrofitResponse<ResponseBody>
            ) {

                if (response.code() == 200) {
                    val responseString = response.body()?.string()
                    if (responseString == null) {
                        onError("NULL 200 RESPONSE")
                        onFinally()
                    } else {
                        val jsonResponse = JSONObject(responseString)
                        onSuccess(jsonResponse)
                        onFinally()
                    }
                    return
                }

                // when token expires we always get 401 Unauthorized status code
                if (response.code() == 401) {
                    refreshToken(onSuccess = {
                        // Retry the API request with the new access token
                        uploadImage(
                            file, onSuccess, onError, onFinally
                        )
                    }, onError = {
                        // Log the user out
                        logoutUser()
                        onFinally()
                    })
                } else {
                    val errorMessage =
                        response.errorBody()?.string() ?: "Some error occurred. Try a smaller image"
                    Log.i(Constants.LOG_TAG_NAME, "error: $errorMessage")
                    onError(errorMessage)
                    onFinally()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle the failure here
                onError("Network request failed: ${t.message}")
                onFinally()
            }
        })
    }

    private fun refreshToken(onSuccess: () -> Unit, onError: () -> Unit) {
        // Make a request to the refresh token endpoint
        // If successful, update the savedAccessToken and call onSuccess()
        // If unsuccessful, call onError()

        val fullUrl = Constants.SERVER_BASE_URL + Constants.API_VERSION + Constants.API_PATH_REFRESH
        val request = object : JsonObjectRequest(Method.POST, fullUrl, null, { response ->
            try {
                TokenManager.setAccessToken(
                    response.getJSONObject("user").getString("access_token")
                )
                Log.i(Constants.LOG_TAG_NAME, "refresh token success.")
                onSuccess()
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG_NAME, "refresh token error: " + e.message)
                onError()
            }
        }, { error ->
            Log.e(Constants.LOG_TAG_NAME, "refresh token error: " + parseErrorMessage(error))
            onError()
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if (TokenManager.getRefreshToken() != null) {
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
        } catch (_: Exception) {
        }

        return defaultErrorMessage
    }

    fun logoutUser() {
        TokenManager.clearTokens()
        Toast.makeText(context, "Logging out", Toast.LENGTH_SHORT).show()
        val intent = Intent(context, Login::class.java)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
    }
}
