package com.coolweather.hong.coolweather.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by 11603 on 2016/11/5.
 */

public class HttpUtil {

    public static void sendHttpRequest(final String address, final HttpCallBackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream in = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    if (connection.getResponseCode() == 200) {
                        in = connection.getInputStream();
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuffer response = new StringBuffer();
//                        String line;
//                        reader.read()
//                        while ((line = reader.readLine()) != null) {
//                            Log.i("line",line);
//                            response.append(line);
//                        }
                        byte [] data = new byte[1024];
                        int num = 0;
                        if((num=in.read(data))!=0){
                            String line = new String(data,0,num);
                            response.append(line);
                        }
                        if (listener != null) {
                            listener.onFinish(response.toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onError(e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
