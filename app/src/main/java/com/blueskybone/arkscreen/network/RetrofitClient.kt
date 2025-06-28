package com.blueskybone.arkscreen.network

import com.blueskybone.arkscreen.logger.FileLoggingInterceptor
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
    private val fileLogger = FileLoggingInterceptor()
    private val loggingInterceptor = HttpLoggingInterceptor(fileLogger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header(
                    "User-Agent",
                    "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0"
                )
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    // 主服务
    val apiService: ApiService by lazy {
        createRetrofit("https://zonai.skland.com").create(ApiService::class.java)
    }

    // 其他服务
    val hypergryphService: ApiService by lazy {
        createRetrofit("https://as.hypergryph.com").create(ApiService::class.java)
    }

    val akHypergryphService: ApiService by lazy {
        createRetrofit("https://ak.hypergryph.com").create(ApiService::class.java)
    }

    // 创建 Retrofit 实例
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()
    }
}