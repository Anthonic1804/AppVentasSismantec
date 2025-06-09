package com.example.acae30.Retrofit

import com.example.acae30.Interface.AppVentasApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCliente {

    private var retrofit: Retrofit? = null

    fun obtenerApi(baseUrl: String): AppVentasApi {

        if (retrofit == null || retrofit?.baseUrl().toString() != baseUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(AppVentasApi::class.java)

    }
}