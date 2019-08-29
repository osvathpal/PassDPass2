package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetworksOnPhone extends AppCompatActivity {


    WifiManager wifiManager;
    List<ScanResult> results;
    ListView listViewWifis;
    List<WifiConfig> wifiList;
    List<WifiConfiguration> savedWifisList;
    ArrayList<String> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networks_on_phone);
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);


        listViewWifis = findViewById(R.id.wifisOnPhone);
        wifiList = new ArrayList<>();
        savedWifisList = new ArrayList<>();
        wifiManager.setWifiEnabled(true);
        savedWifisList = wifiManager.getConfiguredNetworks();



        Iterator<WifiConfiguration> it = savedWifisList.iterator();

        while (it.hasNext()) {
            WifiConfiguration tmp = it.next();
            WifiConfig wifiConfig = new WifiConfig();
            wifiConfig.setSsid(tmp.SSID);

            wifiList.add(wifiConfig);
            System.out.println("Available Networks: "+tmp.SSID);
        }

        WifiList adapter = new WifiList(NetworksOnPhone.this, wifiList);
        listViewWifis.setAdapter(adapter);


    }

}
