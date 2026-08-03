package com.example.acae30.data.remote.api.retrofit

import android.content.Context
import com.example.acae30.Utilidades.AuthInterceptor
import com.example.acae30.Utilidades.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitCliente {
    @Volatile
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    fun obtenerRetrofit(baseUrl: String, context: Context): Retrofit{

        if (retrofit == null || currentBaseUrl != baseUrl) {

            synchronized(this) {

                if (retrofit == null || currentBaseUrl != baseUrl) {

                    val tokenManager = TokenManager(context)

                    val trustAllCerts = arrayOf<TrustManager>(
                        object : X509TrustManager {

                            override fun checkClientTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                            ) {
                            }

                            override fun checkServerTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                            ) {
                            }

                            override fun getAcceptedIssuers(): Array<X509Certificate> {
                                return arrayOf()
                            }
                        }
                    )

                    val sslContext = SSLContext.getInstance("SSL")
                    sslContext.init(
                        null,
                        trustAllCerts,
                        SecureRandom()
                    )

                    val client = OkHttpClient.Builder()
                        .sslSocketFactory(
                            sslContext.socketFactory,
                            trustAllCerts[0] as X509TrustManager
                        )
                        .hostnameVerifier { _, _ -> true }
                        .addInterceptor(AuthInterceptor(tokenManager))
                        .build()

                    retrofit = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(client)
                        .addConverterFactory(
                            GsonConverterFactory.create()
                        )
                        .build()

                    currentBaseUrl = baseUrl
                }
            }
        }

        return retrofit!!

    }

    inline fun <reified T> obtenerApi(
        baseUrl: String,
        context: Context
    ): T {

        return obtenerRetrofit(
            baseUrl,
            context
        ).create(T::class.java)
    }
}