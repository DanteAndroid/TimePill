package com.dante.diary.custom;


import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.dante.diary.setting.SettingFragment;
import com.dante.diary.utils.SpUtil;

import java.util.List;

/**
 * Created by yons on 17/4/12.
 */

public class LockPatternUtil {

    public static void checkPattern(List<PatternLockView.Dot> pattern, PatternLockView view, OnCheckPatternResult l) {
        String patternResult = PatternLockUtils.patternToString(view, pattern);
        if (patternResult.equals(SpUtil.getString(SettingFragment.PATTERN_LOCK_PSW))) {
            l.onSuccess();
        } else {
            view.setViewMode(PatternLockView.PatternViewMode.WRONG);
            view.postDelayed(view::clearPattern, 400);
            l.onFailed();
        }
    }

    public interface OnCheckPatternResult {
        void onSuccess();

        void onFailed();
    }


}
