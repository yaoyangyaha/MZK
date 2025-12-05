package com.xtiantech.mzk.network

import android.content.Context
import com.xtiantech.mzk.api.ApiService
import com.xtiantech.mzk.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit单例封装（Kotlin object实现单例）
object RetrofitClient {
    private var retrofit: Retrofit? = null

    // 获取Retrofit实例（带Token拦截器）
    fun getInstance(context: Context): Retrofit {
        if (retrofit == null) {
            // 日志拦截器（调试阶段打印网络请求日志）
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // 打印完整日志
            }

            // Token拦截器（给所有请求添加Authorization头）
            val authInterceptor = Interceptor { chain ->
                val token = getToken(context)
                val request = chain.request().newBuilder()
                    .apply {
                        if (token.isNotEmpty()) {
                            addHeader("Authorization", "Bearer $token")
                        }
                        addHeader("Content-Type", "application/json")
                    }
                    .build()
                chain.proceed(request)
            }

            // OkHttp客户端
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()

            // 构建Retrofit
            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create()) // Gson解析JSON
                .build()
        }
        return retrofit!!
    }

    // 保存用户信息到SharedPreferences
    fun saveUserInfo(context: Context, token: String, username: String) {
        val sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putString(Constants.KEY_TOKEN, token)
            .putString(Constants.KEY_USERNAME, username)
            .apply()
    }

    // 清除用户信息（退出登录）
    fun clearUserInfo(context: Context) {
        val sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE)
        sp.edit().clear().apply()
    }

    // 获取Token
    fun getToken(context: Context): String {
        val sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE)
        return sp.getString(Constants.KEY_TOKEN, "") ?: ""
    }

    // 获取用户名
    fun getUsername(context: Context): String {
        val sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE)
        return sp.getString(Constants.KEY_USERNAME, "") ?: ""
    }

    // 判断是否已登录
    fun isLogin(context: Context): Boolean {
        return getToken(context).isNotEmpty()
    }
}