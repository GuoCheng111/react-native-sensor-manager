package com.sensormanager;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.content.Context;
import android.hardware.SensorManager;

import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class SensorManagerModule extends ReactContextBaseJavaModule {
    private static final String		REACT_CLASS = "SensorManager";
    private AccelerometerRecord		mAccelerometerRecord = null;
	private GyroscopeRecord 		mGyroscopeRecord = null;
	private MagnetometerRecord		mMagnetometerRecord = null;
	private StepCounterRecord		mStepCounterRecord = null;
	private ThermometerRecord		mThermometerRecord = null;
	private MotionValueRecord		mMotionValueRecord = null;
	private OrientationRecord		mOrientationRecord = null;
	private ProximityRecord			mProximityRecord = null;
  private LightSensorRecord   mLightSensorRecord = null;

	private ReactApplicationContext	mReactContext;

    private float[] inR = new float[9];
    private float[] inclineMatrix = new float[9];
    private float[] accelValues = new float[3], compassValues = new float[3];
    private float[] prefValues = new float[3];
    private double mInclination;
    private int axis_x;
    private int axis_y;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public SensorManagerModule(ReactApplicationContext reactContext) {
		super(reactContext);
		mReactContext = reactContext;
    }

    @ReactMethod
    public void getAzimuth(float  magnetometerX,float magnetometerY,float magnetometerZ,
                           float  accelerometerX,float accelerometerY,float accelerometerZ, Callback successCallback, Callback errorCallback) {
        Log.d("SensorManagerModule", " magnetometerX : " + magnetometerX + " magnetometerY : " + magnetometerY + " magnetometerZ : " + magnetometerZ
                + " accelerometerX : " + accelerometerX + " accelerometerY : " + accelerometerY + " accelerometerZ : " + accelerometerZ );
        accelValues[0] = accelerometerX;
        accelValues[1] = accelerometerY;
        accelValues[2] = accelerometerZ;
        compassValues[0] = magnetometerX;
        compassValues[1] = magnetometerY;
        compassValues[2] = magnetometerZ;

        WindowManager wm = (WindowManager) mReactContext.getSystemService(Context.WINDOW_SERVICE);
        Display dis = wm.getDefaultDisplay();
        int rotation = dis.getRotation();


        axis_x = SensorManager.AXIS_X;
        axis_y = SensorManager.AXIS_Y;
        switch (rotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                axis_x = SensorManager.AXIS_Y;
                axis_y = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                axis_x = SensorManager.AXIS_X;
                axis_y = SensorManager.AXIS_MINUS_Y;
                break;
            case Surface.ROTATION_270:
                axis_x = SensorManager.AXIS_MINUS_Y;
                axis_y = SensorManager.AXIS_X;
                break;
            default:
                break;
        }

        //【2】根据加速传感器的数值accelValues[3]和磁力感应器的数值compassValues[3]，进行矩阵计算，获得方位
        //【2.1】计算rotation matrix R(inR)和inclination matrix I(inclineMatrix) 
        if(SensorManager.getRotationMatrix(inR, inclineMatrix,accelValues, compassValues)){
            float[] outR = new float[9];
            /*
            根据当前上下文的屏幕方向调整与自然方向的相对关系。
            当设备的自然方向是竖直方向（比如，理论上说所有手机的自然方向都是都是是竖直方向，而有些平板的自然方向是水平方向），而应用是横屏时，
             需要将相对设备自然方向的方位角转换为相对水平方向的方位角。
             */
            SensorManager.remapCoordinateSystem(inR, axis_x, axis_y, outR);
            /* 【2.2】根据rotation matrix计算设备的方位。，范围数组：
            values[0]: azimuth, rotation around the Z axis.
            values[1]: pitch, rotation around the X axis.
            values[2]: roll, rotation around the Y axis.*/
            SensorManager.getOrientation(outR, prefValues);
            //【2.2】根据inclination matrix计算磁仰角，地球表面任一点的地磁场总强度的矢量方向与水平面的夹角。
            mInclination = SensorManager.getInclination(inclineMatrix);
            float mAzimuth = (float)Math.toDegrees(prefValues[0]);
            successCallback.invoke(mAzimuth);
        }else{
            Log.d("SensorManagerModule", "无法获得矩阵(SensorManager.getRotationMatrix)");
            errorCallback.invoke("无法获得矩阵");
        }
    }

    @ReactMethod
    public int startAccelerometer(int delay) {
		if (mAccelerometerRecord == null)
			mAccelerometerRecord = new AccelerometerRecord(mReactContext);
		return (mAccelerometerRecord.start(delay));
    }

    @ReactMethod
    public void stopAccelerometer() {
		if (mAccelerometerRecord != null)
			mAccelerometerRecord.stop();
    }

    @ReactMethod
    public int startGyroscope(int delay) {
		if (mGyroscopeRecord == null)
			mGyroscopeRecord = new GyroscopeRecord(mReactContext);
		return (mGyroscopeRecord.start(delay));
    }

    @ReactMethod
    public void stopGyroscope() {
		if (mGyroscopeRecord != null)
			mGyroscopeRecord.stop();
    }

    @ReactMethod
    public int startMagnetometer(int delay) {
		if (mMagnetometerRecord == null)
			mMagnetometerRecord = new MagnetometerRecord(mReactContext);
		return (mMagnetometerRecord.start(delay));
    }

    @ReactMethod
    public void stopMagnetometer() {
		if (mMagnetometerRecord != null)
			mMagnetometerRecord.stop();
    }

    @ReactMethod
    public int startStepCounter(int delay) {
		if (mStepCounterRecord == null)
			mStepCounterRecord = new StepCounterRecord(mReactContext);
		return (mStepCounterRecord.start(delay));
    }

    @ReactMethod
    public void stopStepCounter() {
		if (mStepCounterRecord != null)
			mStepCounterRecord.stop();
    }

    @ReactMethod
    public int startThermometer(int delay) {
		if (mThermometerRecord == null)
			mThermometerRecord = new ThermometerRecord(mReactContext);
		return (mThermometerRecord.start(delay));
    }

    @ReactMethod
    public void stopThermometer() {
		if (mThermometerRecord != null)
			mThermometerRecord.stop();
    }

    @ReactMethod
    public int startMotionValue(int delay) {
		if (mMotionValueRecord == null)
			mMotionValueRecord = new MotionValueRecord(mReactContext);
		return (mMotionValueRecord.start(delay));
    }

    @ReactMethod
    public void stopMotionValue() {
		if (mMotionValueRecord != null)
			mMotionValueRecord.stop();
    }

    @ReactMethod
    public int startOrientation(int delay) {
		if (mOrientationRecord == null)
			mOrientationRecord = new OrientationRecord(mReactContext);
		return (mOrientationRecord.start(delay));
    }

    @ReactMethod
    public void stopOrientation() {
		if (mOrientationRecord != null)
			mOrientationRecord.stop();
    }

    @ReactMethod
    public int startProximity(int delay) {
		if (mProximityRecord == null)
			mProximityRecord = new ProximityRecord(mReactContext);
		return (mProximityRecord.start(delay));
    }

    @ReactMethod
    public void stopProximity() {
		if (mProximityRecord != null)
			mProximityRecord.stop();
    }

    @ReactMethod
    public int startLightSensor(int delay) {
      if(mLightSensorRecord == null)
        mLightSensorRecord = new LightSensorRecord(mReactContext);
      return (mLightSensorRecord.start(delay));
    }

    @ReactMethod
    public void stopLightSensor() {
      if(mLightSensorRecord != null)
        mLightSensorRecord.stop();
    }

	/*
    @Override
    public ReactBarcodeScannerView createViewInstance(ThemedReactContext context) {
    }

    @Override
    public void onDropViewInstance(ReactBarcodeScannerView view) {
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
    }
    */
}
