package com.cczhr.otglocation.net

import com.cczhr.otglocation.bean.DeviceSupportBean
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url


/**
 * @author cczhr
 * @description
 * @since 2021/6/10 10:06
 */
interface Api {
    @GET("repos/filsv/iPhoneOSDeviceSupport/contents")
    suspend fun getDeviceSupport(): DeviceSupportBean?

    @Streaming
    @GET
     fun get(@Url fileUrl: String): Call<ResponseBody>
}