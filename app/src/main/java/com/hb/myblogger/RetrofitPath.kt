package com.hb.myblogger

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitPath {
    @Multipart
    @POST("/caption")
    fun profileSend(
        @Part imageFile: MultipartBody.Part
    ): Call<String>


    @GET("/caption")//서버에 GET요청을 할 주소를 입력
    fun getCaption() : Call<JsonObject> //BoardwriteActivity에서 사용할 json파일 가져오는 메서드
}





