package com.hb.imageup

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface RetrofitPath {
    @Multipart
    @POST("/")
    fun profileSend(
        @Part imageFile : MultipartBody.Part
    ): Call<String>

    @GET("/")//서버에 GET요청을 할 주소를 입력
    fun getCaption() : Call<JsonObject> //MainActivity에서 사용할 json파일 가져오는 메서드
}