package com.dante.diary.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by yons on 17/3/6.
 */

public class GsonConfig {

    /**
     * created : 2010-03-19 10:32:26
     */
    public static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

}
