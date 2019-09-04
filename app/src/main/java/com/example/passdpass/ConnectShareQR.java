package com.example.passdpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConnectShareQR extends AppCompatActivity {

    private static final String TAG = "PassDPass";
    String ssid;
    String password;
    int type;
    String toBarcode;
    String toShare;
    ImageView qrImage;
    String wifiID;
    int isItSaved=0;

    WifiConfiguration conf;
    WifiManager wifiManager;
    EditText qrSSID;
    EditText qrPassword;
    Button btnAddAndConnect;
    Button btnShare;
    Button btnLogOut;

    List<WifiConfiguration> listOfSavedWifis;
    List<WifiConfig> wifiList;
    int checkSaved = 0;

    TextView email_display;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore  db;

    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_share_qr);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String curentemail = user.getEmail();
        email_display = findViewById(R.id.emailID_UA);
        email_display.setText(curentemail);


        db = FirebaseFirestore.getInstance();

        qrSSID = findViewById(R.id.edTxtSSID_MN);
        qrPassword = findViewById(R.id.edTxtPassword_MN);
        btnAddAndConnect = findViewById(R.id.update_and_connect_MN);
        btnShare = findViewById(R.id.btnShare_MN);
        btnLogOut = findViewById(R.id.btnLogOut);
        qrImage = findViewById(R.id.QR_Image_MN);

        conf = new WifiConfiguration();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);



        if (getIntent().getStringExtra("wifiSSID") != null) {
            ssid = (getIntent().getStringExtra("wifiSSID"));
            password = (getIntent().getStringExtra("wifiPassword"));
            type = getIntent().getIntExtra("type", 0);
            qrSSID.setText(ssid);
            qrPassword.setText(password);

            /* - Barcode generation - */
            toBarcode = "WIFI:T:WPA;S:" + ssid + ";P:" + password + ";;";
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(toBarcode, BarcodeFormat.QR_CODE, 500, 500);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                bitmap = barcodeEncoder.createBitmap(bitMatrix);
                qrImage.setImageBitmap(bitmap);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        verifyWifi(ssid);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toShare = "The Wifi: " + ssid + "\n" + " The Password: " + password + "";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing Wifi");
                intent.putExtra(Intent.EXTRA_TEXT, toShare);
                startActivity(Intent.createChooser(intent, "Share Wifi"));


            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ConnectShareQR.this, ActivityLogin.class);
                startActivity(intent);

            }
        });

        btnAddAndConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                wifiID = ssid;
                wifiList = new ArrayList<>();

                CollectionReference dbWifis = db.collection("wifis");
                WifiConfig wifiConfig = new WifiConfig();
                wifiConfig.setSsid(ssid);
                wifiConfig.setPassword(password);
                wifiConfig.setUserId(userID);
                wifiConfig.setType(type);

                if (0 == checkSaved) {
                    dbWifis.add(wifiConfig)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(ConnectShareQR.this, "Wifi Config added to Database", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ConnectShareQR.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(ConnectShareQR.this, "Wifi already in Database. You can update in the My Saved Networks section.", Toast.LENGTH_LONG).show();
                }

                conf.SSID = String.format("\"%s\"", ssid);
                conf.preSharedKey = String.format("\"%s\"", password);

                wifiManager.addNetwork(conf);
                wifiManager.setWifiEnabled(true);


                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                for (WifiConfiguration i : list) {
                    if (i.SSID != null && i.SSID.equals(ssid)) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }

                listOfSavedWifis = wifiManager.getConfiguredNetworks();

                for (WifiConfiguration tmp : listOfSavedWifis) {
                    System.out.println("Saved Networks: " + tmp.SSID + " Pass: " + tmp.preSharedKey);
                }

                startActivity(new Intent(ConnectShareQR.this, MySavedNetworks.class));

            }
        });




    }
    int verifyWifi(final String ssid){

        wifiList = new ArrayList<>();

        db.collection("wifis").whereEqualTo("userId", userID).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData()+ " => " + wifiList.size() );
                                WifiConfig wf = document.toObject(WifiConfig.class);
                                wifiList.add(wf);

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        int i=1;
                        for (WifiConfig wf : wifiList) {
                            String tempSSID = wf.getSsid();
                            if (tempSSID.equals(ssid)) {
                                checkSaved = 1;
                                Toast.makeText(ConnectShareQR.this, "Wifi already in Database..." + i, Toast.LENGTH_LONG).show();
                                i++;
                            }
                        }

                    }
                });




        return checkSaved;
    }


}
