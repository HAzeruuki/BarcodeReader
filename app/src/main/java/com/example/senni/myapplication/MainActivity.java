package com.example.senni.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity {

    public final static int RESULT_LOAD_IMAGE = 0;
    private final static int MY_PERMISSION_STORAGE = 1;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_INTERNET = 1;

    TextView textView, nameTextView, barcodeTextView, priceTextView;
    TextView nameIndicator, barcodeIndicator, priceIndicator;
    Button button;
    Button button2;
    Button button3;
    Toolbar toolbar;
    ResultDatabase database;

    private static boolean clicked1 = false;
    private static boolean clicked2 = false;

    public BarcodeDetector detector;
    public ZXingScannerView scannerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_STORAGE);
                }
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                }
                if(checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET);
                }
            }

            database = (ResultDatabase) new ResultDatabase().execute();
            scannerView = new ZXingScannerView(this);

            createStuff();

            detector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.ALL_FORMATS).build();



            if (!detector.isOperational()) {
                textView.setText(R.string.detector_error);
            }
        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setContentView(R.layout.activity_main);
        createStuff();
    }

    @Override
    public void onPause() {
        super.onPause();

        scannerView.stopCameraPreview();
        scannerView.stopCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        scannerView.stopCameraPreview();
        scannerView.stopCamera();
        scannerView.destroyDrawingCache();
    }

    @Override
    public void onStop() {
        super.onStop();

        scannerView.stopCameraPreview();
        scannerView.stopCamera();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        scannerView.stopCameraPreview();
        scannerView.stopCamera();
        scannerView.destroyDrawingCache();
    }


    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        try {
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (selectedImage != null) {
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();

                            nameTextView.setVisibility(View.INVISIBLE);
                            barcodeTextView.setVisibility(View.INVISIBLE);
                            priceTextView.setVisibility(View.INVISIBLE);
                            button3.setVisibility(View.INVISIBLE);
                            nameIndicator.setVisibility(View.INVISIBLE);
                            barcodeIndicator.setVisibility(View.INVISIBLE);
                            priceIndicator.setVisibility(View.INVISIBLE);

                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                        processData(bitmap);
                        }
                    }
                }
            }
            catch (Exception e){
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                setContentView(R.layout.activity_main);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            if (requestCode == MY_PERMISSION_STORAGE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            if (requestCode == REQUEST_CAMERA) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            if (requestCode == REQUEST_INTERNET) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(clicked2){
            scannerView.setResultHandler(new ZXingResultHandler(MainActivity.this));
            scannerView.setMinimumWidth(ZXingScannerView.LayoutParams.MATCH_PARENT);
            scannerView.setMinimumHeight(ZXingScannerView.LayoutParams.MATCH_PARENT);
            scannerView.setShouldScaleToFill(true);
        }
    }

    public void createStuff(){
        try {
            textView = findViewById(R.id.textView);
            button = findViewById(R.id.button);
            button2 = findViewById(R.id.button_scan);
            nameTextView = findViewById(R.id.textView_name);
            barcodeTextView = findViewById(R.id.textView_barcode);
            priceTextView = findViewById(R.id.textView_price);
            nameIndicator = findViewById(R.id.textView_name_indicator);
            barcodeIndicator = findViewById(R.id.textView_barcode_indicator);
            priceIndicator = findViewById(R.id.textView_price_indicator);
            button3 = findViewById(R.id.button_price);
            toolbar = findViewById(R.id.appbar);

            setSupportActionBar(toolbar);

            if (clicked2 || clicked1 && !textView.getText().equals("")){
                textView.setText(database.getResult());

                nameTextView.setVisibility(View.VISIBLE);
                barcodeTextView.setVisibility(View.VISIBLE);
                priceTextView.setVisibility(View.VISIBLE);
                button3.setVisibility(View.VISIBLE);
                nameIndicator.setVisibility(View.VISIBLE);
                barcodeIndicator.setVisibility(View.VISIBLE);
                priceIndicator.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void processData(Bitmap myBitmap) {
        try {
            Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
            SparseArray<Barcode> barcodes = detector.detect(frame);

            if (barcodes.size() <= 0){
                textView.setText(R.string.barcode_error);
                return;
            }

            Barcode thisCode = barcodes.valueAt(0);
            textView.setText(thisCode.rawValue);
            database.giveResult(textView.getText().toString(), MainActivity.this);
            database.searchDatabase(textView.getText().toString());
            createStuff();
        }
        catch (Exception e){
            textView.setText(R.string.null_value);
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onClickListener1(View view) {
        clicked1 = true;
        clicked2 = false;
        database = (ResultDatabase) new ResultDatabase().execute();
        try{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RESULT_LOAD_IMAGE);
        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickListener2(View view) {
        clicked1 = false;
        clicked2 = true;
        database = (ResultDatabase) new ResultDatabase().execute();
        try{
            nameTextView.setVisibility(View.INVISIBLE);
            barcodeTextView.setVisibility(View.INVISIBLE);
            priceTextView.setVisibility(View.INVISIBLE);
            button3.setVisibility(View.INVISIBLE);
            nameIndicator.setVisibility(View.INVISIBLE);
            barcodeIndicator.setVisibility(View.INVISIBLE);
            priceIndicator.setVisibility(View.INVISIBLE);

            scannerView.setResultHandler(new ZXingResultHandler(MainActivity.this));
            scannerView.setMinimumWidth(ZXingScannerView.LayoutParams.MATCH_PARENT);
            scannerView.setMinimumHeight(ZXingScannerView.LayoutParams.MATCH_PARENT);
            scannerView.setShouldScaleToFill(true);
            scannerView.startCamera();
            setContentView(scannerView);

        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickListener3(View view) {
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialog));
            builder.setTitle(R.string.dialog_title).setMessage(R.string.dialog_message);
            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setView(input);
            builder.setPositiveButton(R.string.dialog_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    database.changePrice(input.getText().toString());
                }
            });
            builder.show();
        }
        catch (Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }
}
