package com.coolweather.android.gson;

/**
 * Created by huqun on 2017/12/26.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
        public String qlty;
    }
}
