package com.example.miniproject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PickImageHelper {

    static private String timeStamp;
    static private boolean isChecked = false;

    public static void selectImage(final Activity activity){
        isChecked = false;
        activity.startActivityForResult(getPickImageChooserIntent(activity), 9162);

    }

    public static Intent getPickImageChooserIntent(final Activity activity) {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri(activity);

        File file = new File(getRealPathFromURI(activity,outputFileUri));
        if (file.exists())
            file.delete();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = activity.getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Obter imagem");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    /**
     * Get URI to image received from capture by camera.
     */
    private static Uri getCaptureImageOutputUri(Activity activity) {
        Uri outputFileUri = null;
        File getImage = new File(Environment.getExternalStorageDirectory(), "miniProject");
        if(! getImage.exists()){
            if(!getImage.mkdirs()){
                Log.d("CameraApp", "failed to create directory");
                return null;
            }
        }
        if(!isChecked) { //현재 해당 함수가 카메라 촬영을 클릭했을 때와 확인을 눌렀을 때, 두 번 불리게 되는데 따라서
            //시간 값에 대한 데이터를 2번 받아오게 된다. 해당 과정이 없으면 지속해서 데이터가 없다고 뜨게 되므로,
            //한 번 해당 함수가 불렸을 때 저장된 데이터 값을 timeStamp에 담아 두어야, 두 번째에도 같은 파일명으로 수정되기 때문이다.
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpeg";
            isChecked = true;
        }
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), timeStamp));
        }
        return outputFileUri;
    }

    public static Uri getPickImageResultUri(Activity activity, Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri(activity) : data.getData();
    }



    public static String getRealPathFromURI(Activity activity,Uri contentUri) {
        String result;
        Cursor cursor = activity.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            result = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
