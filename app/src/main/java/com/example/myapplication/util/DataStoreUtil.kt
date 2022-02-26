package com.example.myapplication.util

import androidx.datastore.preferences.preferencesDataStoreFile
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils

/**
 * @author Dante
 * 2020/12/11
 */
object DataStoreUtil {

    val dataStore = Utils.getApp().preferencesDataStoreFile(name = "preference")

    fun preferenceData() = dataStore


    fun isLogin() = SPUtils.getInstance().getString(KEY_EMAIL).isNotEmpty() &&
            SPUtils.getInstance().getString(KEY_EMAIL).isNotEmpty() &&
            SPUtils.getInstance().getInt(KEY_ID, 0) != 0

    fun getMyId() = SPUtils.getInstance().getInt(KEY_ID, 0)

}
