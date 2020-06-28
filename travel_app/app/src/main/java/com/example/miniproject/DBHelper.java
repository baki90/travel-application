package com.example.miniproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.List;

//해당 class는 SQLite Query문을 이용해 DB에 접근하는 함수들을 담고 있다.
public class DBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 2; //VERSION = 2
    private Context context; //Context

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        StringBuffer sb = new StringBuffer(); //StringBuffer의 append 함수를 이용해 String을 구성한다.
        sb.append(" CREATE TABLE MAP_TABLE ( ");
        sb.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(" TITLE TEXT, "); //title
        sb.append(" CONTENT TEXT, "); //content
        sb.append(" FILE_PATH TEXT, "); //file_path
        sb.append(" LATITUDE DOUBLE, "); //lat
        sb.append(" LONGITUDE DOUBLE ) "); //lng
        //해당 DB는 아래와 같이 구성되어 있다.
        // _ID | TITLE | CONTENT | FILE_PATH | LATITUDE | LONGITUDE

        db.execSQL(sb.toString()); //DB의 table을 생성하는 Query를 DB에 실행시킨다.

        Toast.makeText(context, "make map table", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Toast.makeText(context, "버전이 올라갔습니다.", Toast.LENGTH_SHORT).show();
    }

    public void mapDB() {
        SQLiteDatabase db = getReadableDatabase();
    } //읽기 모드로 DB를 불러온다.

    //id 인자를 통해서 해당하는 row를 지우는 함수이다.
    public void deleteDataById(int id) {

        StringBuffer sb = new StringBuffer();
        //_ID 값이 Argument로 받아온 id 값과 동일하다면 해당 MAP_TABLE의 ROW를 제거한다.
        sb.append(" DELETE FROM MAP_TABLE WHERE _ID = " + id + " ");
        SQLiteDatabase db = getWritableDatabase(); //DB를 쓰기 모드로 불러온다.
        db.execSQL(sb.toString());

    }

    //DB의 모든 data를 제거한다.
    public void deleteAll(){
        StringBuffer sb = new StringBuffer();
        sb.append(" DELETE FROM MAP_TABLE "); //해당 TABLE의 모든 내용을 삭제한다.

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sb.toString());
    }

    //MapData Object를 받아온 뒤, DB에 DATA를 INSERT해 주는 함수이다.
    public void addMapData(MapData mapData){
        SQLiteDatabase db = getWritableDatabase(); //DB를 읽기 모드로 불러온다.

        StringBuffer sb = new StringBuffer();
        //INSERT 문을 통하여 MAP_TABLE이라는 TABLE에 해당하는 변수들에게 값을 넣어 준다.
        sb.append(" INSERT INTO MAP_TABLE ( ");
        sb.append(" TITLE, CONTENT, FILE_PATH, LATITUDE, LONGITUDE ) ");
        sb.append(" VALUES ( ?, ?, ?, ?, ? ) ");

        // execSQL 함수를 실행시켜서 MAPDATA로 받아온 argument 값을 바탕으로 db에 값을 삽입해 준다.
        // 이는 위의 insert문에서 변수 순으로 삽입한다.
        db.execSQL(sb.toString(), new Object[]{
                mapData.getTitle(), mapData.getContent(), mapData.getUri().toString(),
                mapData.getLatLng().latitude, mapData.getLatLng().longitude
        });
        Toast.makeText(context, "Insert OK", Toast.LENGTH_SHORT).show();

    }

    //현재 DB의 모든 data 값을 list 형태로 return하는 함수이다.
    public List<MapData> getData(){

        List<MapData> mapDatas = new LinkedList<>(); //LIST 객체를 생성해 준다.

        StringBuffer sb = new StringBuffer();
        //MAP_TABLE에서 select된 변수들을 차례로 받아온다.
        sb.append(" SELECT _ID, TITLE, CONTENT, FILE_PATH, LATITUDE, LONGITUDE FROM MAP_TABLE ");

        SQLiteDatabase db = getReadableDatabase(); //DB를 읽기 모드로 호출한 뒤
        Cursor cursor = db.rawQuery(sb.toString(), null); //쿼리를 보낸 후, 받은 값을 cursor에 담는다.

        while(cursor.moveToNext()){ //cursor는 한 row씩 지니고 있는데, 다음 값이 존재할 때까지 list 객체에 map을 차례로 add한다.
            mapDatas.add(new MapData(cursor.getInt(0), new LatLng(cursor.getDouble(4), cursor.getDouble(5)),
                    Uri.parse(cursor.getString(3)), cursor.getString(1), cursor.getString(2)));
        }

        return mapDatas; //db로부터 받은 값들의 객체의 집합을 return한다.
    }

    //해당 함수는 ID에 따라서 MapData를 return하는 함수이다.
    public MapData getMapdataById(int _id){

        StringBuffer sb = new StringBuffer();
        //WHERE문을 통해 id에 해당하는 row를 찾아서 해당하는 변수들의 값을 받아온다.
        sb.append(" SELECT TITLE, CONTENT, FILE_PATH, LATITUDE, LONGITUDE FROM MAP_TABLE WHERE _ID= ? ");

        SQLiteDatabase db = getReadableDatabase(); //DB를 읽기 전용 모드로 open한 뒤,
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{_id + ""}); //Cursor에 받아온 값들을 담는다.
        //(실제로 id는 primary 값이므로 한 row만 받아오기 때문에 이보다 간단하게 구현할 수 있을 것 같다.)

        MapData mapData = new MapData(); //MapData 객체를 생성해 준 뒤,
        if(cursor.moveToNext()){ //각자 맞는 위치에 setting을 해 준다.
            mapData.setLatLng(new LatLng(cursor.getDouble(3), cursor.getDouble(4)));
            mapData.setTitle(cursor.getString(0));
            mapData.setContent(cursor.getString(1));
            mapData.setUri(Uri.parse(cursor.getString(2)));
        }

        return mapData; //이후 해당 객체를 반환한다.
    }


}
