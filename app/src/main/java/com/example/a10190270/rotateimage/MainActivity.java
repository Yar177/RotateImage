package com.example.a10190270.rotateimage;

import android.Manifest;
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
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button addImage;
    private ImageView imageView;

    private final int REQ_CODE_PICTURES = 1021;

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
                Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG);

                if (uri != null) {
                    String imageURI = uri.toString();
                    Log.i(TAG, "Selected Photo URI: " + imageURI);
                    //  selectedPhotos.add(imageURI);

                    File myFile = new File(uri.toString());
                    String path = String.valueOf(myFile.getAbsoluteFile());
                    selectedPhotos.add(path);

                    imageView.setImageURI(uri);


                    //String filePath = getRealPathFromURI(this, uri);

                    try {
                        getExifInfo(uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

//                    int orientation = getDegreesExifOrientation(path);

                   // Log.i("EXIF",  filePath);
                }
            }

         }

        }



    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

        public  void getExifInfo(Uri uri) throws IOException {
            if (isStoragePermissionGranted()) {


                File file = new File(uri.toString());
                String filePath = file.getAbsolutePath();

                // InputStream inputStream = new FileInputStream(uri.getPath());
                String path = String.valueOf(file.getAbsolutePath());
                String path1 = String.valueOf(file.getPath());


//                BitmapFactory.Options options = new BitmapFactory.Options();
//
//
//                options.inJustDecodeBounds = true;
//
//                BitmapFactory.decodeFile(filePath, options);
//
//                int height = options.outHeight;
//                int width = options.outWidth;

                try {

                    InputStream is = this.getContentResolver().openInputStream(uri);

                    //tried this with
                    Bitmap bitmap = BitmapFactory.decodeStream(is);

                    //Bitmap bitmap = BitmapFactory.decodeFile(path);
                    //Bitmap bitmap = BitmapFactory.decodeFile(path1);
                    //Bitmap bitmap = BitmapFactory.decodeStream(inputStream);


//                    InputStream input = this.getContentResolver().openInputStream(uri);
                    ExifInterface ei = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        ei = new ExifInterface(is);
                    } else {
                        ei = new ExifInterface(uri.getEncodedPath());
                    }
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

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
                } catch (Exception e) {
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



//MainActivity end
}
