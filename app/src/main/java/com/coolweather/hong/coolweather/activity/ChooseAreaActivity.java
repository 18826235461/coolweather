package com.coolweather.hong.coolweather.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.hong.coolweather.R;
import com.coolweather.hong.coolweather.db.CoolWeatherDB;
import com.coolweather.hong.coolweather.model.City;
import com.coolweather.hong.coolweather.model.County;
import com.coolweather.hong.coolweather.model.Province;
import com.coolweather.hong.coolweather.util.HttpCallBackListener;
import com.coolweather.hong.coolweather.util.HttpUtil;
import com.coolweather.hong.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 11603 on 2016/11/6.
 */

public class ChooseAreaActivity extends AppCompatActivity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    //当前选中的级别
    private int currentLevel;
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省
    private Province selectedProvince;
    //选中的市
    private City selectedCity;
    //选中的县

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    //存放查询到的信息并构造ArrayAdapter
    private List<String> dataList = new ArrayList<String>();
    //构造ListView的adapter
    private ArrayAdapter<String> adapter;
    //数据库对象
    private CoolWeatherDB coolWeatherDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_area);
        this.titleText = (TextView) findViewById(R.id.title_text);
        this.listView = (ListView) findViewById(R.id.list_view);
        this.adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        this.listView.setAdapter(adapter);
        this.coolWeatherDB = CoolWeatherDB.getInstance(this);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){

                }
            }
        });
        queryProvinces();
    }

    /*
    查询全国所有的省，优先从数据库中读取，如果数据库没有再去服务中查询。
     */
    private void queryProvinces(){
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(null,"province");
        }
    }

    /*
    查询选中的省的所有市信息，优先从数据库中读取，如果没有再去服务器中查找
     */
    private void queryCities(){
        cityList = coolWeatherDB.loadCity(selectedProvince.getId());
        if (cityList.size()>0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /*
    查询选中的市的所有县信息，优先从数据库中读取，如果没有再去服务器中查找
     */
    private void queryCounties(){
        countyList = coolWeatherDB.loadCounty(selectedCity.getId());
        if (countyList.size()>0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /*
    根据传入的的代号和类型从服务器中查询所有的省市县数据
    */
    private void queryFromServer(String code, final String type){
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if (result){
                    //通过runOnUIThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                //通过runOnUIThread()方法回到主线程处理线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*
    显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*
    关闭进度条对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /*
    捕获Back按键，根据当前的级别来判断，此时应该返回市列表，省列表还是直接退出
     */

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            finish();
        }
    }
}
