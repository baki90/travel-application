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

//Diary Data DB를 이용하기 위해 사용하는 DBHelper이다.
public class DiaryDBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 2;
    private Context context;

    public DiaryDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        StringBuffer sb = new StringBuffer();
        sb.append(" CREATE TABLE MAP_TABLE ( ");
        sb.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(" TITLE TEXT, "); //title
        sb.append(" CONTENT TEXT, "); //content
        sb.append(" DATE LONG, "); //file_path
        sb.append(" LATITUDE DOUBLE, "); //lat
        sb.append(" LONGITUDE DOUBLE ) "); //lng

        db.execSQL(sb.toString());

        Toast.makeText(context, "make map table", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Toast.makeText(context, "버전이 올라갔습니다.", Toast.LENGTH_SHORT).show();
    }

    public void mapDB() {
        SQLiteDatabase db = getReadableDatabase();
    }

    public void deleteDataById(int id) {

        StringBuffer sb = new StringBuffer();
        sb.append(" DELETE FROM MAP_TABLE WHERE _ID = " + id + " ");

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sb.toString());

    }

    public void deleteAll(){
        StringBuffer sb = new StringBuffer();
        sb.append(" DELETE FROM MAP_TABLE ");

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sb.toString());
    }

    public void addDiaryData(DiaryData diaryData){
        SQLiteDatabase db = getWritableDatabase();

        StringBuffer sb = new StringBuffer();
        sb.append(" INSERT INTO MAP_TABLE ( ");
        sb.append(" TITLE, CONTENT, DATE, LATITUDE, LONGITUDE ) ");
        sb.append(" VALUES ( ?, ?, ?, ?, ? ) ");

        db.execSQL(sb.toString(), new Object[]{
               diaryData.getTitle(), diaryData.getContent(), diaryData.getDate(),
                diaryData.getLatLng().latitude, diaryData.getLatLng().longitude
        });
        Toast.makeText(context, "Insert OK", Toast.LENGTH_SHORT).show();

    }

    public List<DiaryData> getData(){

        List<DiaryData> diaryData = new LinkedList<>();

        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT _ID, TITLE, CONTENT, DATE, LATITUDE, LONGITUDE FROM MAP_TABLE ");

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sb.toString(), null);

        while(cursor.moveToNext()){
            diaryData.add(new DiaryData(cursor.getInt(0), new LatLng(cursor.getDouble(4), cursor.getDouble(5)),
                    cursor.getLong(3), cursor.getString(1), cursor.getString(2)));
        }

        return diaryData;
    }

    public DiaryData getDiaryDataByDate(long _date){
        //Log.e("shs","DB id " + _id);

        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT TITLE, CONTENT, LATITUDE, LONGITUDE FROM MAP_TABLE WHERE DATE= ? ");

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{_date + ""});

        DiaryData diaryData = new DiaryData();
        if(cursor.moveToNext()){
            diaryData.setLatLng(new LatLng(cursor.getDouble(2), cursor.getDouble(3)));
            diaryData.setTitle(cursor.getString(0));
            diaryData.setContent(cursor.getString(1));
        }
        //Log.e("shs","DB content " +mapData.getLatLng());
        return diaryData;
    }
}
