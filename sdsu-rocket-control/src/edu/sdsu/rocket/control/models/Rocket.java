package edu.sdsu.rocket.control.models;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import edu.sdsu.rocket.control.DeviceManager;
import edu.sdsu.rocket.control.devices.BreakWire;
import edu.sdsu.rocket.control.devices.DMO063;
import edu.sdsu.rocket.control.devices.DeviceRunnable;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.devices.PS050;
import edu.sdsu.rocket.control.devices.PhoneAccelerometer;

public class Rocket {

	public enum SensorPriority {
		SENSOR_PRIORITY_LOW,
		SENSOR_PRIORITY_MEDIUM,
		SENSOR_PRIORITY_HIGH,
	}
	
	public DMO063 ignitor;
	public DMO063 fuelValve;
	public BreakWire breakWire;
	
	public P51500AA1365V tankPressureLOX;
	public P51500AA1365V tankPressureEthanol;
	public P51500AA1365V tankPressureEngine;
	
	public PS050 servoLOX;
	public PS050 servoEthanol;
	
	public MS5611 barometer;
	
	public PhoneAccelerometer accelerometer;
	
	public DeviceRunnable tankPressureLOXRunnable;
	public DeviceRunnable tankPressureEthanolRunnable;
	public DeviceRunnable tankPressureEngineRunnable;
	public DeviceRunnable barometerRunnable;
	
