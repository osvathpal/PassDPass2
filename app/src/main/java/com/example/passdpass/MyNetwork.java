package com.example.passdpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyNetwork extends AppCompatActivity {

    String ssid;
    String password;
    String passwordUpdate;
    int type;
    String userId;
    String toBarcode;
    String toShare;
    ImageView qrImage;
    String wifiID;

    WifiConfiguration conf;
    WifiManager wifiManager;
    TextView qrSSID;
    EditText qrPassword;
    Button btnUpdateConnect;
    Button btnShare;
    Button btnGenerateQR;
    Button btnDelete;

    List<WifiConfig> wifiList;

    TextView email_display;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db;

    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_network);

        db = FirebaseFirestore.getInstance();

        qrSSID = findViewById(R.id.edTxtSSID_MN);
        qrPassword = findViewById(R.id.edTxtPassword_MN);


        btnUpdateConnect =findViewById(R.id.update_and_connect_MN);
        btnShare = findViewById(R.id.btnShare_MN);
        btnGenerateQR = findViewById(R.id.btnGenerateQR_MN);
        btnDelete = findViewById(R.id.btnDelete);

        ssid = (getIntent().getStringExtra("wifiSSID"));
        password =(getIntent().getStringExtra("wifiPassword"));
        type = getIntent().getIntExtra("type", 0);
        wifiID = getIntent().getStringExtra("wifiID");



        qrSSID.setText(ssid);
        qrPassword.setText(password);




        btnUpdateConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                wifiList = new ArrayList<>();

                passwordUpdate = qrPassword.getText().toString().trim();

                WifiConfig wifiConfig = new WifiConfig();
                wifiConfig.setSsid(ssid);
                wifiConfig.setPassword(passwordUpdate);
                wifiConfig.setUserId(userID);
                wifiConfig.setType(type);
                wifiConfig.setId(wifiID);

                /*--------update ----------------*/
                db.collection("wifis").document(wifiConfig.getId())
                        .update(
                                "password",passwordUpdate
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MyNetwork.this, "Wifi Config updated in Database", Toast.LENGTH_LONG).show();
                            }
                        });





                /*--------add ---
                dbWifis.add(wifiConfig)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(MyNetwork.this, "Wifi Config added to Database", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MyNetwork.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });



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
                }-------------*/
                startActivity(new Intent(MyNetwork.this, MySavedNetworks.class));



            }
        });






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





    }
}
