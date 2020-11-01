@file:Suppress("FunctionName")

package com.max.reader.app.env

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.*
import javax.net.ssl.*

fun gson(config: GsonBuilder.() -> Unit = {}): Gson =
    GsonBuilder().serializeNulls().setPrettyPrinting().apply(config).create()

fun buildRetrofit(gson: Gson = gson()): Retrofit {
    val client = OkHttpClient.Builder()
        .readTimeout(3, TimeUnit.SECONDS)
        .connectTimeout(3, TimeUnit.SECONDS)
        .apply {

            addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY//HEADERS
            })

            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }

            val sslContext = SSLContext.getInstance("SSL").apply {
                init(null, arrayOf(trustManager), java.security.SecureRandom())
            }

            sslSocketFactory(sslContext.socketFactory, trustManager)
                .hostnameVerifier(HostnameVerifier { _, _ -> true })

        }
        .build()


    return Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
}