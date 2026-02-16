package com.example.acae30.Retrofit

import com.example.acae30.Interface.AppVentasApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

//object RetrofitCliente {
//
//    private var retrofit: Retrofit? = null
//
//    fun obtenerApi(baseUrl: String): AppVentasApi {
//
//        if (retrofit == null || retrofit?.baseUrl().toString() != baseUrl) {
//            retrofit = Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//        }
//        return retrofit!!.create(AppVentasApi::class.java)
//
//    }
//}
object RetrofitCliente {

    private var retrofit: Retrofit? = null

    fun obtenerApi(baseUrl: String): AppVentasApi {

        if (retrofit == null || retrofit?.baseUrl().toString() != baseUrl) {

            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val client = OkHttpClient.Builder()
                .sslSocketFactory(
                    sslContext.socketFactory,
                    trustAllCerts[0] as X509TrustManager
                )
                .hostnameVerifier { _, _ -> true }
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!.create(AppVentasApi::class.java)
    }
}