package com.coolweather.hong.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.hong.coolweather.R;
import com.coolweather.hong.coolweather.util.HttpCallBackListener;
import com.coolweather.hong.coolweather.util.HttpUtil;
import com.coolweather.hong.coolweather.util.Utility;

import java.io.BufferedReader;

/**
 * Created by 11603 on 2016/11/10.
 */

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener{
    private LinearLayout weatherInfoLayout;
    //用于显示城市名
    private TextView cityNameText;
    //用于显示发布时间
    private TextView publishText;
    //用于显示天气描述信息
    private TextView weatherDespText;
    //用于显示气温1
    private TextView temp1Text;
    //用于显示气温2
    private TextView temp2Text;
    //用于显示当前日期
    private TextView currentDateText;

    //更换显示城市按钮
    private Button switch_city;
    //手动刷新天气
    private Button refresh_weather;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        //初始化控件
        this.weatherInfoLayout= (LinearLayout) findViewById(R.id.weather_info_layout);
        this.cityNameText= (TextView) findViewById(R.id.city_name);
        this.publishText= (TextView) findViewById(R.id.publish_text);
        this.weatherDespText= (TextView) findViewById(R.id.weather_desp);
        this.temp1Text= (TextView) findViewById(R.id.temp1);
        this.temp2Text= (TextView) findViewById(R.id.temp2);
        this.currentDateText= (TextView) findViewById(R.id.current_date);
        this.switch_city = (Button) findViewById(R.id.switch_city);
        this.refresh_weather = (Button) findViewById(R.id.refresh_weather);
        this.switch_city.setOnClickListener(this);
        this.refresh_weather.setOnClickListener(this);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)){
            //有县级代号就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else {
            //没有就直接显示天气
            showWeather();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.switch_city:
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                this.startActivity(intent);
                this.finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String weather_code = sharedPreferences.getString("weather_code", "");
                if (!TextUtils.isEmpty(weather_code)){
                    queryWeatherInfo(weather_code);
                }
                break;
            default:
                break;
        }
    }

    //查询县级代号对应的天气代号
    private void queryWeatherCode(String countyCode){
        Log.i("qWeatherCode.countyCode",countyCode);
        String address = "http://www.weather.com.cn/data/list3/city" +
                countyCode + ".xml";
        queryFromServer(address,"countyCode");

    }

    //查询天气代号对应的天气。
    private void queryWeatherInfo(String weatherCode) {
        Log.i("weatherCode",weatherCode);
        String address = "http://www.weather.com.cn/data/cityinfo/" +
               weatherCode + ".html";
        queryFromServer(address,"weatherCode");
    }

    //根据传入的地址和类型去向服务器查询天气代号或者天气信息。
    private void queryFromServer(String address, final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)){
                    Log.i("countyCode.onFinish",response);
                    if (!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析出天气代号
                        String [] array = response.split("\\|");
                        if (array != null&& array.length == 2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if ("weatherCode".equals(type)){
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.i("onError",e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    //从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
    private void showWeather(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(sharedPreferences.getString("city_name",""));
        temp1Text.setText(sharedPreferences.getString("temp1",""));
        temp2Text.setText(sharedPreferences.getString("temp2",""));
        weatherDespText.setText(sharedPreferences.getString("weather_desp",""));
        publishText.setText("今天" + sharedPreferences.getString("publish_time","") + "发布");
        currentDateText.setText(sharedPreferences.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

    }
}
