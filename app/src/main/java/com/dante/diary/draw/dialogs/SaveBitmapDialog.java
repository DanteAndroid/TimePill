package com.dante.diary.draw.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.dante.diary.R;
import com.dante.diary.utils.DateUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Created by Ing. Oscar G. Medina Cruz on 09/11/2016.
 */

public class SaveBitmapDialog extends DialogFragment {
    private static final String TAG = "SaveBitmapDialog";
    private OnSaveBitmapListener onSaveBitmapListener;

    // VARS
    private Bitmap mPreviewBitmap;
    private String fileName;

    public SaveBitmapDialog() {
    }

    public static SaveBitmapDialog newInstance() {
        return new SaveBitmapDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_save_bitmap, null);
        ImageView imageView = view.findViewById(R.id.iv_capture_preview);
        final TextInputEditText textInputEditText = view.findViewById(R.id.et_file_name);
        fileName = DateUtil.getDisplayDay(new Date());
        if (mPreviewBitmap != null)
            imageView.setImageBitmap(mPreviewBitmap);
        else
            imageView.setImageResource(R.color.colorAccent);
        textInputEditText.setText(fileName);

        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fileName = charSequence.toString();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton(R.string.save, (dialogInterface, i) -> {
                    textInputEditText.setText(fileName);
                    save(mPreviewBitmap);
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    if (onSaveBitmapListener != null)
                        onSaveBitmapListener.onSaveBitmapCanceled();
                    dismiss();
                });

        return builder.create();
    }

    public void save(Bitmap bitmap) {
        try {
            if (!fileName.contains(".")) {
                fileName = fileName + ".jpg";
            }
            File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
            boolean result = image.createNewFile();
            Log.d(TAG, "onCreateDialog: save draw result: " + result);

            FileOutputStream fileOutputStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            if (onSaveBitmapListener != null)
                onSaveBitmapListener.onSaveBitmapCompleted(image);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // METHODS
    public void setPreviewBitmap(Bitmap bitmap) {
        this.mPreviewBitmap = bitmap;
    }

    // LISTENER
    public void setOnSaveBitmapListener(OnSaveBitmapListener onSaveBitmapListener) {
        this.onSaveBitmapListener = onSaveBitmapListener;
    }

    public interface OnSaveBitmapListener {
        void onSaveBitmapCompleted(File file);

        void onSaveBitmapCanceled();
    }
}
