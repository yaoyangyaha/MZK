package com.xtiantech.mzk.utils

// 全局常量（替换为你的Flask后端IP）
object Constants {
    // 后端接口基础地址（局域网IP，需替换为实际地址）
    const val BASE_URL = "http://192.168.1.17:5000/"
    // SharedPreferences 名称（存储用户信息）
    const val SP_NAME = "user_info"
    // SP存储Key
    const val KEY_TOKEN = "access_token"
    const val KEY_USERNAME = "username"
    // 资讯页WebView加载的网址（替换为实际资讯页面）
    const val WEBVIEW_URL = "https://gytv.xtiantech.cn"
}