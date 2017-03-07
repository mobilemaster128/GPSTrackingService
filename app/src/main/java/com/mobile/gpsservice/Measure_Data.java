package com.mobile.gpsservice;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by PC on 8/2/2016.
 */
public class Measure_Data {
    //private variables
    float[] Accel = new float[3];
    String Date_time;
    String ID;
    int Bat_level;
    double Lat;
    double Long;
    double Alt;
    float speed;

    // Empty constructor
    public Measure_Data(){
        Accel = new float[]{0, 0, 0};
        Date_time = "";
        ID = "";
        Bat_level = 50;
        Lat = 0;
        Long = 0;
        Alt = 0;
        speed = 0;
    }

    public JSONObject getRequestObject() {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("ID", ID);
            requestObject.put("Battery", Bat_level);
            requestObject.put("Speed", speed);
            requestObject.put("Latitude", Lat);
            requestObject.put("Longitude", Long);
            requestObject.put("XAccel", Accel[0]);
            requestObject.put("YAccel", Accel[1]);
            requestObject.put("ZAccel", Accel[2]);
        }
        catch (JSONException ex) {
            return null;
        }
        return requestObject;
    }

    public void setLocation(double Lat, double Long, double Alt){
        this.Lat = Lat;
        this.Long = Long;
        this.Alt = Alt;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void  setAccel(float[] accel) {
        float temp;
        temp = accel[0];
        this.Accel[0] = temp;
        temp = accel[1];
        this.Accel[1] = temp;
        temp = accel[2];
        this.Accel[2] = temp;
    }

    public void setBat(int bat) {
        this.Bat_level = bat;
    }

    public void setDateTime(Date dt) {
        this.Date_time = dt.toString();
    }

    public void setID (String ID) {
        this.ID = ID;
    }

    // getting ID
    public String getID(){
        return this.ID;
    }

    public float getSpeed() {return this.speed;}

    public Date getDate_time() {
        Date date = new Date();

        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Date_time);
        }
        catch (ParseException exception) {

        }
        return date;
    }

    public float[] getAccel() {
        return Accel;
    }

    public int getBat() {
        return Bat_level;
    }
}
