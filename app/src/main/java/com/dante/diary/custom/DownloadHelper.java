package com.dante.diary.custom;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bugtags.library.Bugtags;
import com.dante.diary.BuildConfig;
import com.dante.diary.base.App;

import java.io.File;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Download and install apk file.
 */
public class DownloadHelper {
    private BroadcastReceiver receiver;
    private String url;
    private String apkName;
    private Context context;
    private DownloadManager manager;
    private File file;
    private long taskId;
    private File oldFile;
    private boolean isDownloading;


    public DownloadHelper(Context context, String url) {
        this.url = url;
        this.context = context;
    }

    private void startInstall() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String type = "application/vnd.android.package-archive";
        Uri apkUri = file.exists() ? Uri.fromFile(file) : manager.getUriForDownloadedFile(taskId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && file.exists()) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        }
        intent.setDataAndType(apkUri, type);
        context.startActivity(intent);

        if (BuildConfig.DEBUG) {
            Toast.makeText(App.context, "当前是debug版本，由于正式版签名不同更新会失败。卸载重装即可", Toast.LENGTH_LONG).show();
        }
        isDownloading = false;//无需反注册
        if (receiver != null) context.unregisterReceiver(receiver);
    }

    public void downWithDownloadManager(String name, String oldName) {
        if (url.isEmpty()) {
            return;
        }
        manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri resource = Uri.parse(url);
        Log.d(TAG, "downWithDownloadManager: " + url);
        if (TextUtils.isEmpty(name)) {
            apkName = url.substring(url.lastIndexOf('/') + 1);
        } else {
            apkName = name;
        }
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName);
        oldFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), oldName);
        if (oldFile.exists()) {
            boolean result = oldFile.delete();
            Log.i(TAG, "downWithDownloadManager:  old apk file deleted " + result);
        }
//        if (file.exists()) {
//            Log.i(TAG, "downWithDownloadManager: exists" + file.getPath());
//            startInstall();
//            return;
//        }
        registerInstall();
        DownloadManager.Request request = new DownloadManager.Request(resource);
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//        request.setVisibleInDownloadsUi(true);
        request.setTitle(apkName);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
        request.setDescription("Downloading...");
        taskId = manager.enqueue(request);

    }

    private void registerInstall() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //可取得下载的id，适用与多个下载任务的监听
                try {
                    startInstall();
                } catch (Exception e) {
                    Bugtags.sendException(e.getCause());
                    e.printStackTrace();
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        isDownloading = true;
    }

    public void release() {
        if (file != null && file.exists()) {
            boolean result = file.delete();
            Log.i(TAG, "release:  apk file deleted " + result);
        }
        if (isDownloading && receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }
}
