package com.example.miniproject;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.System.currentTimeMillis;

//당일의 날짜와 현재 위치를 띄워준 뒤, 일기를 쓰고 upload 버튼을 누르면 이를 mydb2에 입력하는 activity
public class DiaryPlusActivity extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationProviderClient;
    DiaryData diaryData;
    DiaryDBHelper diaryDBHelper;
    EditText content;
    TextView dat;
    TextView locationText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_plus);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        diaryData = new DiaryData();

        diaryDBHelper = new DiaryDBHelper(DiaryPlusActivity.this, "mydb2", null, DiaryDBHelper.DB_VERSION);
        diaryDBHelper.mapDB();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String setDate = dateFormat.format(currentTimeMillis());
        dat = (TextView) findViewById(R.id.dat);
        dat.setText(setDate);
        diaryData.setDate(Long.parseLong(setDate)); //날짜값을 yyyyMMdd 형태로 DB에 넣어 둘 것이다.

        //FusedLocationProviderClient 객체를 생성해 준다.
        try {
            //getLastLocation()함수를 사용하면 디바이스의 위치값을 알아낼 수 있다.
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override //장소값을 받아온다. 만약 GPS가 꺼져 있다면 location은 null값이다.
                public void onSuccess(Location location) {
                    if (location != null) {
                        locationText= (TextView) findViewById(R.id.loc);
                        locationText.setText(getAddress(location.getLatitude(), location.getLongitude()));
                        diaryData.setLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                        Toast.makeText(DiaryPlusActivity.this, getAddress(location.getLatitude(), location.getLongitude()) ,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e){}
    }

    public void clickUpload(View v){
        content = (EditText) findViewById(R.id.Edit_content);
        diaryData.setContent(content.getText().toString());
        diaryDBHelper.addDiaryData(diaryData);
    }
    public void clickBack(View v){
        this.finish(); //Back 버튼을 누르면 해당 액티비티를 종료한다.
    }
    public String getAddress(double lat, double lng) {
        String address = null;

        //위치정보를 활용하기 위한 구글 API 객체
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        //주소 목록을 담기 위한 List
        List<Address> list = null;

        try {
            //주소 목록을 가져온다. --> 위도, 경도, 조회 갯수
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (list == null) {
            Log.e("getAddress", "주소 데이터 얻기 실패");
            return null;
        }

        if (list.size() > 0) {
            Address addr = list.get(0);
            address = addr.getCountryName() + " "       // 나라
                    + addr.getAdminArea() + " "                 // 시
                    + addr.getLocality() + " "                  // 구
                    + addr.getThoroughfare() + " "              // 동
                    + addr.getFeatureName();                    // 지번
        }
        return address;
    }

}