	public Rocket() {
		/*
		 * IOIO Pin Overview:
		 * 
		 * Note: The board that we stayed up all night re-purposing to be the
		 * rocket control board had the edge where we placed the LOX Press
		 * Screw-down all connected (shorted the Screw-down Connector pins) so
		 * ended up soldering the LOX pressure transducer directly to the
		 * dangling wires (as noted below as well).
		 * 
		 * 1 = MS5611 SDA
		 * 2 = MS5611 SCL
		 * 9 = Break Wire -
		 * 13 = Servo PWM LOX Orange Wire (Soldered Directly to Wire)
		 * 14 = Servo PWM Ethanol Orange Wire (Eth Servo Screw-down Connector Port #3)
		 * 19 = DMO063 #1 Control +
		 * 20 = DMO063 #2 Control +
		 * 41 = P51500AA1365V #1 White Wire (LOX Press. Screw-down Connector Port #3)
		 * 42 = P51500AA1365V #2 White Wire (Eth Press. Screw-down Connector Port #3)
		 * 43 = P51500AA1365V #3 White Wire (Eng Press. Screw-down Connector Port #3)
		 * 
		 * 3.3V = MS5611 VCC
		 *        Break Wire +
		 * 3.3V GND = MS5611 GND
		 *            DMO063 #1 Control -
		 *            DMO063 #2 Control -
		 * 
		 * 5V = P51500AA1365V #1 Red Wire (LOX Press. Screw-down Connector Port #1)
		 *      P51500AA1365V #2 Red Wire (Eth Press. Screw-down Connector Port #1)
		 *      P51500AA1365V #3 Red Wire (Eng Press. Screw-down Connector Port #1)
		 *      Servo Signal VCC LOX Red Wire (Soldered Directly to Wire)
		 *      Servo Signal VCC Ethanol Red Wire (Eth Screw-down Connector Port #1)
		 *      10k Resistor #1
		 *      10k Resistor #2
		 * 5V GND = P51500AA1365V #1 Black Wire (LOX Press. Screw-down Connector Port #2)
		 *          P51500AA1365V #2 Black Wire (Eth Press. Screw-down Connector Port #2)
		 *          P51500AA1365V #3 Black Wire (Eng Press. Screw-down Connector Port #2)
		 *          Servo Signal GND LOX Brown Wire (Soldered Directly to Wire)
		 *          Servo Signal GND Ethanol Brown Wire (Eth Servo Screw-down Connector Port #2)
		 * 
		 * 10k Resistor #1 = Soldered Directly to LOX Servo Orange Wire
		 * 10k Resistor #2 = Eth Servo Screw-down Connector Port #3 (Eth Servo Orange Wire)
		 * 
		 * DMO063 #1 DC Load + = Wire Left Dangling (For Ignitor +)
		 * DMO063 #1 DC Load - = Wire Left Dangling (For Ignitor -)
		 * 
		 * DMO063 #2 DC Load + = Wire Left Dangling (Fuel Box Red Wire)
		 * DMO063 #2 DC Load - = Wire Left Dangling (Fuel Box White Wire)
		 */
		
		// DMO063 #1
		ignitor = new DMO063(19 /* pin */, 3.0f /* duration (seconds) */);
		
		// DMO063 #2
		fuelValve = new DMO063(20 /* pin */, 10.0f /* duration (seconds) */);
		
		breakWire = new BreakWire(9 /* pin */);
		
		// max voltage for analog input = 3.3V
		// calibrated May 12, 2013
		tankPressureLOX     = new P51500AA1365V(41 /* pin */, 179.0827f /* slope */, -145.268f /* bias */);
		tankPressureEthanol = new P51500AA1365V(42 /* pin */, 181.8296f /* slope */, -144.22f /* bias */);
		tankPressureEngine  = new P51500AA1365V(43 /* pin */, 179.7781f /* slope */, -140.324f /* bias */);
		
		servoLOX = new PS050(13 /* pin */, 100 /* frequency */);
		servoEthanol = new PS050(14 /* pin */, 100 /* frequency */);
		
		// twiNum 1 = pin 1 (SDA) and 2 (SCL)
		// VCC = 3.3V
		barometer = new MS5611(1 /* twiNum */, MS5611.ADD_CSB_LOW /* address */, 30 /* sample rate */);
		
		/*
		 * Devices internal to the Android phone (not connected via the IOIO).
		 */
		
		accelerometer = new PhoneAccelerometer(SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	public void setupDevices(DeviceManager deviceManager, SensorManager sensorManager) {
		Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accelerometer.setDataSource(sensorManager, accelerometerSensor);
		
		deviceManager.add(ignitor);
		deviceManager.add(fuelValve);
		deviceManager.add(breakWire);
		
		tankPressureLOXRunnable = new DeviceRunnable(tankPressureLOX);
		deviceManager.add(tankPressureLOXRunnable);
		
		tankPressureEthanolRunnable = new DeviceRunnable(tankPressureEthanol);
		deviceManager.add(tankPressureEthanolRunnable);
		
		tankPressureEngineRunnable = new DeviceRunnable(tankPressureEngine);
		deviceManager.add(tankPressureEngineRunnable);
		
		deviceManager.add(servoLOX);
		deviceManager.add(servoEthanol);
		
		barometerRunnable = new DeviceRunnable(barometer);
		deviceManager.add(barometerRunnable);
	}

	public void setSensorPriority(SensorPriority priority) {
		if (SensorPriority.SENSOR_PRIORITY_HIGH.equals(priority)) {
			tankPressureLOXRunnable.setThreadSleep(5 /* milliseconds */);
			tankPressureEthanolRunnable.setThreadSleep(5 /* milliseconds */);
			tankPressureEngineRunnable.setThreadSleep(1 /* milliseconds */);
			barometerRunnable.setThreadSleep(1 /* milliseconds */);
		} else if (SensorPriority.SENSOR_PRIORITY_MEDIUM.equals(priority)) {
			tankPressureLOXRunnable.setThreadSleep(50 /* milliseconds */);
			tankPressureEthanolRunnable.setThreadSleep(50 /* milliseconds */);
			tankPressureEngineRunnable.setThreadSleep(10 /* milliseconds */);
			barometerRunnable.setThreadSleep(10 /* milliseconds */);
		} else { // SENSOR_PRIORITY_LOW
			tankPressureLOXRunnable.setThreadSleep(500 /* milliseconds */);
			tankPressureEthanolRunnable.setThreadSleep(500 /* milliseconds */);
			tankPressureEngineRunnable.setThreadSleep(500 /* milliseconds */);
			barometerRunnable.setThreadSleep(200 /* milliseconds */);
		}
	}
	
}
