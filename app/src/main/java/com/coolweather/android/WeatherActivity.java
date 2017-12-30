package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.lang.UCharacter;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private TextView qltyText;
    private ImageView bing_pic;
    public SwipeRefreshLayout swipeRefreshLayout;
    private String mweatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        forecastLayout=findViewById(R.id.forecast_layout);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        qltyText=findViewById(R.id.qlty_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        bing_pic=findViewById(R.id.bing_pic);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout=findViewById(R.id.drawer_layout);
        navButton=findViewById(R.id.nav_button);
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=sharedPreferences.getString("weather",null);
        String bingPicImg=sharedPreferences.getString("bing_pic",null);
        if (bingPicImg!=null){
            Glide.with(WeatherActivity.this).load(bingPicImg).into(bing_pic);
        }else {
            loadBingPic();
        }
        if (weatherString!=null){
                Weather weather=Utility.handleWeatherResponse(weatherString);
                mweatherId=weather.basic.weatherId;
                showWeatherInfo(weather);
        }else {
            mweatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mweatherId);
        }

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mweatherId);
            }
        });

    }

    public void requestWeather(final String weatherId){
        loadBingPic();
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId
                +"&key=3fd7d9214612428993550e82dd7f13b3";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                  final String responseText= response.body().string();
              final Weather weather=Utility.handleWeatherResponse(responseText);
              runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      if (weather!=null&&"ok".equals(weather.status)){
                          SharedPreferences sharedPreferences= PreferenceManager
                                  .getDefaultSharedPreferences(WeatherActivity.this);
                          SharedPreferences.Editor editor=sharedPreferences.edit();
                          editor.putString("weather",responseText);
                          editor.apply();
                          mweatherId=weather.basic.weatherId;
                          showWeatherInfo(weather);
                          Toast.makeText(WeatherActivity.this,"天气信息更新成功",Toast.LENGTH_SHORT).show();
                      }else {
                          Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                      }
                        swipeRefreshLayout.setRefreshing(false);
                  }
              });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String degree=weather.now.temprature+"℃";
        String weatherInfo=weather.now.more.info;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        titleCity.setText(cityName);
        titleUpdateTime.setText("更新时间"+updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastLst){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=view.findViewById(R.id.date_text);
            TextView infoText=view.findViewById(R.id.info_text);
            TextView maxText=view.findViewById(R.id.max_text);
            TextView minText=view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText("最高温度 "+forecast.temperature.max+"℃");
            minText.setText("最低温度 "+forecast.temperature.min+"℃");
            forecastLayout.addView(view);
        }
        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            qltyText.setText(weather.aqi.city.qlty);
        }
        String comfort="舒适度： "+weather.suggestion.comfort.info;
        comfortText.setText(comfort);
        String carWash="洗车指数： "+weather.suggestion.carwash.info;
        carWashText.setText(carWash);
        String sport="运动建议： "+weather.suggestion.sport.info;
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    public void loadBingPic(){
        final String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
               final String bingPic=response.body().string();
               SharedPreferences.Editor editor=PreferenceManager
                       .getDefaultSharedPreferences(WeatherActivity.this).edit();
               editor.putString("bing_pic",bingPic);
               editor.apply();
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Glide.with(WeatherActivity.this).load(bingPic)
                               .into(bing_pic);
                   }
               });
            }
        });
    }

}
