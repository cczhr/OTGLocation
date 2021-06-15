package com.cczhr.otglocation.net

import android.util.Log
import okhttp3.*
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject


/**
 * @author cczhr
 * @description
 * @since 2021/6/10 10:17
 */
class LoggingInterceptor : Interceptor {
    val TAG = "LoggingInterceptor"
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val t1 = System.nanoTime() //请求发起的时间

        val method: String = request.method()
        val jsonObject = JSONObject()
        if ("POST" == method || "PUT" == method) {
            if (request.body() is FormBody) {
                val body = request.body() as? FormBody
                if (body != null) {
                    for (i in 0 until body.size()) {
                        try {
                            jsonObject.put(body.name(i), body.encodedValue(i))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                Log.e(
                    TAG,
                    "request" + java.lang.String.format(
                        "发送请求 %s on %s  %nRequestParams:%s%nMethod:%s",
                        request.url(), chain.connection(), jsonObject.toString(), request.method()
                    )
                )
            } else {
                val buffer = Buffer()
                val requestBody: RequestBody? = request.body()
                if (requestBody != null) {
                    request.body()!!.writeTo(buffer)
                    val body: String = buffer.readUtf8()
                    Log.e(
                        TAG,
                        "request" + java.lang.String.format(
                            "发送请求 %s on %s  %nRequestParams:%s%nMethod:%s",
                            request.url(), chain.connection(), body, request.method()
                        )
                    )
                }
            }
        } else {
            Log.e(
                TAG,
                "request" + java.lang.String.format(
                    "发送请求 %s on %s%nMethod:%s",
                    request.url(), chain.connection(), request.method()
                )
            )
        }
        val response = chain.proceed(request)
        return try {
            val t2 = System.nanoTime() //收到响应的时间
            val responseBody = response.peekBody((1024 * 1024).toLong())
            Log.e(
                TAG,
                "request" + String.format(
                    "接收响应: %s %n返回json:【%s】 %n耗时：%.1fms",
                    response.request().url(),
                    responseBody.string(),
                    (t2 - t1) / 1e6
                )
            )
            response
        } catch (e: Exception) {
            response
        }
    }
}