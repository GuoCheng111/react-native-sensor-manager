package com.sensormanager;

import android.content.Context;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.*;
import java.util.Date;
import java.util.Timer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;

public class OrientationRecord implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Sensor mGyroscope;
    private Sensor mGravitySensor;
    private long lastUpdate = 0;
    private int i = 0, n = 0;
    private int delay;
    private int isRegistered = 0;

    private ReactContext mReactContext;
    private Arguments mArguments;


    public OrientationRecord(ReactApplicationContext reactContext) {
        mSensorManager = (SensorManager)reactContext.getSystemService(reactContext.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mReactContext = reactContext;
    }

    public int start(int delay) {
        this.delay = delay;
        if (mAccelerometer != null && isRegistered == 0) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_UI);
            isRegistered = 1;
            return (1);
        }
        return (0);
    }

    public void stop() {
        if (isRegistered == 1) {
            mSensorManager.unregisterListener(this);
        isRegistered = 0;
      }
    }

    private void sendEvent(String eventName, @Nullable WritableMap params)
    {
        try {
            mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        } catch (RuntimeException e) {
            Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke JS before CatalystInstance has been set!");
        }
    }

    float[] mGravity = new float[3];
    float[] mGeomagnetic= new float[3];

    // raw inputs from Android sensors
    float m_Norm_Gravity;           // length of raw gravity vector received in onSensorChanged(...).  NB: should be about 10
    float[] m_NormGravityVector;    // Normalised gravity vector, (i.e. length of this vector is 1), which points straight up into space
    float m_Norm_MagField;          // length of raw magnetic field vector received in onSensorChanged(...).
    float[] m_NormMagFieldValues;   // Normalised magnetic field vector, (i.e. length of this vector is 1)

    // accuracy specifications. SENSOR_UNAVAILABLE if unknown, otherwise SensorManager.SENSOR_STATUS_UNRELIABLE, SENSOR_STATUS_ACCURACY_LOW, SENSOR_STATUS_ACCURACY_MEDIUM or SENSOR_STATUS_ACCURACY_HIGH
    int m_GravityAccuracy;          // accuracy of gravity sensor
    int m_MagneticFieldAccuracy;    // accuracy of magnetic field sensor

    // values calculated once gravity and magnetic field vectors are available
    float[] m_NormEastVector = new float[3];       // normalised cross product of raw gravity vector with magnetic field values, points east
    float[] m_NormNorthVector = new float[3];      // Normalised vector pointing to magnetic north
    boolean m_OrientationOK;        // set true if m_azimuth_radians and m_pitch_radians have successfully been calculated following a call to onSensorChanged(...)
    float m_azimuth_radians;        // angle of the device from magnetic north
    float m_pitch_radians;          // tilt angle of the device from the horizontal.  m_pitch_radians = 0 if the device if flat, m_pitch_radians = Math.PI/2 means the device is upright.
    float m_pitch_axis_radians;     // angle which defines the axis for the rotation m_pitch_radians
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//      Sensor mySensor = sensorEvent.sensor;
//      WritableMap map = mArguments.createMap();
//
//      if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
//          //mGravity = sensorEvent.values;
//          for(int i = 0; i< 3; i++){
//              mGravity[i] = sensorEvent.values[i];
//          }
//      }
//      if (mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
//          //mGeomagnetic = sensorEvent.values;
//          for(int i = 0; i< 3; i++){
//              mGeomagnetic[i] = sensorEvent.values[i];
//          }
//      }
//      if(mySensor.getType() == Sensor.TYPE_ORIENTATION){
//          Log.d("OrientationRecord", "Azimuth:" + sensorEvent.values[0] + "Pitch:" + sensorEvent.values[1] +  "Roll:" + sensorEvent.values[2] );
//      }
//
//        WindowManager wm = (WindowManager) mReactContext.getSystemService(Context.WINDOW_SERVICE);
//        Display dis = wm.getDefaultDisplay();
//        int rotation = dis.getRotation();
//
//        int axis_x = SensorManager.AXIS_X;
//        int axis_y = SensorManager.AXIS_Y;
//        Log.d("OrientationRecord", "rotation : " + rotation);
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                break;
//            case Surface.ROTATION_90:
//                axis_x = SensorManager.AXIS_Y;
//                axis_y = SensorManager.AXIS_MINUS_X;
//                break;
//            case Surface.ROTATION_180:
//                axis_x = SensorManager.AXIS_X;
//                axis_y = SensorManager.AXIS_MINUS_Y;
//                break;
//            case Surface.ROTATION_270:
//                axis_x = SensorManager.AXIS_MINUS_Y;
//                axis_y = SensorManager.AXIS_X;
//                break;
//            default:
//                break;
//        }
//
//      if (mGravity != null && mGeomagnetic != null) {
//        float R[] = new float[9];
//        float I[] = new float[9];
//        boolean success = mSensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
//        if (success) {
//          long curTime = System.currentTimeMillis();
//          float orientation[] = new float[3];
//          mSensorManager.getOrientation(R, orientation);
//            Log.d("OrientationRecord", "orientation[0]:" + orientation[0] + " Math.toDegrees(orientation[0]) :" + Math.toDegrees(orientation[0]));
//          float heading = (float)((Math.toDegrees(orientation[0])) % 360.0f);
//          float pitch = (float)((Math.toDegrees(orientation[1])) % 360.0f);
//          float roll = (float)((Math.toDegrees(orientation[2])) % 360.0f);
//
//          Log.d("OrientationRecord", "heading:" + heading + "axis_x:" + axis_x +  "axis_y:" + axis_y );
//
////          if (heading < 0) {
////            heading = 360 - (0 - heading);
////          }
////
////          if (pitch < 0) {
////            pitch = 360 - (0 - pitch);
////          }
////
////          if (roll < 0) {
////            roll = 360 - (0 - roll);
////          }
//
//            if ((curTime - lastUpdate) > delay) {
//                map.putDouble("azimuth", heading);
//                map.putDouble("pitch", pitch);
//                map.putDouble("roll", roll);
//                sendEvent("Orientation", map);
//                lastUpdate = curTime;
//            }
//        }
//      }

        int SensorType = sensorEvent.sensor.getType();
        switch(SensorType) {
            case Sensor.TYPE_GRAVITY:
                if (m_NormGravityVector == null) m_NormGravityVector = new float[3];
                System.arraycopy(sensorEvent.values, 0, m_NormGravityVector, 0, m_NormGravityVector.length);
                m_Norm_Gravity = (float)Math.sqrt(m_NormGravityVector[0]*m_NormGravityVector[0] + m_NormGravityVector[1]*m_NormGravityVector[1] + m_NormGravityVector[2]*m_NormGravityVector[2]);
                for(int i=0; i < m_NormGravityVector.length; i++) m_NormGravityVector[i] /= m_Norm_Gravity;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (m_NormMagFieldValues == null) m_NormMagFieldValues = new float[3];
                System.arraycopy(sensorEvent.values, 0, m_NormMagFieldValues, 0, m_NormMagFieldValues.length);
                m_Norm_MagField = (float)Math.sqrt(m_NormMagFieldValues[0]*m_NormMagFieldValues[0] + m_NormMagFieldValues[1]*m_NormMagFieldValues[1] + m_NormMagFieldValues[2]*m_NormMagFieldValues[2]);
                for(int i=0; i < m_NormMagFieldValues.length; i++) m_NormMagFieldValues[i] /= m_Norm_MagField;
                break;
        }

        if (m_NormGravityVector != null && m_NormMagFieldValues != null) {
            // first calculate the horizontal vector that points due east
            float East_x = m_NormMagFieldValues[1]*m_NormGravityVector[2] - m_NormMagFieldValues[2]*m_NormGravityVector[1];
            float East_y = m_NormMagFieldValues[2]*m_NormGravityVector[0] - m_NormMagFieldValues[0]*m_NormGravityVector[2];
            float East_z = m_NormMagFieldValues[0]*m_NormGravityVector[1] - m_NormMagFieldValues[1]*m_NormGravityVector[0];
            float norm_East = (float)Math.sqrt(East_x * East_x + East_y * East_y + East_z * East_z);
            if (m_Norm_Gravity * m_Norm_MagField * norm_East < 0.1f) {  // Typical values are  > 100.
                m_OrientationOK = false; // device is close to free fall (or in space?), or close to magnetic north pole.
            } else {
                m_NormEastVector[0] = East_x / norm_East; m_NormEastVector[1] = East_y / norm_East; m_NormEastVector[2] = East_z / norm_East;

                // next calculate the horizontal vector that points due north
                float M_dot_G = (m_NormGravityVector[0] *m_NormMagFieldValues[0] + m_NormGravityVector[1]*m_NormMagFieldValues[1] + m_NormGravityVector[2]*m_NormMagFieldValues[2]);
                float North_x = m_NormMagFieldValues[0] - m_NormGravityVector[0] * M_dot_G;
                float North_y = m_NormMagFieldValues[1] - m_NormGravityVector[1] * M_dot_G;
                float North_z = m_NormMagFieldValues[2] - m_NormGravityVector[2] * M_dot_G;
                float norm_North = (float)Math.sqrt(North_x * North_x + North_y * North_y + North_z * North_z);
                m_NormNorthVector[0] = North_x / norm_North; m_NormNorthVector[1] = North_y / norm_North; m_NormNorthVector[2] = North_z / norm_North;

                // take account of screen rotation away from its natural rotation
                 WindowManager wm = (WindowManager) mReactContext.getSystemService(Context.WINDOW_SERVICE);
                Display dis = wm.getDefaultDisplay();
                int rotation = dis.getRotation();
                float screen_adjustment = 0;
                switch(rotation) {
                    case Surface.ROTATION_0:   screen_adjustment =          0;         break;
                    case Surface.ROTATION_90:  screen_adjustment =   (float)Math.PI/2; break;
                    case Surface.ROTATION_180: screen_adjustment =   (float)Math.PI;   break;
                    case Surface.ROTATION_270: screen_adjustment = 3*(float)Math.PI/2; break;
                }
                // NB: the rotation matrix has now effectively been calculated. It consists of the three vectors m_NormEastVector[], m_NormNorthVector[] and m_NormGravityVector[]

                // calculate all the required angles from the rotation matrix
                // NB: see https://math.stackexchange.com/questions/381649/whats-the-best-3d-angular-co-ordinate-system-for-working-with-smartfone-apps
                float sin = m_NormEastVector[1] -  m_NormNorthVector[0], cos = m_NormEastVector[0] +  m_NormNorthVector[1];
                m_azimuth_radians = (float) (sin != 0 && cos != 0 ? Math.atan2(sin, cos) : 0);
                m_pitch_radians = (float) Math.acos(m_NormGravityVector[2]);
                sin = -m_NormEastVector[1] -  m_NormNorthVector[0]; cos = m_NormEastVector[0] -  m_NormNorthVector[1];
                float aximuth_plus_two_pitch_axis_radians = (float)(sin != 0 && cos != 0 ? Math.atan2(sin, cos) : 0);
                m_pitch_axis_radians = (float)(aximuth_plus_two_pitch_axis_radians - m_azimuth_radians) / 2;
                m_azimuth_radians += screen_adjustment;
                m_pitch_axis_radians += screen_adjustment;
                m_OrientationOK = true;
                //Log.d("OrientationRecord","m_azimuth_radians : "+ m_azimuth_radians + " Math.toDegrees(m_azimuth_radians)" + Math.toDegrees(m_azimuth_radians));
            }
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > delay) {
                WritableMap map = mArguments.createMap();
                map.putDouble("azimuth", Math.toDegrees(m_azimuth_radians));
                map.putDouble("pitch", 0);
                map.putDouble("roll", 0);
                sendEvent("Orientation", map);
                lastUpdate = curTime;
            }
        }
        else{
            Log.d("OrientationRecord","is null");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("OrientationRecord","sensor.getType() : " + sensor.getType() + " accuracy : " + accuracy);
    }
}
