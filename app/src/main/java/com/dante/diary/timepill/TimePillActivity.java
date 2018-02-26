package com.dante.diary.timepill;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVObject;
import com.blankj.utilcode.utils.ClipboardUtils;
import com.blankj.utilcode.utils.ToastUtils;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.BottomDialogFragment;
import com.dante.diary.interfaces.QueryResultCallback;
import com.dante.diary.model.DataBase;
import com.dante.diary.utils.UiUtils;

import java.util.List;

import butterknife.BindView;
import top.wefor.circularanim.CircularAnim;

/**
 * Created by yons on 17/5/11.
 */

public class TimePillActivity extends BaseActivity {
    private static final String TAG = "TimePillActivity";

    private static final String TIMEPILL_INTRO_KEY = "#tpk-Q4LGF27FC4#";
    @BindView(R.id.pill)
    ImageView pill;
    @BindView(R.id.time_pill)
    TextView timePill;
    @BindView(R.id.put)
    Button put;
    @BindView(R.id.get)
    Button get;
    @BindView(R.id.timepillIntro)
    TextView timepillIntro;
    @BindView(R.id.myTimepill)
    TextView myTimepill;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_time_pill;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        get.setOnClickListener(new View.OnClickListener() {
            BottomDialogFragment dialog;

            @Override
            public void onClick(View v) {
                dialog = BottomDialogFragment.create(R.layout.open_pill_layout)
                        .with(TimePillActivity.this)
                        .bindView(v1 -> {
                            EditText editText = v1.findViewById(R.id.keyEditText);
                            Button open = v1.findViewById(R.id.open);
                            open.setOnClickListener(v2 -> {
                                if (editText.getText().length() < TimePillOpenActivity.KEY_LENGTH
                                        || !TimePillOpenActivity.isValid(editText.getText().toString())) {
                                    UiUtils.showSnack(open, getString(R.string.invalid_timepill_key));
                                } else {
                                    dialog.dismiss();
                                    openTimePill(editText.getText().toString(), open);
                                }
                            });

                        });
                dialog.show();
            }
        });

        put.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), TimePillCreateActivity.class)));

        timepillIntro.setOnClickListener(v -> openTimePill(TIMEPILL_INTRO_KEY, timepillIntro));
        myTimepill.setOnClickListener(v -> DataBase.findTimePills(new QueryResultCallback<AVObject>() {
            @Override
            public void onExist(List<AVObject> list) {
                showTimePills(list);
            }

            @Override
            public void notExist() {
                UiUtils.showSnack(myTimepill, getString(R.string.timepills_not_found));
            }
        }));
    }

    private void showTimePills(List<AVObject> list) {
        String[] keys = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            AVObject tp = list.get(i);
            keys[i] = (String) tp.get(Constants.KEY);
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.my_timepills_key)
                .setItems(keys, (dialog, which) -> {
                    ClipboardUtils.copyText(keys[which]);
                    ToastUtils.showShortToast(R.string.timepill_key_copied_no_share);
                }).show();
    }

    private void openTimePill(String key, View view) {
        CircularAnim.fullActivity(TimePillActivity.this, pill)
                .colorOrImageRes(R.color.red)
                .duration(600)
                .go(() -> {
                    Intent intent = new Intent(getApplicationContext(), TimePillOpenActivity.class);
                    intent.putExtra(Constants.KEY, key);
                    startActivity(intent);
                });
    }


}
