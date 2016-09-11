package com.example.rajatshah.csc750p1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView mLightTextLabel;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLocationText;
    private TextView mRexDistanceText;
    private TextView mAccelerationText;
    private TextView mPressureText;

    private SensorManager mSensorManager;
    private SensorEventListener mEventListenerLight;
    private SensorEventListener mEventListenerAccelerometer;
    private SensorEventListener mEventListenerPressure;


    private float lastLightValue;
    private float lastAccelerationY;
    private float[] locationResults;
    private float pressureValue;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private enum SensorType {
        LIGHT, ACCELEROMETER, LOCATION, PRESSURE
    };
    // Create a new thread to update the UI
    private void updateUI(final SensorType sensorType) {
        runOnUiThread(new Runnable() {
            @Override

            public void run() {
                switch (sensorType) {
                    case LIGHT:
                        mLightTextLabel.setText(String.valueOf(lastLightValue) + " lux");
                        break;
                    case ACCELEROMETER:
                        mAccelerationText.setText(String.valueOf(lastAccelerationY) + " m/s^2");
                        break;
                    case LOCATION:
                        mLocationText.setText("(" + String.valueOf(mLastLocation.getLatitude()) + "," + mLastLocation.getLongitude() + ")");
                        //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                        //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                        mRexDistanceText.setText(String.valueOf(locationResults[0]) + " m"); //Set the distance from Current location
                        break;
                    case PRESSURE:
                        mPressureText.setText(String.valueOf(pressureValue) + " bar");
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Start the background service
        startService(new Intent(getApplicationContext(), BackgroundService.class));
        //Light Sensor
        mLightTextLabel = (TextView) findViewById(R.id.lightText);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mEventListenerLight = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float[] values = sensorEvent.values;
                lastLightValue = values[0];
                updateUI(SensorType.LIGHT);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        //Accelerometer
        mAccelerationText = (TextView) findViewById(R.id.accelerationText);
        mEventListenerAccelerometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float[] values = sensorEvent.values;
                //update UI only when change is significant
                if(lastAccelerationY == 0.0 || Math.abs(lastAccelerationY-values[1]) > 0.5) {
                    lastAccelerationY = values[1];
                    updateUI(SensorType.ACCELEROMETER);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        //Pressure
        mPressureText = (TextView) findViewById(R.id.pressureText);
        mEventListenerPressure = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float[] values = sensorEvent.values;
                if(pressureValue == 0.0 || Math.abs(pressureValue-values[0]/1000) > 0.0001) {
                    pressureValue = values[0]/1000; //convert from millibar to bar
                    updateUI(SensorType.PRESSURE);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        //Location Listener
        // Create an instance of GoogleAPIClient
        //mLatitudeText = (TextView) findViewById(R.id.latitudeText);
        //mLongitudeText = (TextView)  findViewById(R.id.longitudeText);
        mLocationText = (TextView) findViewById(R.id.locationText);
        mRexDistanceText = (TextView) findViewById(R.id.distanceText);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        showNotification();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect(); //location
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mEventListenerLight, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mEventListenerAccelerometer, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mEventListenerPressure, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect(); //location
        super.onStop();
        mSensorManager.unregisterListener(mEventListenerLight);
        mSensorManager.unregisterListener(mEventListenerAccelerometer);
        mSensorManager.unregisterListener(mEventListenerPressure);
    }

    public void showNotification() {
        String msg = getResources().getString(R.string.notification_msg);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Emergency Details");
        builder.setContentText(msg);

        Intent intent = new Intent(this, NotificationClass.class);
        intent.putExtra("More Information:", msg);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NotificationClass.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(msg));     //to show bigger expandable notification

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    //Location service
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                double rexHospitalLatitude = 35.8178743;    //Hardcoded coordinates of Rex Hospital
                double rexHospitalLongitude = -78.702539;
                locationResults =  new float[1];
                Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                        rexHospitalLatitude, rexHospitalLongitude, locationResults);
                updateUI(SensorType.LOCATION);
            }
        } catch (SecurityException ex) {

        }
    }
    //Location service
    @Override
    public void onConnectionSuspended(int i) {

    }
    //Location service
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
