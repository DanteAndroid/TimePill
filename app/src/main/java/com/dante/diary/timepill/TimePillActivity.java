package com.dante.diary.timepill;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.BottomDialogFragment;
import com.dante.diary.utils.UiUtils;

import butterknife.BindView;
import top.wefor.circularanim.CircularAnim;

/**
 * Created by yons on 17/5/11.
 */

public class TimePillActivity extends BaseActivity {


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

    @Override
    protected int initLayoutId() {
        return R.layout.activity_time_pill;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        get.setOnClickListener(new View.OnClickListener() {
            public BottomDialogFragment dialog;

            @Override
            public void onClick(View v) {
                dialog = BottomDialogFragment.create(R.layout.open_pill_layout)
                        .with(TimePillActivity.this)
                        .bindView(v1 -> {
                            EditText editText = (EditText) v1.findViewById(R.id.keyEditText);
                            Button open = (Button) v1.findViewById(R.id.open);
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
