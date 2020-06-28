package com.example.miniproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailActivity extends FragmentActivity implements OnMapReadyCallback {
    private DBHelper dbHelper;
    private GoogleMap mMap;
    private MapData mapData;
    private int id;
    private DialogInterface.OnDismissListener onDismissListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = new Intent(this.getIntent());
        id = intent.getIntExtra("id", 1); //putExtra로 받아온 변수 값을 받는다. (id)

        if (dbHelper == null) {
            dbHelper = new DBHelper(this, "mydb", null, DBHelper.DB_VERSION);
        }

        mapData = dbHelper.getMapdataById(id); //id 값을 바탕으로 db에서 데이터를 받아온다.

        //받은 data를 바탕으로 layout에 setting한다.
        TextView title = (TextView) findViewById(R.id.textView);
        TextView content = (TextView) findViewById(R.id.textView3);
        ImageView image = (ImageView) findViewById(R.id.imageView2);

        try { //Uri to Bitmap을 이용해 image를 띄운다.
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mapData.getUri());
            image.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        title.setText(mapData.getTitle());
        content.setText(mapData.getContent());

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(mapData.getLatLng()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mapData.getLatLng()));
        //moveCamera로 해당하는 위치에 map을 보여준다.

    }

    public void onClickDeleteBtn(View v) {
        dbHelper.deleteDataById(id);
        Toast.makeText(getApplicationContext(), id + " 열이 제거되었습니다.", Toast.LENGTH_SHORT).show();
        this.finish();

    }

    public void onClickShareBtn(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //AlertDialog.Builder 객체를 생성해 준다.
        builder.setTitle("공유하기"); //해당 Builder(alert dialog)의 title에 공유하기를 입력해 띄운다.
        //만일 YES 버튼을 눌렀다면, 공유 창으로 넘어간다는 알림을 띄운 뒤, kakao link 공유하기를 한다.
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                shareKakao();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.show(); //설정해 놓은 dialog를 show()해서 화면에 나타나게 한다.

    }


    public void shareKakao() {

        String address = getAddress(mapData.getLatLng().latitude, mapData.getLatLng().longitude);
        Log.e("tag", address);

        LocationTemplate params = LocationTemplate.newBuilder(address,
                ContentObject.newBuilder(address,
                        "http://www.kakaocorp.com/images/logo/og_daumkakao_151001.png",
                        LinkObject.newBuilder()
                                .setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com")
                                .build())
                        .setDescrption(mapData.getContent())
                        .build())
                .setAddressTitle(mapData.getTitle())
                .build();

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");

        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());

            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
            }
        });
    }

    //위치 정보를 주소지로 바꾸어 주는 함수이다.
    public String getAddress(double lat, double lng) {

        String address = "";

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
