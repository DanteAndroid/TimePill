package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * For app update
 */
class AppInfo(
    @SerializedName("lastest_version")
    val version: String,
    @SerializedName("lastest_version_code")
    val versionCode: Int,
    val message: String,
    @SerializedName("apk_url")
    val apkUrl: String,
    val isForceUpdate: Boolean,
    @SerializedName("former_version")
    val formerVersion: String,
    @SerializedName("share_app_description")
    val shareApp: String,
    @SerializedName("egg_url")
    val eggUrl: String,
    val announcement: String,
) {
    /**
     * lastest_version : 1.0
     * lastest_version_code : 2
     * message : 修复一些bug;
     * UI细节调整
     * attach_info :
     * apkUrl : app-armeabi-v7a-release.apk
     * forceUpdate : true
     * former_version : v1.0
     * announcement :
     * share_app_description : 这个软件不错，图片适合做壁纸。https://github.com/DanteAndroid/Beauty/
     * eggUrl : http://pic62.nipic.com/file/20150321/10529735_111347613000_2.jpg
     */
}