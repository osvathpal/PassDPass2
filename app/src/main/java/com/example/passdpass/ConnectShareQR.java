package com.example.passdpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ConnectShareQR extends AppCompatActivity {


    String ssid;
    String password;
    int type;
    String toBarcode;
    ImageView qrImage;
    String wifiID;

    WifiConfiguration conf;
    WifiManager wifiManager;
    EditText qrSSID;
    EditText qrPassword;
    Button btnAddAndConnect;
    List<WifiConfiguration> listOfSavedWifis;

    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore  db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_share_qr);







        db = FirebaseFirestore.getInstance();

        qrSSID = findViewById(R.id.edTxtSSID);
        qrPassword = findViewById(R.id.edTxtPassword);
        btnAddAndConnect = findViewById(R.id.add_and_connect);
        qrImage = findViewById(R.id.QR_Image);

        conf = new WifiConfiguration();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);

        if (getIntent().getStringExtra("wifiSSID") != null) {
            ssid = (getIntent().getStringExtra("wifiSSID"));
            password =(getIntent().getStringExtra("wifiPassword"));
            type = getIntent().getIntExtra("type", 0);
            qrSSID.setText(ssid);
            qrPassword.setText(password);

            /* - Barcode generation- */
            toBarcode = "WIFI:T:WPA;S:"+ssid+";P:"+password+";;";
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(toBarcode, BarcodeFormat.QR_CODE, 500, 500);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                qrImage.setImageBitmap(bitmap);

            } catch (WriterException e) {
                e.printStackTrace();
            }




         }
        btnAddAndConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                wifiID = ssid;
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                CollectionReference dbWifis = db.collection("wifis");
                WifiConfig wifiConfig = new WifiConfig();
                wifiConfig.setSsid(ssid);
                wifiConfig.setPassword(password);
                wifiConfig.setUserId(userID);
                wifiConfig.setType(type);



                dbWifis.add(wifiConfig)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(ConnectShareQR.this, "Wifi Config added to Firestore", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ConnectShareQR.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });


                conf.SSID = String.format("\"%s\"",ssid);
                conf.preSharedKey = String.format("\"%s\"",password);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

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



                listOfSavedWifis = wifiManager.getConfiguredNetworks();

                for (WifiConfiguration tmp : listOfSavedWifis) {
                    System.out.println("Saved Networks: " + tmp.SSID);
                }

            }
        });




    }
}
