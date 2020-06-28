package com.example.miniproject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.melnykov.fab.FloatingActionButton;
;
import java.util.List;

public class ListviewFragment extends Fragment {
    FloatingActionButton fab; // + 버튼
    List<MapData> mapDatas; //ListView에 담길 MapData들의 List
    ListView listView;
    DBHelper dbHelper; //DBHelper
    ListAdapter adapter; //adapter

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dbHelper == null) {
            dbHelper = new DBHelper(getContext(), "mydb", null, DBHelper.DB_VERSION);
        } //mydb라는 이름을 가진 DB를 다룬다는 것을 명시해 둔다. 참고로 사진+좌표에 관한 (MapData에 관한)
          //db의 이름은 mydb라고 두고 사용하고 있다.
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //view 객체에 해당 fragment layout을 inflate 해 준다.
        View view = inflater.inflate(R.layout.fragment_listview, null);
        listView = (ListView) view.findViewById(R.id.list);
        //dbHelper를 통해 db에 있는 모든 데이터를 불러오는 함수를 호출한다. 해당 값을 mapDatas에 담는다.
        mapDatas = dbHelper.getData();
        adapter = new ListAdapter(getActivity(), mapDatas);
        listView.setAdapter(adapter);

        //listview의 item이 click되면
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("id", mapDatas.get(i).getId()); //string to integer
                startActivity(intent);
            }
        });


        //fab을 클릭하면 PlusActivity로 넘어가게 된다.
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), PlusActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    //해당 fragment가 다시 켜 질 때마다 실행되는 것이다.
    @Override
    public void onResume() {
        super.onResume();

        //변경된 정보가 있을지 모르니 getData()를 이용해 값을 불러온 뒤, list의 adapter를 새로 할당해 준다.
        mapDatas = dbHelper.getData();
        adapter = new ListAdapter(getActivity(), mapDatas);
        listView.setAdapter(adapter);
    }

}
