package com.example.miniproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static android.content.pm.PackageManager.*;
//MainActivity에서 Navigation Bar를 사용한다. 화면 전환은 각각의 Fragment를 통해서 이루어진다.
//Fragment의 종류는 총 세 가지인데, ListView를 띄워 주는 ListView Fragment, Map을 띄워주는 Map Fragment,
//Diary(본인 자유)를 띄워 주는 Noti Fragment로 구성되어 있다. 또한 이 곳에서 Permission을 받는 함수를 구현해 두었다.
public class MainActivity extends AppCompatActivity {

    final int MY_PERMISSIONS = 10; //Permission Code

    private TextView mTextMessage;
    private DBHelper dbHelper;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    private Fragment listviewFragment; //ListView를 보여 주는 Fragment
    private Fragment mapFragment; //Map을 보여 주는 Fragment
    private Fragment notiFragment; //Diary를 보여 주는 Fragment

    //각 Navigation Bar의 item이 CLICK되었을 때, Bar의 글자를 setting한다.
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callPermission(); //Permission을 받아오는 함수를 실행한다.

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //DB를 받아온 후, mapDB() 함수를 통해 DB를 Readable() 상태로 받아온다.
        dbHelper = new DBHelper( MainActivity.this, "mydb", null, DBHelper.DB_VERSION); dbHelper.mapDB();

        listviewFragment = new ListviewFragment();
        mapFragment = new MapsFragment();
        notiFragment = new NotiFragment();

        //Navigation Bar의 각 item들이 click되었을 때, 화면 전환을 해 준다.
        //이는 transaction.replace로 해당 frame_layout을 각 해당하는 fragment로 바꾼다.
        //처음 화면의 fragment는 listview로 설정해 두었다.
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, listviewFragment).commitAllowingStateLoss();
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigation_home: {
                        transaction.replace(R.id.frame_layout, listviewFragment).commitAllowingStateLoss();
                        break;
                    }
                    case R.id.navigation_dashboard: {
                        transaction.replace(R.id.frame_layout, mapFragment).commitAllowingStateLoss();
                        break;
                    }
                    case R.id.navigation_notifications: {
                        transaction.replace(R.id.frame_layout, notiFragment).commitAllowingStateLoss();
                        break;
                    }
                }

                return true;
            }
        });
    }


    //CallPermisson 함수를 통해 5개의 권한이 모두 PERMISSION_GRANTED 상태인지 체크한 후, 권한이 없는 경우
    //requestPermission을 통해서 권한을 요청하게 된다.
    public void callPermission() {
        boolean per1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        boolean per2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        boolean per3 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        boolean per4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean per5 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!per1 || !per2 || !per3 || !per4 || !per5) { //권한이 없는 경우일 때이므로, requestPermission 함수를 사용하여 권한 요청을 하게 된다.
            if (!per1)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS);
            if (!per2)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS);
            if(!per3)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS);
            if(!per4)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS);
            if(!per5)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       /* RequestPermission에 대한 처리는 이곳에서 진행된다. 사용자가 정의해 놓은 requestCode에 따라서 일처리를 하게 되는데,
          각자의 권한 요청을 한 뒤, CallPermission 함수를 다시 호출하여 해당 함수의 else문에 걸릴 때만(모든 권한이 존재할 때에만)
          다음 Activity 로 넘어갈 수 있도록 코드를 짰다. 즉, 권한 검증이 5개가 있는데 이 모든 권한을 체크할 수 있게 된다. */

        //사용자가 권한 요청을 수락한 경우이다.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MY_PERMISSIONS: {
                    callPermission();
                    break;
                }
            }
        }
    }
}
