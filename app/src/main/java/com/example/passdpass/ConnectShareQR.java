package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Iterator;
import java.util.List;

public class ConnectShareQR extends AppCompatActivity {


    String ssid;
    String password;
    int type;
    WifiConfiguration conf;
    WifiManager wifiManager;
    EditText qrSSID;
    EditText qrPassword;
    Button btnAddAndConnect;
    List<WifiConfiguration> listOfSavedWifis;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_share_qr);
        qrSSID = findViewById(R.id.edTxtSSID);
        qrPassword = findViewById(R.id.edTxtPassword);
        btnAddAndConnect = findViewById(R.id.add_and_connect);

        conf = new WifiConfiguration();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);

        if (getIntent().getStringExtra("wifiSSID") != null) {
            ssid = (getIntent().getStringExtra("wifiSSID"));
            password =(getIntent().getStringExtra("wifiPassword"));
            type = getIntent().getIntExtra("type", 0);
            qrSSID.setText(ssid);
            qrPassword.setText(password);




         }
        btnAddAndConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
