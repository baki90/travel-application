package com.example.miniproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nineoldandroids.view.ViewHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.System.out;

//해당 Activity는 DB에 data를 insert할 때, 값을 받아오는 Activity이다.
public class PlusActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DBHelper dbHelper; //DB 접근 함수
    MapData mapData; //MapData 객체
    PickImageHelper ViewHelper; //Camera, Gallery 선택
    boolean isClicked = false; //Map이 이미 click되었을 때, Marker 제거를 위한 변수 (Marker 1개만 띄우려고)
    private GoogleMap mMap; //googleMap


    //layout
    ImageButton imageButton;
    EditText title;
    EditText snippet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus);

        //"mydb"라는 DB를 불러온다.
        dbHelper = new DBHelper(PlusActivity.this, "mydb", null, DBHelper.DB_VERSION);
        dbHelper.mapDB();
        title = findViewById(R.id.input_title);
        snippet = findViewById(R.id.input_snippet);
        ViewHelper = new PickImageHelper();
        mapData = new MapData();

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ImageButton을 클릭했을 때, ViewHelper를 통해 SelectImage라는 함수를 불러 Camera, Gallery를 선택한다.
                //해당 함수는 ActivityForResult를 사용하는데, 받은 값은 아래 onActivityResult에서 처리한다.
                ViewHelper.selectImage(PlusActivity.this);
            }
        });

        //Map을 설정해 준다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            try { //getPickImageResultUri를 통해 사진의 Uri를 받아오고, 이를 makeThum 함수로 썸네일을 만들어 Uri로 지정한다.
                Uri imageUri = makeThum(ViewHelper.getPickImageResultUri(this, data));
                //해당하는 uri를 mapData 객체에 할당해 준 뒤, bitmap image로 imageButton에 띄운다.
                mapData.setUri(imageUri);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageButton.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //map이 click되면 해당하는 곳에 marker를 심는다. 그러나 이미 marker가 있는 경우에, mMap.clear()를 통해
        //원래 있던 마커를 삭제해 주었다. draggable을 true로 설정하여 마커가 움직일 수 있게 해 주었다.
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng _latLng) {
                if (isClicked == true) { //이미 눌려 있는 경우에 마커들을 제거
                    mMap.clear();
                    isClicked = false;
                }
                MarkerOptions markerOptions = new MarkerOptions().position(_latLng).draggable(true);
                mMap.addMarker(markerOptions);
                mapData.setLatLng(_latLng); //mapData 객체에 latlng를 설정해 준다.
                isClicked = true; //한 번 눌린 경우 true
            }
        });
    }

    //PostButton을 클릭했을 때, DB에 값을 insert하게 된다.
    public void postClick(View view) {
        if (mapData.getLatLng() == null) { //marker를 입력하지 않았을 때, post되지 못하게 설정해 두었다.
            //이는 현재 mapData 객체에 latlng값이 할당되었는지를 바탕으로 알 수 있다.
            Toast.makeText(this, "Please mark on map", Toast.LENGTH_SHORT).show();
        } else {
            //title, content에 있는 값을 mapData에 삽입한 후, dbHelper의 addMapdata함수를 호출하여 insert한다.
            mapData.setTitle(title.getText().toString());
            mapData.setContent(snippet.getText().toString());
            dbHelper.addMapData(mapData);
            //insert를 성공적으로 마치면 성공적으로 insert 되었다는 알림이 뜨게 된다.
            Toast.makeText(this, "insert success", Toast.LENGTH_SHORT).show();
            this.finish(); //이후 해당 activity를 종료하여 이전 페이지로 돌아간다.
        }
    }

    //사진 파일의 크기가 너무 크기 때문에, 썸네일을 이용하였다. 썸네일은 CaheDir에 저장해 두었다.
    public Uri makeThum(Uri uri) throws IOException {
        File getImage = this.getExternalCacheDir(); //현 패키지의 CacheDir을 불러온다.

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 6; //이미지를 1/6으로 줄일 것이다.
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri); //해당 uri로 bitmap을 만들고,
        Bitmap resize = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true ); //resize

        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        File mediaFile = new File(getImage, timeStamp+ ".jpg");
        OutputStream out = null;

        //resize된 이미지를 바탕으로 새로운 파일을 만든 뒤, 해당하는 uri 값을 return해 준다.
        mediaFile.createNewFile();
        out = new FileOutputStream(mediaFile);
        resize.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();

        return Uri.fromFile(mediaFile);
    }
}
