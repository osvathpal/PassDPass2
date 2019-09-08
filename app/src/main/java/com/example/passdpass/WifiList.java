package com.example.passdpass;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;



import java.util.List;

public class WifiList extends ArrayAdapter<WifiConfig> {

    private Activity context;
    private List<WifiConfig> wifiList;

    public WifiList(Activity context, List<WifiConfig> wifiList) {
        super(context, R.layout.list_view, wifiList);
        this.context = context;
        this.wifiList = wifiList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.list_view, null, true);
        TextView textViewSsid = listViewItem.findViewById(R.id.textViewSSID);
        WifiConfig wifiConfig = wifiList.get(position);
        textViewSsid.setText(wifiConfig.getSsid());
        return listViewItem;
    }
}

