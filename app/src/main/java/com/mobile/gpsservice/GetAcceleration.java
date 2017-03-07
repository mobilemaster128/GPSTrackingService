package com.mobile.gpsservice;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import java.text.DecimalFormat;

/**
 * Created by PC on 8/2/2016.
 */
public class GetAcceleration implements SensorEventListener {

    Context m_context = null;
    SensorManager m_sensor_manager = null;
    Sensor m_accelerometer = null;
    float[] m_accel_data = new float[3];
    DecimalFormat m_format;
    Measure_Data m_data = null;

    public GetAcceleration(Context context, Measure_Data data) {
        m_context = context;
        m_data = data;

        m_format = new DecimalFormat();
        m_format.applyLocalizedPattern("0.##");
    }

    public boolean StartAcceleration() {
        m_sensor_manager = (SensorManager)m_context.getSystemService(Context.SENSOR_SERVICE);
        m_accelerometer = m_sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        m_sensor_manager.registerListener(this, m_accelerometer, SensorManager.SENSOR_DELAY_UI);

        return true;
    }

    public void StopAcceleration() {
        m_sensor_manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // alpha = t / (t + Dt)
            final float alpha = (float)0.8;

            float[] m_gravity_data = new float[3];

            m_gravity_data[0] = alpha * m_gravity_data[0] + (1 - alpha) * sensorEvent.values[0];
            m_gravity_data[1] = alpha * m_gravity_data[1] + (1 - alpha) * sensorEvent.values[1];
            m_gravity_data[2] = alpha * m_gravity_data[2] + (1 - alpha) * sensorEvent.values[2];

            m_accel_data[0] += sensorEvent.values[0] - m_gravity_data[0];
            m_accel_data[1] += sensorEvent.values[1] - m_gravity_data[1];
            m_accel_data[2] += sensorEvent.values[2] - m_gravity_data[2];

            m_accel_data[0] /= 2; m_accel_data[1] /= 2; m_accel_data[2] /= 2;

            m_data.setAccel(m_accel_data);

            //String str;

            //str = "x : " + m_format.format(m_accel_data[0]) + ", y : " + m_format.format(m_accel_data[1]) +
            //        ", z : " + m_format.format(m_accel_data[2]);
            //Toast.makeText(m_context, str, Toast.LENGTH_LONG).show();
            //Toast.makeText(m_context, "bat : " + m_data.getBat(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
