package com.ocean.myapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.ocean.myapp.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    boolean chooseOrUpload = true;
    boolean status = false;
    String selectedDocument = "";
    private static final int REQUEST_GALLERY_CODE = 201;
    ActivityResultLauncher<Intent> activityResultLauncher;
    String imageFilePath;
    ArrayList<DataPart> dataPart = new ArrayList<>();
    Uri uri;
    List<Uri> uries = new ArrayList<Uri>();
    Bitmap bmp;
    byte[] imageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.chooseOrUploadBtn.setOnClickListener(v -> {
//            if (!chooseOrUpload){
                startDialogChoose();
//            }else {
//                TODO: uploadAttachment
//            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                Intent intent1 = result.getData();

                if (intent1 != null) {


                    if (status == false) {


                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            if (selectedDocument.equals("selectGallery")) {

                                Intent intent = result.getData();

                                onSelectFromGalleryResult(intent);

                            } else {

                                Intent intent = result.getData();

                                selectPdfFromGallery(intent);
                            }

                        }
                        status = true;

                    } else {

                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            if (selectedDocument.equals("selectGallery")) {

                                Intent intent = result.getData();

                                onSelectFromGalleryResult(intent);

                            } else {

                                Intent intent = result.getData();

                                selectPdfFromGallery(intent);
                            }
                        }
                        status = false;
                    }

                } else {

                    Toast.makeText(MainActivity.this, "Yo Don't Selected AnyThing", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void startDialogChoose() {
        final CharSequence[] items = {"Choose PDF", "Choose Camera", "Choose Images Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo or PDF!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Choose PDF")) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            selectPdf();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_GALLERY_CODE);
                        }
                    } else {
                        selectPdf();
                    }

                } else if (items[item].equals("Choose Camera")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            selectedDocument = "selectGallery";
                            chooseGallery();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_GALLERY_CODE);
                        }
                    } else {
                        selectedDocument = "selectGallery";
                        chooseGallery();
                    }

                } else if (items[item].equals("Choose Images Camera")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            selectedDocument = "selectCamera";
                            openCameraIntent();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_GALLERY_CODE);
                        }
                    } else {
                        selectedDocument = "selectCamera";
                        openCameraIntent();
                    }

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void selectPdf() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        activityResultLauncher.launch(Intent.createChooser(intent, "title1"));
    }

    private void openCameraIntent() {

        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activityResultLauncher.launch(pictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // File finalFile = FileUtils.getFile(getApplicationContext(), furi);
        imageFilePath = image.getAbsolutePath();
        binding.textView.setText(image.getName());
        try {
            DataPart dp = new DataPart();
            dp.setFileName(image.getAbsolutePath());
            //dp.setContent(inputData);
            //dp.setType("image/jpeg");
            dataPart.add(dp);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    private void chooseGallery() {
    }

    private void onSelectFromGalleryResult(@NonNull Intent data) {

        uri = data.getData();
        uries.add(uri);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                if (data.getClipData() != null) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    for (int i = 0; i < count; i++) {
                        Uri selectedImageUri = data.getClipData().getItemAt(i).getUri();

                        options.inSampleSize = calculateInSampleSize(options, 1000, 1000);
                        options.inJustDecodeBounds = false;

                        bmp = BitmapFactory.decodeStream(MainActivity.this.getContentResolver().openInputStream(selectedImageUri), null, options);


                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        imageBytes = baos.toByteArray();


                        Uri furi = getImageUri(getApplicationContext(), bmp);
                        File finalFile = FileUtils.getFile(getApplicationContext(), furi);


                        imageFilePath = finalFile.toString();
                        binding.textView.setText(finalFile.getName());
                        try {
                            InputStream iStream = getContentResolver().openInputStream(uri);
                            byte[] inputData = getBytes(iStream);
                            DataPart dp = new DataPart();
                            dp.setFileName(finalFile.getName());
                            dataPart.add(dp);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } else if (data.getData() != null) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.

                    Uri selectedImageUri = data.getData();

                    BitmapFactory.decodeStream(MainActivity.this.getContentResolver().openInputStream(selectedImageUri), null, options);

                    options.inSampleSize = calculateInSampleSize(options, 1000, 1000);
                    options.inJustDecodeBounds = false;

                    bmp = BitmapFactory.decodeStream(MainActivity.this.getContentResolver().openInputStream(selectedImageUri), null, options);


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    imageBytes = baos.toByteArray();


                    Uri furi = getImageUri(getApplicationContext(), bmp);
                    //File finalFile = new File(getRealPathFromUri(furi));
                    File finalFile = FileUtils.getFile(getApplicationContext(), furi);

                    imageFilePath = finalFile.toString();
                    binding.textView.setText(finalFile.getName());
                    try {
                        InputStream iStream = getContentResolver().openInputStream(uri);
                        byte[] inputData = getBytes(iStream);
                        DataPart dp = new DataPart();
                        dp.setFileName(finalFile.getName());
                        //dp.setContent(inputData);
                        //dp.setType("image/jpeg");
                        dataPart.add(dp);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.v("tostring", e.toString());
        }
    }

    private void selectPdfFromGallery(@NonNull Intent data) {
        uri = data.getData();
        String uriString = uri.toString();
        File myFile = new File(uriString);
        String paths = myFile.getAbsolutePath();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {

                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range") String fgd = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    @SuppressLint("Range") String exfgd = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                    binding.textView.setText(fgd);
                    imageFilePath = fgd;
                    try {
                        InputStream iStream = getContentResolver().openInputStream(uri);
                        byte[] inputData = getBytes(iStream);
                        DataPart dp = new DataPart();
                        dp.setFileName(imageFilePath);
                        //dp.setContent(inputData);
                        //dp.setType("application/pdf");
                        dataPart.add(dp);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {

            imageFilePath = myFile.getName();
            binding.textView.setText(imageFilePath);
            try {
                InputStream iStream = getContentResolver().openInputStream(uri);
                byte[] inputData = getBytes(iStream);
                DataPart dp = new DataPart();
                dp.setFileName(imageFilePath);
                //dp.setContent(inputData);
                //dp.setType("application/pdf");
                dataPart.add(dp);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Uri getImageUri(@NonNull Context inContext, @NonNull Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "IMG_" + Calendar.getInstance().getTime(), null);
        return Uri.parse(path);
    }

}