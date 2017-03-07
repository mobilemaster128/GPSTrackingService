package com.mobile.gpsservice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;

/**
 * Created by PC on 8/2/2016.
 */
public class GeoLocation implements LocationListener {

    private Context m_context = null;
    private LocationManager lm = null;
    private Measure_Data m_data = null;
    int m_satellites = 0;
    double lat = 0, lon = 0, alt = 0;
    float speed = 0;
    boolean first = true;

    public GeoLocation(Context context, Measure_Data data) {
        m_data = data;
        m_context = context;
    }

    public void setTimeout(int timeout) {
        StopGeoLocation();
        StartGeoLocatiom(timeout);
    }

    public boolean StartGeoLocatiom(int timeout) {
        lm = (LocationManager) m_context.getSystemService(m_context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeout, 0, this);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeout, 0, this);

        lm.addGpsStatusListener(gpsStatusListener);
        //lm.addNmeaListener(nmea_listener);

        return true;
    }

    public void StopGeoLocation() {
        if (ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.removeUpdates(this);
        lm.removeGpsStatusListener(gpsStatusListener);
        //lm.removeNmeaListener(nmea_listener);
    }

    public int getSatellites() {
        GpsStatus status = lm.getGpsStatus(null);

        Iterable sats = status.getSatellites();
        Iterator satI = sats.iterator();
        int count = 0;

        while (satI.hasNext()) {
            GpsSatellite gpssatellite = (GpsSatellite) satI.next();
            if (gpssatellite.usedInFix()) {
                count++;
            }
        }

        m_satellites = count;

        return m_satellites;
    }

    private GpsStatus.NmeaListener nmea_listener = new GpsStatus.NmeaListener() {
        public void onNmeaReceived(long timestamp, String nmea) {
            // TODO Auto-generated method stub
            // $GPGGA,054208.000,3725.973,N,12709.561,E,1,05,1.90,141.50,M,160.50,M,,*58
            // Log.d(LOG_TAG,nmea);
            String str_temp[] = nmea.split(",");
            if (str_temp[0].equals("$GPGGA")) {
                Log.d("GPS", str_temp[7]);  // m_satellites
            }
        }
    };

    private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            GpsStatus status = lm.getGpsStatus(null);

            Iterable sats = status.getSatellites();
            Iterator satI = sats.iterator();
            int count = 0;

            while (satI.hasNext()) {
                GpsSatellite gpssatellite = (GpsSatellite) satI.next();
                if (gpssatellite.usedInFix()) {
                    count++;
                }
            }

            m_satellites = count;

        }
    };

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (first) {
                speed = location.getSpeed();
            }
            else {
                speed += location.getSpeed();
                speed /= 2;
            }
            m_data.setSpeed((float) (speed * 3.6));

            if (first) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                alt = location.getAltitude();
            }
            else {
                lat += location.getLatitude();
                lon += location.getLongitude();
                alt += location.getAltitude();
                lat /= 2;
                lon /= 2;
                alt /= 2;
            }
            first = false;
            m_data.setLocation(lat, lon, alt);
            //Toast.makeText(m_context, "\nLat : " + lat + " Long : " + lon + " speed : " + speed + "m/s num : " + m_satellites+ location.getProvider(), Toast.LENGTH_LONG).show();
            //Toast.makeText(m_context, "\nLat : " + lat + "Long : " + lon + " Alt : " + alt + "speed : " + speed + "num : " + m_satellites+ location.getProvider(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
}
