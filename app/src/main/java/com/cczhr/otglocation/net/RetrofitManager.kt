package com.cczhr.otglocation.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream


/**
 * @author cczhr
 * @description
 * @since 2021/6/10 09:51
 */
class RetrofitManager {
    @Volatile
    private var apiSingleton: Api? = null
    private var BASEURL = "https://api.github.com/"

    companion object {
        @Volatile
        private var INSTANCE: RetrofitManager? = null
        fun getInstance(): RetrofitManager {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = RetrofitManager()
                INSTANCE = instance
                return instance
            }
        }
    }


    fun getBaseApi(): Api {
        synchronized(this) {
            if (apiSingleton == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASEURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(OkHttpClient.Builder() .build())//
                    .build()

                apiSingleton = retrofit.create(Api::class.java)
            }
        }
        return apiSingleton!!
    }


    /*.apply {
        // addInterceptor(LoggingInterceptor())


    }*/



}