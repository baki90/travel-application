package com.example.miniproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static java.lang.System.currentTimeMillis;

//해당 Fragment는 Diary로서 Calendar의 날짜를 Click하면 해당 날짜의 일기와 주소지가 뜬다.
public class NotiFragment extends Fragment {
    TextView title; //주소
    TextView note; //일기
    DiaryData diaryData;
    long selectDate; //날짜 선택
    FloatingActionButton fab; //+ 버튼
    CalendarView calendarView; //Calendar
    DiaryDBHelper diaryDBHelper;
    DBHelper dbHelper;
    Button button;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_noti, null);
        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        button = view.findViewById(R.id.button6);
        //all delete와 관련된 button이다.
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); //AlertDialog.Builder 객체를 생성해 준다.
                builder.setTitle("초기화하시겠습니까?"); //해당 Builder(alert dialog)의 title에 공유하기를 입력해 띄운다.
                //만일 YES 버튼을 눌렀다면, 공유 창으로 넘어간다는 알림을 띄운 뒤, kakao link 공유하기를 한다.
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dbHelper == null) {
                            dbHelper = new DBHelper(getContext(), "mydb", null, DBHelper.DB_VERSION);
                        }
                        dbHelper.deleteAll();
                        diaryDBHelper.deleteAll();
                        Toast.makeText(getActivity(), "제거되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

                builder.show(); //설정해 놓은 dialog를 show()해서 화면에 나타나게 한다.
            }
        });

        //Diary 정보는 mydb2에 저장해 두었다.
        diaryDBHelper = new DiaryDBHelper(getContext(), "mydb2", null, DiaryDBHelper.DB_VERSION);

        //default 날짜는 현재 날짜 기준이다.
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String setDate = dateFormat.format(currentTimeMillis());
        selectDate = Long.parseLong(setDate);

        //+를 눌렀을 때, DiaryPlusActivity 창이 뜨게 한다.
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), DiaryPlusActivity.class);
                startActivity(intent);
            }
        });

        //입력된 날짜 값을 바탕으로 DB에서 값을 받아오고, (현재는 default, 오늘 날짜) layout 내용을 구성한다.
        diaryData = diaryDBHelper.getDiaryDataByDate(selectDate);
        if(diaryData.getContent() != null) {
            title = (TextView) view.findViewById(R.id.title);
            note = (TextView) view.findViewById(R.id.note);
            title.setText(getAddress(diaryData.getLatLng().latitude, diaryData.getLatLng().longitude));
            note.setText(diaryData.getContent());
        }

        //Calendar의 날짜들이 클릭되었을 때, 해당하는 날짜의 data를 받아오고, 그에 따른 layout을 구성한다.
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectDate = Long.parseLong(dateFormat.format(getDate(year, month, dayOfMonth)));
                Toast.makeText(getActivity(), String.valueOf(selectDate), Toast.LENGTH_SHORT).show();

                //반환된 값이 없을 시에 설정 X (data가 없는 경우)
                diaryData = diaryDBHelper.getDiaryDataByDate(selectDate);
                if(diaryData.getContent() != null) {
                   title.setText(getAddress(diaryData.getLatLng().latitude, diaryData.getLatLng().longitude));
                   note.setText(diaryData.getContent());
                }

            }
        });

        return view;
    }

    //year, month, day의 값을 바탕으로 Date로 바꾸어 주는 함수이다.
    public Date getDate(int year, int month, int day){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        return cal.getTime();
    }
    //위경도 값을 주소로 변환해 주는 함수이다.
    public String getAddress(double lat, double lng) {

        String address = "";

        //위치정보를 활용하기 위한 구글 API 객체
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

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
        List<String> addresses = new LinkedList<>();
        if (list.size() > 0) { //주소 데이터가 존재한다면
            Address addr = list.get(0);

            //String list에 이들을 넣어 두고, null 값이 아니라면 + 해 준다.
            addresses.add(addr.getCountryName()); //나라
            addresses.add(addr.getAdminArea()); //시
            addresses.add(addr.getLocality()); //구
            addresses.add(addr.getThoroughfare()); //동
            addresses.add(addr.getFeatureName()); //지번


            for( Iterator<String> itr = addresses.iterator() ; itr.hasNext();) {
                String str;
                str = itr.next();
                if (str != null) //null 제거
                    address += str + " ";
            }
        }
        return address;
    }


}
