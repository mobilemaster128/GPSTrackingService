package com.mobile.gpsservice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by PC on 8/2/2016.
 */
public class GpsService extends Service {

    private GeoLocation m_geolocation;
    private GetAcceleration m_acceleration;
    private Measure_Data m_data;
    private static AsyncHttpClient client = new AsyncHttpClient();
    int limit = 0;
    int SPEED_LIMIT = 20;
    String Base_url = "http://192.168.1.106/GpsTracker/gps_tracker/user";

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the battery scale
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
            // get the battery level
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            // Display the battery level in TextView

            // Calculate the battery charged percentage
            int percentage = level * 100 / scale;

            m_data.setBat(percentage);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notify = new Notification.Builder(this)
                .setTicker("Service Message")
                .setContentTitle("GPS Tracking")
                .setContentText("Started.")
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setWhen(System.currentTimeMillis())
                .build();

        startForeground(1, notify);

        //notificationManager.notify(1, notify);
        m_data = new Measure_Data();

        m_data.setBat(getBatteryLevel());
        //m_data.setID(getData());
        m_data.setID(getUsername());
        //Toast.makeText(this, m_data.getID(), Toast.LENGTH_SHORT).show();

        m_geolocation = new GeoLocation(this, m_data);
        m_geolocation.StartGeoLocatiom(30000);

        m_acceleration = new GetAcceleration(this, m_data);
        m_acceleration.StartAcceleration();

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getApplicationContext().registerReceiver(mBroadcastReceiver, iFilter);

        handler.sendEmptyMessage(0);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public int getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50;
        }

        return (level * 100 / scale);
    }

    private String getData() {
        //Getting all registered Google Accounts;
        String name = "";
        try {
            Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
            for (Account account : accounts) {
                String type = account.type;
                name = account.name;
                break;
            }
        } catch (Exception e) {
            Log.i("Exception", "Exception:" + e);
            return "NonAccount";
        }
        return name;
    }

    public String getUsername() {
        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");

        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            // TODO: Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type
            // values.
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");
            if (parts.length > 0 && parts[0] != null && parts[1] != null && parts[1].equals("gmail.com"))
                return parts[0];
            else
                return "NonAccount";
        } else
            return "NonAccount";
    }

    public final Handler handler = new Handler(){
        int i = 0;
        public void handleMessage(Message msg) {
            i++;
            Log.d("GPS", "handle" + i);
            if (m_data.getSpeed() >= SPEED_LIMIT) {
                handler.sendMessageDelayed(new Message(), 3000);
                if (limit == 0) {
                    limit = 100;
                    m_geolocation.setTimeout(3000);
                    m_acceleration.StartAcceleration();
                }
                /*JSONObject requestJson = m_data.getRequestObject();
                if (requestJson != null) {
                    makeRequest("http://localhost/", requestJson.toString());
                }*/
                //sendJson();
                sendGPS(Base_url, m_data.getRequestObject());
                //sendGPS("http://192.168.1.110/GpsTracker/index.php/gps_tracker/user", m_data.getRequestObject());
                Log.d("GPS", "sending" + i);
            }
            else if (limit > 1) {
                limit--;
                handler.sendMessageDelayed(new Message(), 3000);
            }
            else {
                handler.sendMessageDelayed(new Message(), 60000);
                if (limit == 1) {
                    m_geolocation.setTimeout(30000);
                    m_acceleration.StopAcceleration();
                    limit = 0;
                }
            }
        }
    };

    protected void sendJson() {
        Thread t = new Thread() {
            public void run() {
                JSONObject requestJson = m_data.getRequestObject();
                if (requestJson != null) {
                    makeRequest(Base_url, requestJson.toString());
                    //makeRequest("http://192.168.1.110/GpsTracker/index.php/gps_tracker/user", requestJson.toString());
                }
            }
        };

        t.start();
    }

    public static String makeRequest(String uri, String json) {
        HttpURLConnection urlConnection;
        String data = json;
        String result = null;
        try {
            //Connect
            urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);
            writer.close();
            outputStream.close();

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void sendGPS(String url, JSONObject params) {
        StringEntity entity = null;
        try {
            entity = new StringEntity(params.toString());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            client.post(this,url, entity, "application/json",  new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    // called before request is started
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                }

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                }

                @Override
                public void onRetry(int retryNo) {
                    // called when request is retried
                }
            });
        }
        catch (UnsupportedEncodingException ex) {

        }
    }

}
