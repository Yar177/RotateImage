package com.example.a10190270.rotateimage;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button addImage;
    private ImageView imageView;
    private TextView mImgL;
    private TextView mImgInfo;
    private TextView mMemL;
    private TextView mMemInfo;

    private final int REQ_CODE_PICTURES = 1021;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private final String TAG = "MainActivity --> ";

    private ArrayList<String> selectedPhotos = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addImage = (Button)findViewById(R.id.btn_addImage);
        addImage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPictures();
                    }
                });


        imageView = (ImageView) findViewById(R.id.imageView);



    }


    private void openPictures(){
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQ_CODE_PICTURES);
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_CODE_PICTURES) {
                Uri uri = data.getData();
                    try {
                        getExifInfo(uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }

        }

    }


    private ActivityManager.MemoryInfo getAvaliableMemory(){
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;

    }



    public  void getExifInfo(Uri uri) throws IOException {


        if (isStoragePermissionGranted()) {

//            ActivityManager.getMyMemoryState(getExifInfo(uri));
            ActivityManager.MemoryInfo memoryInfo = getAvaliableMemory();

            if (!memoryInfo.lowMemory){
                Log.i("MemoryCheck", "==========>>> Passed Memory Check");

            }else {
                Log.i("MemoryCheck", "==========>>> Didn't Pass Memory Check");
            }






            try {

                InputStream is = this.getContentResolver().openInputStream(uri);
                //tried this with
                Bitmap bitmap = BitmapFactory.decodeStream(is);

                int size =  bitmap.getAllocationByteCount();

                if ((size*3) >= memoryInfo.threshold) Log.i("Low memry", "H===================>>>ere");
                else Log.i("Low memry", "NOOOOT ==========>>>  H===================>>>ere");

                is.close();

                try{


                InputStream inputStream = this.getContentResolver().openInputStream(uri);




                ExifInterface ei = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try{
                        ei = new ExifInterface(inputStream);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } else {
                    try{
                        ei = new ExifInterface(uri.getEncodedPath());
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);

                Bitmap rotatedBitmap = null;

                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }

                imageView.setImageBitmap(rotatedBitmap);

                    saveImage(rotatedBitmap);

                    inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }}catch (Exception e){
                e.printStackTrace();
            }

        }else {
            return;
        }

//


    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }




    public static String saveImage(Bitmap image) {
        String savedImagePath = null;

        int size =  image.getAllocationByteCount();

        if (size != 0) Log.i("Low memry", "H===================>>>ere");
        else Log.i("Low memry", "NOT ======> H===================>>>ere");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        String TEMP_FOLDER = "/temp/.kodak";
        String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + TEMP_FOLDER;
        File storageDir = new File(tempFolder);

        boolean success = true;

        if (!storageDir.exists()){
            success = storageDir.mkdirs();
        }
        if (success){
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 95, fOut);
                fOut.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        return savedImagePath;

    }





    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                   return;
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }



//MainActivity end
}