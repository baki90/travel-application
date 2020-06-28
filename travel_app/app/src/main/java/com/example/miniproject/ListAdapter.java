package com.example.miniproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class ListAdapter extends BaseAdapter {
    private List<MapData> mapContents;
    private Activity context;

    public ListAdapter(Activity _context, List<MapData> _mapContents) {
        mapContents = _mapContents;
        context = _context;
    }

    @Override
    public int getCount() {
        return mapContents.size();
    }

    @Override
    public Object getItem(int position) {
        return mapContents.get(position).getTitle();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title = null; //제목
        TextView snippet = null; //내용
        ImageView image = null; //사진

        if (convertView == null) {
            //layout을 inflate하여서 convertView의 참조를 획득한다. 해당 layout_item에는 각 list를 이루고 있는
            //형태를 지정해 놓았는데, 아래에서 각각을 link해 준다.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_item, parent, false);
        }

        //제목과 내용, 이미지를 각각의 layout에 설정해 준다.
        title = (TextView) convertView.findViewById(R.id.title_content);
        snippet = (TextView) convertView.findViewById(R.id.snippet_content);
        image = (ImageView) convertView.findViewById(R.id.imageView);
        title.setText(mapContents.get(position).getTitle());
        snippet.setText(mapContents.get(position).getContent());

        Uri imageUri = mapContents.get(position).getUri();
        try {
            //MdidaStore.Images.Media.getBitmap()을 이용하여 URI를 bitmap 형식으로 바꿔 준다.
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            image.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
