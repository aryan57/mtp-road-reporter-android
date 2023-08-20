package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = context.applicationContext.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if(sharedPreferences!=null){
            accessToken = sharedPreferences!!.getString(Constants.TOKEN_CONFIG_ACCESS_TOKEN, null)
            refreshToken = sharedPreferences!!.getString(Constants.TOKEN_CONFIG_REFRESH_TOKEN, null)
        }
    }

    fun getAccessToken(): String? {
        return accessToken
    }

    fun setAccessToken(newAccessToken: String) {
        accessToken = newAccessToken
        if(sharedPreferences!=null){
            val editor = sharedPreferences!!.edit()
            editor.putString(Constants.TOKEN_CONFIG_ACCESS_TOKEN,accessToken)
            editor.apply()
        }
    }

    fun getRefreshToken(): String? {
        return refreshToken
    }

    fun setRefreshToken(newRefreshToken: String) {
        refreshToken = newRefreshToken
        if(sharedPreferences!=null){
            val editor = sharedPreferences!!.edit()
            editor.putString(Constants.TOKEN_CONFIG_REFRESH_TOKEN,refreshToken)
            editor.apply()
        }
    }

    fun clearTokens() {
        accessToken = null
        refreshToken = null
        if(sharedPreferences!=null){
            val editor = sharedPreferences!!.edit()
            editor.remove(Constants.TOKEN_CONFIG_ACCESS_TOKEN)
            editor.remove(Constants.TOKEN_CONFIG_REFRESH_TOKEN)
            editor.apply()
        }
    }

    fun hasTokens(): Boolean {
        return (accessToken!=null && refreshToken!=null)
    }

}
