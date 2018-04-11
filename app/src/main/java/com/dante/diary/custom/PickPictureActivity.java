package com.dante.diary.custom;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.blankj.utilcode.utils.FileUtils;
import com.dante.diary.R;
import com.dante.diary.utils.UiUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PickPictureActivity extends AppCompatActivity {
    public static final int REQUEST_PICK_PICTURE = 100;
    public static final int RESULT_FAILED = 1;
    public static final float SCALE_RATIO = 0.8f;

    private static final String TAG = "PickPictureActivity";
    private static File photo;
    private static Uri photoUri;

    public static Uri getPhotoUri() {
        return photoUri;
    }

    public static File getPhotoi() {
        return photo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pick();
    }

    private void pick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            photoUri = data.getData();

            RxPermissions permissions = new RxPermissions(this);
            permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(grant -> {
                        if (grant) {
                            saveToFile(data);
                        } else {
                            UiUtils.showSnack(getWindow().getDecorView(), getString(R.string.unable_upload_picture));
                        }
                    });
        } else {
            handleResult(resultCode);
        }
    }

    private void saveToFile(Intent data) {
        String suffix = ".jpg";
        if (data.getData() == null) return;
        String type;
        if (data.getType() == null) {
            type = getContentResolver().getType(data.getData());
        } else {
            type = data.getType();
        }
        if (type != null && type.contains("/")) {
            suffix = "." + type.split("/")[1];
        }
        Log.d(TAG, "saveToFile: " + suffix);
        if (data.getData().getPath().endsWith(".gif")) {
            suffix = ".gif";
        }
        photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "temp" + suffix);
        if (photo.exists()) {
            photo.delete();
        }
        try {
            boolean exist = photo.createNewFile();
            if (!exist) {
                Log.e(TAG, "saveToFile: can't create file");
                handleResult(RESULT_FAILED);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (suffix.endsWith("gif")) {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                FileUtils.writeFileFromIS(photo, inputStream, true);
            } else {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                if (bitmap != null) {
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(photo));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    Log.i(TAG, "saveToFile: scale bitmap " + bitmap.getWidth() + ", " + bitmap.getHeight());
                    os.close();
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "saveToFile: write temp file failed");
            handleResult(RESULT_FAILED);
            e.printStackTrace();
        }
        Log.i(TAG, "saveToFile: path " + photo.getPath());
        data.putExtra("path", photo.getAbsolutePath());
        setResult(RESULT_OK, data);
        finish();
    }

    private void handleResult(int result) {
        setResult(result);
        finish();
    }

}
