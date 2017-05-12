package com.termux.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.util.JsonWriter;
import android.util.Log;

import com.termux.api.util.ResultReturner;
import com.termux.api.util.ResultReturner.ResultJsonWriter;
import com.termux.api.util.TermuxApiLogger;

import java.io.IOException;

class BarometerAPI {
    static void onReceive(TermuxApiReceiver apiReceiver, final Context context, Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultJsonWriter() {
            @Override
            public void writeJson(final JsonWriter out) throws Exception {
                final SensorManager sm = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);

                Looper.prepare();
                sm.registerListener(new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        try {
                            sm.unregisterListener(this);
                            pressureToJson(event.values[0], out);
                        } catch (IOException e) {
                            TermuxApiLogger.error("Writing json", e);
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                }, sm.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_FASTEST);

                final Looper looper = Looper.myLooper();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Log.e("termux", "INTER", e);
                        }
                        looper.quit();
                    }
                }.start();
                Looper.loop();
            }
        });
    }

    private static void pressureToJson(float pressure, JsonWriter out) throws IOException
    {
        out.beginObject();
        out.name("pressure").value(pressure);
        out.endObject();
    }
}