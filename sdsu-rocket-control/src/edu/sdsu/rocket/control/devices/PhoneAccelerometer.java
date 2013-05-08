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
	private static final int Y_INDEX = 0;
	private static final int Z_INDEX = 0;
	
	private PhoneAccelerometerListener listener;
	
	private SensorManager sensorManager;
	private Sensor dataSource;
	private int rate;
	
	public float x;
	public float y;
	public float z;
	
	public PhoneAccelerometer(int rate) {
		this.rate = rate;
	}
	
	public void setListener(PhoneAccelerometerListener listener) {
		this.listener = listener;
	}

	public void setDataSource(SensorManager sensorManager, Sensor dataSource) {
		this.sensorManager = sensorManager;
		this.dataSource = dataSource;
	}
	
	public void start() {
		if (sensorManager != null && dataSource != null) {
			sensorManager.registerListener(this, dataSource, rate);
		}
	}
	
	public void stop() {
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (listener != null) {
			x = event.values[X_INDEX];
			y = event.values[Y_INDEX];
			z = event.values[Z_INDEX];
			
			listener.onPhoneAccelerometer(x, y, z);
		}
	}

}
