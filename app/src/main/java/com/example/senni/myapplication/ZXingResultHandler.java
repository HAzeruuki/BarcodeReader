package com.example.senni.myapplication;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ZXingResultHandler implements ZXingScannerView.ResultHandler {
    private Context context;
    private Activity activity;

    ZXingResultHandler(Context context){
        this.context = context;
        activity = (Activity) context;
    }

    @Override
    public void handleResult(Result result) {
        try {
            String scanResult = result.getText();
            ((MainActivity)activity).database.giveResult(scanResult, context);

            ((MainActivity)activity).scannerView.stopCameraPreview();
            ((MainActivity)activity).scannerView.stopCamera();
            ((MainActivity)activity).scannerView.destroyDrawingCache();
            activity.setContentView(R.layout.activity_main);
            ((MainActivity)activity).createStuff();
        }
        catch (Exception e){
            ((MainActivity)activity).database.giveResult(String.valueOf(R.string.result_empty), context);
            Toast.makeText(activity.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
