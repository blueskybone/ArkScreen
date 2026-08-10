package com.blueskybone.arkscreen.data.network

import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.BuildConfig
import com.blueskybone.arkscreen.core.logger.FileLoggingInterceptor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

/**
 *   Created by blueskybone
 *   Date: 2025/5/19
 */
object RetrofitClient {
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 15L
    private val objectMapper = ObjectMapper().registerKotlinModule().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor(FileLoggingInterceptor(APP)).apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    }
                )
            }
        }
        .build()

    // 森空岛api
    val sklandApiService: ApiService by lazy {
        createRetrofit("https://zonai.skland.com").create(ApiService::class.java)
    }

    // 鹰角服务器身份认证相关
    val hypergryphService: ApiService by lazy {
        createRetrofit("https://as.hypergryph.com").create(ApiService::class.java)
    }

    //官网api，抽卡用
    val akHypergryphService: ApiService by lazy {
        createRetrofit("https://ak.hypergryph.com").create(ApiService::class.java)
    }

    //b站api，用于首页列表
    val biliService: ApiService by lazy {
        createRetrofit("https://app.biliapi.com").create(ApiService::class.java)
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()
    }
}
