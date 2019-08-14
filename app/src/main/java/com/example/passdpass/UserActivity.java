package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    //Variables --------------------------------------------------------
    SurfaceView surfaceView;
    Button btnLogOut;
    Button btnConnect;

    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    TextView email_display;

    String ssid;
    String password;
    int type;
    WifiConfiguration conf;
    WifiManager wifiManager;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    boolean isWifi = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        initViews();

        conf = new WifiConfiguration();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);

// --- List for DUMMY Available Networks to be implemented later -------------------------
        ListView listView = findViewById(R.id.networkListView);
        ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add("Network one");
        arrayList.add("Hálózat kettő");
        arrayList.add("Retea Harom");
        arrayList.add("Chicken Tyuk");
        arrayList.add("Network one");
        arrayList.add("Hálózat kettő");
        arrayList.add("Retea Harom");
        arrayList.add("Chicken Tyuk");
        arrayList.add("Network one");
        arrayList.add("Hálózat kettő");
        arrayList.add("Retea Harom");
        arrayList.add("Chicken Tyuk");

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);

        listView.setAdapter(arrayAdapter);

// --- Firebase Authentication ---------------------
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String curentemail = user.getEmail();

        email_display = findViewById(R.id.emailID_UA);
        email_display.setText(curentemail);
        btnLogOut = findViewById(R.id.btnLogOut);

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                Intent I = new Intent(UserActivity.this, ActivityLogin.class);
                startActivity(I);

            }
        });

    }
// --- Barcode reader initialisation ---------------------
    private void initViews() {
       surfaceView = findViewById(R.id.surfaceView);
       txtBarcodeValue= findViewById(R.id.txtBarcodeValue);
       btnConnect = findViewById(R.id.btnConnect);


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intentData.length() > 0) {
                    conf.SSID = ssid;
                    conf.preSharedKey = password;
                }
            }
        });


    }
    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

            System.out.println("sajat net: " );



        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(UserActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(UserActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });


        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent switchIntent = new Intent(UserActivity.this, NetworkAdder.class);
                startActivity(switchIntent);
            }
        });

    }




    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }



}