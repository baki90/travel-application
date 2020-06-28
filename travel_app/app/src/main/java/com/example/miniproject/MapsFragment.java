package com.example.miniproject;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.melnykov.fab.FloatingActionButton;

import java.util.Iterator;
import java.util.List;

//map을 띄우고 DB에서 받아온 정보로 map에 마커를 찍는다. 마커를 클릭하면 상세 페이지로 넘어간다.
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private DBHelper dbHelper;
    private List<MapData> mapDatas;
    private MapView mapView;
    private GoogleMap mMap;
    public FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //"mydb"이름을 가진 DB를 불러온다.
        if (dbHelper == null) {
            dbHelper = new DBHelper(getContext(), "mydb", null, DBHelper.DB_VERSION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_maps, container, false);

        mapView = (MapView) layout.findViewById(R.id.map);
        mapView.getMapAsync(this);

        //+ button 클릭 시, PlusActivity가 실행된다.
        fab = (FloatingActionButton) layout.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlusActivity.class);
                startActivity(intent);
            }
        });

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        getData();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
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

    //해당 함수는 DB에서 모든 data값들을 가져와 Map 위에 Marker로 찍는 함수이다.
    public void getData(){
        mapDatas = dbHelper.getData(); //DB에서 data를 가지고 온 뒤,
       if(mMap != null){
           mMap.clear(); //mMap을 clear한다. (resume에서도 해당 함수가 실행되기 때문)
           //이후 가지고 온 data 값을 바탕으로 markerOption에서 위경도를 설정해 준다. title은 해당 db에서 해당
           //값에 접근해야 하므로 id 값을 지정해 준 뒤, MarkerClick 시 intent.putExtra로 다음 activity에 값을 넘기는 데 사용
           for (Iterator<MapData> itr = mapDatas.iterator(); itr.hasNext();) {
               MapData mapData = itr.next();
               MarkerOptions markerOptions = new MarkerOptions();
               markerOptions.position(mapData.getLatLng()).title(String.valueOf(mapData.getId())); //int to str
               mMap.addMarker(markerOptions);
           }
       }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; //mMap에 googleMap을 할당하였다.
        getData(); //getData를 이용해 모든 Marker들을 불러온다.

        //Marker가 click되면, 해당하는 marker의 title 값을 intent에 putExtra를 통해 넣은 뒤, 엑티비티에 전달해 준다.
        //해당 title에 해당 marker가 속한 mapData의 id 값을 저장해 두었다. 이후 Detail을 보여 주는 Activity를 불러온다.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("id", Integer.valueOf(marker.getTitle())); //string to integer
                startActivity(intent);
                return true;
            }
        });
    }
}
