package com.example.passdpass;

public class WifiConfig {

    private String ssid;
    private String password;
    private int type;
    private String userId;


    public WifiConfig() {
    }

    public WifiConfig(String ssid, String password, int type, String userId) {
        this.ssid = ssid;
        this.password = password;
        this.type = type;
        this.userId = userId;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userID) {
        this.userId = userID;
    }

}
