package com.example.miniproject;

import android.net.Uri;
import android.widget.ProgressBar;

import com.google.android.gms.maps.model.LatLng;

class MapData {
    private int id;
    private LatLng latLng; //위경도 값
    private Uri uri; //image link
    private String title; //title
    private String content; //content
    public MapData() {
        //image를 넣지 않았을 때의 Default Uri를 배정해 준다. 해당 사진은 drawable에 존재한다.
        uri = Uri.parse("android.resource://com.example.miniproject/drawable/daram");
    }

    public MapData(int _id, LatLng _latLng, Uri _uri, String _title, String _content) {
        id = _id;
        latLng = _latLng;
        uri = _uri;
        title = _title;
        content = _content;
    }

    public int getId() {
        return id;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public Uri getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

class DiaryData {
    private int id;
    private LatLng latLng; //위경도 값
    private long date; //date
    private String title; //title (사실 diary에서 사용하지 않는다.)
    private String content; //content

    public DiaryData() {
    }

    public DiaryData(int id, LatLng latLng, long date, String title, String content) {
        this.id = id;
        this.latLng = latLng;
        this.date = date;
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
