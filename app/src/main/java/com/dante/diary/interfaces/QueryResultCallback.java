package com.dante.diary.interfaces;

import java.util.List;

/**
 * Created by yons on 17/3/13.
 */

public interface QueryResultCallback<T> {
    void onExist(List<T> list);

    void notExist();
}
