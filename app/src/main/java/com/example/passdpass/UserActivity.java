package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.SparseArray;
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
import java.util.Iterator;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    //Variables --------------------------------------------------------

    Button btnLogOut;


    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    TextView email_display;

    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    Button btnConnect;
    Button btnScan;
    String intentData = "";
    String intentData2 = "";
    boolean isWifi = false;

    String ssid;
    String password;
    int type;
    WifiConfiguration conf;
    WifiManager wifiManager;

    List<ScanResult> results;
    ArrayList<String> arrayList = new ArrayList<>();
    ListView listView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        conf = new WifiConfiguration();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);

        initViews();
        scanWifi();


        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifi();
            }
        });

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

    // Barcode  ->  SSID and Pass -> wifimanager
    private void initViews() {
       txtBarcodeValue= findViewById(R.id.txtBarcodeValue);
       surfaceView = findViewById(R.id.surfaceView);
       btnConnect = findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conf.SSID = ssid;
                conf.preSharedKey = password;
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                //conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiManager.addNetwork(conf);

                wifiManager.setWifiEnabled(true);

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                for( WifiConfiguration i : list ) {
                    if(i.SSID != null && i.SSID.equals(ssid)) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }


            }
        });


    }

    private void scanWifi(){
        //arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this,"Scanning Wifi...", Toast.LENGTH_SHORT).show();

    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            wifiManager.setWifiEnabled(true);

            Iterator<ScanResult> it = results.iterator();

            while (it.hasNext()) {


                ScanResult tmp = it.next();
                arrayList.add(tmp.SSID);

                System.out.println("sajat net: "+tmp.SSID);
            }

            listView = findViewById(R.id.networkListView);

            ArrayAdapter arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
            listView.setAdapter(arrayAdapter);

            for(String i: arrayList)
            {
                System.out.println("sajat net: "+i);
            }

        }
    };


// --- Barcode reader initialisation ---------------------
    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

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
            public void surfaceDestroyed(SurfaceHolder holder)  {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {

                            if (barcodes.valueAt(0).wifi != null) {

                                txtBarcodeValue.removeCallbacks(null);
                                txtBarcodeValue.setText(barcodes.valueAt(0).wifi.ssid);
                                ssid = barcodes.valueAt(0).wifi.ssid;
                                password = barcodes.valueAt(0).wifi.password;
                                type = barcodes.valueAt(0).wifi.encryptionType;

                                intentData = barcodes.valueAt(0).wifi.ssid;
                            } else {
                                isWifi = false;
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }




    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }



}