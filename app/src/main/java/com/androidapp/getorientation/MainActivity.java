package com.androidapp.getorientation;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextView txtAzimuth, txtPitch, txtRoll;
    SensorManager sensorManager;
    Sensor magSensor, accSensor;
    SensorEventListener listener;

    private float[] magValues, accValues;     // magSensor, accSensor 값들을 받음

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtAzimuth = findViewById(R.id.txtAzimuth);
        txtPitch = findViewById(R.id.txtPitch);
        txtRoll = findViewById(R.id.txtRoll);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);



        listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                switch (sensorEvent.sensor.getType()) { //센서에 타입 확인
                    case Sensor.TYPE_ACCELEROMETER:     //가속계일경우
                         accValues = sensorEvent.values.clone(); break;
                    case Sensor.TYPE_MAGNETIC_FIELD:    //자기센서
                        magValues = sensorEvent.values.clone(); break;
                }

                if (magValues != null && accValues != null) {

                    // 1) 회전(Rotation) 행렬과 경사(Inclination) 행렬 얻기
                    float[] R = new float[16]; //얻고자 하는 회전 행렬 (장비의 방향을 계산할 때 이용)
                    float[] I = new float[16]; //얻고자 하는 경사 행렬 (장비의 경사 각도를 계산할 때 이용)
                    SensorManager.getRotationMatrix(R, I, accValues, magValues);

                    // 2) 회전행렬로부터 방향 얻기
                    float[] values = new float[3];
                    SensorManager.getOrientation(R, values);
                    if((int) radian2Degree(values[0]) == 180) {
                        Toast.makeText(MainActivity.this, "180", Toast.LENGTH_SHORT);   //북쪽에서 동쪽으로 장비가 회전할 경우 0~180도
                    } else if((int) radian2Degree(values[0]) == -180) {
                        Toast.makeText(MainActivity.this, "-180", Toast.LENGTH_SHORT);      //북쪽에서 서쪽으로 장비가 회전할경우 0~-180도
                    }


                    txtAzimuth.setText("Azimuth: " + (int) radian2Degree(values[0]));   // values[0] Z축 회전량(azimuth)
                    txtPitch.setText("Pitch: " + (int) radian2Degree(values[1]));       // values[1] Y축 회전량(pitch)
                    txtRoll.setText("Roll: " + (int) radian2Degree(values[2]));         // values[2] X축 회전량(roll)

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {            }   //손될일 없다.
        };
        sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private float radian2Degree(float radian) {
        return radian * 180 / (float)Math.PI;
    }
}