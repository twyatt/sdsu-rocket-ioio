package edu.sdsu.rocket.control.devices;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PhoneAccelerometer implements SensorEventListener {
	
	public interface PhoneAccelerometerListener {
		public void onPhoneAccelerometer(float x, float y, float z);
	}

	private static final int X_INDEX = 0;
	private static final int Y_INDEX = 1;
	private static final int Z_INDEX = 2;
	
	private PhoneAccelerometerListener listener;
	
	private SensorManager sensorManager;
	private Sensor dataSource;
	private int rate;
	
	private volatile float x;
	private volatile float y;
	private volatile float z;
	
	public PhoneAccelerometer(int rate) {
		this.rate = rate;
	}
	
	public PhoneAccelerometer setListener(PhoneAccelerometerListener listener) {
		this.listener = listener;
		return this;
	}
	
	public PhoneAccelerometer setSensorManager(SensorManager sensorManager) {
		this.sensorManager = sensorManager;
		return this;
	}

	public PhoneAccelerometer setDataSource(Sensor dataSource) {
		this.dataSource = dataSource;
		return this;
	}
	
	public void start() {
		if (sensorManager == null) {
			throw new NullPointerException();
		}
		if (dataSource == null) {
			throw new NullPointerException();
		}
		
		sensorManager.registerListener(this, dataSource, rate);
	}
	
	public void stop() {
		sensorManager.unregisterListener(this);
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getZ() {
		return z;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		x = event.values[X_INDEX];
		y = event.values[Y_INDEX];
		z = event.values[Z_INDEX];
		
		if (listener != null) {
			listener.onPhoneAccelerometer(x, y, z);
		}
	}

}
