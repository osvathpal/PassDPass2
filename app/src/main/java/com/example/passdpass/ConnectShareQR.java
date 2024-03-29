package com.example.passdpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

public class ConnectShareQR extends AppCompatActivity {

    private static final String TAG = "PassDPass";
    private String ssid;
    private  String password;
    private  int type;
    private  String toShare;
    private  String wifiID;
    private  WifiConfiguration conf;
    private  WifiManager wifiManager;
    private  List<WifiConfiguration> listOfSavedWifis;
    private  List<WifiConfig> wifiList;
    private  int checkSaved = 0;
    private FirebaseFirestore  db;
    private String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_share_qr);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String curentemail = user.getEmail();
        TextView email_display = findViewById(R.id.emailID_UA);
        email_display.setText(curentemail);


        db = FirebaseFirestore.getInstance();

        EditText qrSSID = findViewById(R.id.edTxtSSID_MN);
        EditText qrPassword = findViewById(R.id.edTxtPassword_MN);
        Button btnAddAndConnect = findViewById(R.id.update_and_connect_MN);
        Button btnShare = findViewById(R.id.btnShare_MN);
        ImageView qrImage = findViewById(R.id.QR_Image_MN);

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
            String toBarcode = "WIFI:T:WPA;S:" + ssid + ";P:" + password + ";;";
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

        db.collection("wifis").whereEqualTo("userId", userID).whereEqualTo("ssid", ssid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.getResult().isEmpty()) { checkSaved = 0;}
                        else{
                        checkSaved = 1;
                        Toast.makeText(ConnectShareQR.this, "Wifi already in Database...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        return checkSaved;
    }


}
