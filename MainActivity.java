package com.example.a00839270.sensorservdb;

        import android.content.Context;
        import android.content.Intent;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.AsyncTask;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.widget.TextView;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.PrintWriter;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.sql.Connection;
        import java.sql.DriverManager;
        import java.sql.SQLException;
        import java.sql.Statement;
        import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView textView;
    private TextView textXaccel;
    private TextView textYaccel;
    private TextView textZaccel;

    private TextView textXgyro;
    private TextView textYgyro;
    private TextView textZgyro;

    private SensorManager mSensorManager;
    private Sensor acceleration;
    private Sensor gyro;
    private SensorEventListener mSensorListener;
    public static final String URL = "http://10.2.2:8080/midp/hits";
    private float xG;
    private float yG;
    private float zG;
    private float xA;
    private float yA;
    private float zA;
    private boolean accelFlag;
    private boolean gyroFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize event values and sensor flag
        xG = 0;
        yG = 0;
        zG = 0;
        xA = 0;
        yA = 0;
        zA = 0;
        accelFlag = false;
        gyroFlag = false;

        textXaccel = (TextView) findViewById(R.id.TextViewXaccel);
        textYaccel = (TextView) findViewById(R.id.TextViewYaccel);
        textZaccel = (TextView) findViewById(R.id.TextViewZaccel);

        textXgyro = (TextView) findViewById(R.id.TextViewXgyro);
        textYgyro = (TextView) findViewById(R.id.TextViewYgyro);
        textZgyro = (TextView) findViewById(R.id.TextViewZgyro);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        acceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mSensorListener, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorListener, acceleration, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            getGyro(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void getGyro(SensorEvent event) {
        xG = event.values[0];
        yG = event.values[1];
        zG = event.values[2];

        textXgyro.setText((int) xG + " rad/s");
        textYgyro.setText((int) yG + " rad/s");
        textZgyro.setText((int) zG + " rad/s");

       /* if (Math.abs(xG)+Math.abs(yG)+Math.abs(zG) > 3){
            gyroFlag = true;
            HttpSensor(1);
        }*/
    }

    private void getAccelerometer(SensorEvent event) {
        xA = event.values[0];
        yA = event.values[1];
        zA = event.values[2];

        DecimalFormat dF = new DecimalFormat("#.##");
        xA = Float.parseFloat(dF.format(xA));
        yA = Float.parseFloat(dF.format(yA));
        zA = Float.parseFloat(dF.format(zA));

        textXaccel.setText(String.valueOf(xA) + " m/s2");
        textYaccel.setText(String.valueOf(yA) + " m/s2");
        textZaccel.setText(String.valueOf(zA) + " m/s2");

        /*if (Math.abs(xA)+Math.abs(yA)+Math.abs(zA) > 12){
            accelFlag = true;
            HttpSensor(1);
        }*/
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void HttpSensor(int flag) {
        if (accelFlag && gyroFlag){
            HttpTask task = new HttpTask();
            task.execute(new String[] {URL});
            accelFlag = false;
            gyroFlag = false;
        }
    }

    private class HttpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = null;
            BufferedReader br = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String str = "";
                while ((str = br.readLine()) != null) {
                    response = response + str;
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
        }
    }
}

