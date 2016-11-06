package com.coolweather.hong.coolweather.util;

/**
 * Created by 11603 on 2016/11/5.
 */

public interface HttpCallBackListener {
    void onFinish(String response);
    void onError(Exception e);
}
