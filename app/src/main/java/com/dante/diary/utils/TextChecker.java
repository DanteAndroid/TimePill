package com.dante.diary.utils;

import android.widget.EditText;

/**
 * Created by yons on 17/3/14.
 */

public class TextChecker {

    public static boolean isTextInvalid(EditText editText) {
        return (editText == null || editText.getText() == null ||
                editText.getText().toString().replace(" ", "").length() == 0);
    }
}
