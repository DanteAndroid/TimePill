package com.dante.diary.model;

import com.google.gson.annotations.SerializedName;

/**
 * For app update
 */

public class AppInfo {

    /**
     * lastest_version : 1.0
     * lastest_version_code : 2
     * message : 修复一些bug;
     * UI细节调整
     * attach_info :
     * apkUrl : app-armeabi-v7a-release.apk
     * forceUpdate : true
     * former_version : v1.0
     * share_app_description : 这个软件不错，图片适合做壁纸。https://github.com/DanteAndroid/Beauty/
     */

    @SerializedName("lastest_version")
    private String version;
    @SerializedName("lastest_version_code")
    private int versionCode;
    private String message;
    @SerializedName("apk_url")
    private String apkUrl;
    private boolean forceUpdate;
    @SerializedName("former_version")
    private String formerVersion;
    @SerializedName("share_app_description")
    private String shareApp;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getFormerVersion() {
        return formerVersion;
    }

    public void setFormerVersion(String formerVersion) {
        this.formerVersion = formerVersion;
    }

    public String getShareApp() {
        return shareApp;
    }

    public void setShareApp(String shareApp) {
        this.shareApp = shareApp;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "version='" + version + '\'' +
                ", versionCode=" + versionCode +
                ", message='" + message + '\'' +
                ", apkUrl='" + apkUrl + '\'' +
                ", forceUpdate=" + forceUpdate +
                ", formerVersion='" + formerVersion + '\'' +
                ", shareApp='" + shareApp + '\'' +
                '}';
    }
}
