package com.coolweather.hong.coolweather.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.coolweather.hong.coolweather.db.CoolWeatherDB;
import com.coolweather.hong.coolweather.model.City;
import com.coolweather.hong.coolweather.model.County;
import com.coolweather.hong.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 11603 on 2016/11/5.
 */

public class Utility {
    /*
    解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response){
        if (!TextUtils.isEmpty(response)){
            Log.i("handleProvincesResponse",response);
            String [] allProvince = response.split(",");
            if (allProvince!=null&&allProvince.length>0){
                for (String p : allProvince){
                    Log.i("handleProvincesResponse",p);
                    String [] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到Province表
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /*
    解析和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities!=null&&allCities.length>0){
                for (String c : allCities){
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setProvinceId(provinceId);
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /*
    解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if (allCounties!=null&&allCounties.length>0){
                for (String c : allCounties){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /*
    解析服务器返回的JSON数据，并解析出的数据存储到本地。
     */
    public static void handleWeatherResponse (Context context, String response){
        try {
            Log.i("JSON",response);
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weateherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weateherInfo.getString("city");
            String weatherCode = weateherInfo.getString("cityid");
            String temp1 = weateherInfo.getString("temp1");
            String temp2 = weateherInfo.getString("temp2");
            String weatherDesp = weateherInfo.getString("weather");
            String publishTime = weateherInfo.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    将服务器返回的所有天气信息存储到SharedPreferences文件中
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static void saveWeatherInfo (Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
//        editor.putString("current_date", simpleDateFormat.format(new Date()));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        String current_date = c.get(Calendar.YEAR) + "年" +c.get(Calendar.MONTH) + "月" + c.get(Calendar.DAY_OF_MONTH) +"日";
        editor.putString("current_date", current_date);
        editor.commit();
    }
}
