package com.example.myapplication.ui.login

import com.blankj.utilcode.util.SPUtils
import com.example.myapplication.data.model.User
import com.example.myapplication.di.RegisterApiClass
import com.example.myapplication.net.NetService
import com.example.myapplication.net.TimeApi
import com.example.myapplication.util.KEY_EMAIL
import com.example.myapplication.util.KEY_ID
import com.example.myapplication.util.KEY_PASSWORD
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Dante
 * 2020/12/11
 */
@Singleton
class LoginRepository @Inject constructor(@RegisterApiClass private val timeApi: TimeApi) {

    suspend fun register(email: String, password: String, name: String): User {
        val result = timeApi.register(email, password, name)
        if (result.isSuccessful && result.body() != null) {
            SPUtils.getInstance().put(KEY_EMAIL, email)
            SPUtils.getInstance().put(KEY_PASSWORD, password)
            SPUtils.getInstance().put(KEY_ID, result.body()!!.id)
            return result.body()!!
        } else {
            val errorResponse: String = result.errorBody()?.string()!!
            val jsonObject = JSONObject(errorResponse)
            val errorMessage = jsonObject.optString("message")
            throw IllegalStateException(errorMessage)
        }
    }


    suspend fun login(email: String, password: String): User {
        return NetService.getTimeApi(email, password).getMyProfile().also {
            SPUtils.getInstance().put(KEY_EMAIL, email)
            SPUtils.getInstance().put(KEY_PASSWORD, password)
            SPUtils.getInstance().put(KEY_ID, it.id)
        }
    }


}