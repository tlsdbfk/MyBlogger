package com.hb.imageup

import okhttp3.OkHttpClient
import retrofit2.Retrofit

import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitSetting {
    val API_BASE_URL = "http://192.168.18.67:8080/"
    //val httpClient = OkHttpClient.Builder()

    //timeout 시간 늘려주기
    var httpClient = OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val baseBuilder = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())

    fun <S> createBaseService(serviceClass: Class<S>?): S {
        val retrofit = baseBuilder.client(httpClient).build()
        return retrofit.create(serviceClass)
    }
}

