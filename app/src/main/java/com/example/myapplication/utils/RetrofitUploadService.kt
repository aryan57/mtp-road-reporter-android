package com.example.myapplication.utils

import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitUploadService {

    @Multipart
    @POST(Constants.API_PATH_UPLOAD_IMAGE)
    fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>

}