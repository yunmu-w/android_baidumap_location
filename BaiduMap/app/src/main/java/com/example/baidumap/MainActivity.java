package com.example.baidumap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    LocationClient mLocationClient;
    MapView mapView;
    BaiduMap baiduMap;
    boolean isFirstLocation = true;

    TextView txtPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Without Location Permissions!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Got Location Permissions!", Toast.LENGTH_SHORT).show();
                    requestLocation();
                }
                break;
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();//启动位置请求
    }

    private void initLocation() {
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myLocationListener);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        txtPosition = findViewById(R.id.txtPosition);

        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);//每一秒发送一次
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
//        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            StringBuffer currentPosition = new StringBuffer();
            currentPosition.append("Longitude: ").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("Latitude: ").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("Country: ").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("Province: ").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("City: ").append(bdLocation.getCity()).append("\n");
            currentPosition.append("District: ").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("Address: ").append(bdLocation.getAddrStr());
            String s = "" + bdLocation.getLatitude();
            txtPosition.setText(currentPosition);

//            navigateTo(bdLocation);

            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }

        }
    }

    private void navigateTo(BDLocation bdLocation) {
        if (isFirstLocation) {
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            MyLocationData locationData = new MyLocationData.Builder().latitude(latLng.latitude).longitude(latLng.longitude).build();
            baiduMap.animateMapStatus(update);
            baiduMap.setMyLocationData(locationData);       // 地图上显示定位点
            isFirstLocation = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
    }
}
