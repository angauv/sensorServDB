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
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.TextView;
        import android.widget.Button;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.net.URLConnection;
        import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    TextView outputText;
    Button button;
    private TextView textXaccel;
    private TextView textYaccel;
    private TextView textZaccel;

    private TextView textXgyro;
    private TextView textYgyro;
    private TextView textZgyro;

    private SensorManager mSensorManager;
    private Sensor acceleration;
    private Sensor gyro;
    public static final String URL = "http://10.0.2.2:8082/hello";
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

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        outputText = (TextView) findViewById(R.id.outputTxt);
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

    public void onClick(View v) {
        GetXMLTask task = new GetXMLTask();
        task.execute(new String[] {"http://10.0.2.2:8082/hello"});
    }

    public final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                getGyro(event);
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                getAccelerometer(event);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        private void getGyro(SensorEvent event) {
            xG = event.values[0];
            yG = event.values[1];
            zG = event.values[2];

            textXgyro.setText((int) xG + " rad/s");
            textYgyro.setText((int) yG + " rad/s");
            textZgyro.setText((int) zG + " rad/s");

            if (Math.abs(xG) + Math.abs(yG) + Math.abs(zG) > 3) {
                gyroFlag = true;
                //HttpSensor(0);
            }
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

            //if (Math.abs(xA)+Math.abs(yA)+Math.abs(zA) > 9){
            //  accelFlag = true;
            //HttpSensor(1);
            //}
        }

        protected void onResume() {
            MainActivity.super.onResume();
            mSensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        protected void onPause() {
            MainActivity.super.onPause();
            mSensorManager.unregisterListener(this);
        }

        public void HttpSensor(int flag) {
            if (flag == 1) {
                GetXMLTask task = new GetXMLTask();
                task.execute(new String[] {"http://10.0.2.2:8082/hello"});
              //  accelFlag = false;
              //  gyroFlag = false;
            }
        }
    };

    public class GetXMLTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = null;
            for (String url : urls) {
                response = getOutputFromUrl(url);
            }
            return response;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return output.toString();
        }

        private InputStream getHttpConnection(String urlString) throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }

        @Override
        protected void onPostExecute(String out) {
            outputText.setText(out);
        }
    }
}

