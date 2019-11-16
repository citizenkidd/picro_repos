package com.example.picro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener, ZXingScannerView.ResultHandler, FirebaseController.ResultHandler{

    private FirebaseController firebaseController = new FirebaseController();
    private ZXingScannerView scannerView;
    RelativeLayout buttonBack;
    LinearLayout progress;
    String uid;

    Intent intentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseController.setResultHandler(this);

        // view scanner init
        scannerView = (ZXingScannerView)findViewById(R.id.rxscan);
        getSupportActionBar().hide();

        // back button init
        buttonBack = findViewById(R.id.backButton);
        buttonBack.setOnClickListener(this);

        // hide the progress bar
        progress = findViewById(R.id.progressBar);
        progress.setVisibility(View.INVISIBLE);

        // permission
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(ActivityLogin.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getApplicationContext(), "You must grant access",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
    }

    @Override
    protected void onStop() {
        scannerView.stopCamera();
        super.onStop();
    }

    @Override
    public void handleResult(Result result) {
        scannerView.startCamera();                              // restart the camera for next scanning
        progress.setVisibility(View.VISIBLE);                   // set progress bar to show
        uid = String.valueOf(result);                           // convert result to string
        String temp_path = "picro_cards_manifest/" + uid;       // search for the card in cards_manifest
        firebaseController.getChildValueOneTime(temp_path);     // trigger the firebase module by using the previous path
    }

    @Override
    public void onClick(View view) {

        int selector = view.getId();

        if(selector == R.id.backButton){
            scannerView.stopCamera();
            finish();
        }

    }

    @Override
    public void setValueStatus(String path, int status) {

    }

    @Override
    public void valueListener(String path, DataSnapshot data) {

        // variable definition
        int cardStatus = Integer.parseInt(data.child("stats").getValue().toString());
        String cardUid = data.child("card_uid").getValue().toString();

        // do register
        if(cardStatus == 0){
            intentSettings = new Intent(ActivityLogin.this, ActivityRegister.class);
        }

        // do login
        else if(cardStatus == 1){
            intentSettings = new Intent(ActivityLogin.this, ActivitySplash.class);
            setShareData(uid);
        }

        startActivity(intentSettings);

    }

    // set share data
    public void setShareData(String data){
        SharedPreferences shared = getSharedPreferences("rootUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString("auth_code", data);
        editor.commit();
    }

    // get share data
    public String getShareData(String selector){
        SharedPreferences shared = getSharedPreferences("rootUser", Context.MODE_PRIVATE);
        return shared.getString(selector, null);
    }

}
