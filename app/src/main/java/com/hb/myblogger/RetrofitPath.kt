package com.hb.imageup

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface RetrofitPath {
    @Multipart
    @POST("/")
    fun profileSend(
        @Part imageFile : MultipartBody.Part
    ): Call<String>
}