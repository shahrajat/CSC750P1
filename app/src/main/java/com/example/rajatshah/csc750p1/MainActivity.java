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
    private TextView mRexDistanceText;
    private TextView mAccelerationText;

    private SensorManager mSensorManager;
    private SensorEventListener mEventListenerLight;
    private SensorEventListener mEventListenerAccelerometer;

    private float lastLightValue;
    private float lastAccelerationY;
    private float[] locationResults;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private enum SensorType {
        LIGHT, ACCELEROMETER, LOCATION
    };
    // Create a new thread to update the UI
    private void updateUI(final SensorType sensorType) {
        runOnUiThread(new Runnable() {
            @Override

            public void run() {
                switch (sensorType) {
                    case LIGHT:
                        mLightTextLabel.setText(String.valueOf(lastLightValue));
                        break;
                    case ACCELEROMETER:
                        mAccelerationText.setText(String.valueOf(lastAccelerationY));
                        break;
                    case LOCATION:
                        mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                        mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                        mRexDistanceText.setText(String.valueOf(locationResults[0]) + "m"); //Set the distance from Current location
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //Location Listener
        // Create an instance of GoogleAPIClient
        mLatitudeText = (TextView) findViewById(R.id.latitudeText);
        mLongitudeText = (TextView)  findViewById(R.id.longitudeText);
        mRexDistanceText = (TextView) findViewById(R.id.distanceText);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
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
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect(); //location
        super.onStop();
        mSensorManager.unregisterListener(mEventListenerLight);
        mSensorManager.unregisterListener(mEventListenerAccelerometer);
    }

    public void showNotification(View view) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Emergency Details");
        builder.setContentText("Blood Group: O- \n No Allergies \n Emergency Contact: 911");

        Intent intent = new Intent(this, NotificationClass.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NotificationClass.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

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
