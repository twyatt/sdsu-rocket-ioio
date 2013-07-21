package edu.sdsu.rocket.control.models;

import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import android.hardware.SensorManager;
import edu.sdsu.rocket.control.devices.BreakWire;
import edu.sdsu.rocket.control.devices.DeviceRunnable;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.devices.PS050;
import edu.sdsu.rocket.control.devices.PhoneAccelerometer;
import edu.sdsu.rocket.control.devices.Relay;
import edu.sdsu.rocket.control.devices.RelayIgnitor;
import edu.sdsu.rocket.control.devices.RelayValve;
import edu.sdsu.rocket.control.devices.SB70;
import edu.sdsu.rocket.control.devices.ServoValve;

public class Rocket {
	
	private static final float ACTION_DURATION = 3.0f; // seconds

	public enum Mode {
		
	}
	
	public enum SensorPriority {
		SENSOR_PRIORITY_LOW,
		SENSOR_PRIORITY_MEDIUM,
		SENSOR_PRIORITY_HIGH,
	}
	
	public SB70 connection1;
	public SB70 connection2;
	
	public RelayIgnitor ignitor;
	public BreakWire breakWire;
	public RelayValve fuelValve;
	
	public P51500AA1365V tankPressureLOX;
	public P51500AA1365V tankPressureEthanol;
	public P51500AA1365V tankPressureEngine;
	
	public RelayValve loxValve;
	public ServoValve ethanolValve;
	
	public MS5611 barometer;
	
	public PhoneAccelerometer accelerometer;
	
	DeviceRunnable tankPressureLOXRunnable;
	DeviceRunnable tankPressureEthanolRunnable;
	DeviceRunnable tankPressureEngineRunnable;
	DeviceRunnable barometerRunnable;
	
	public Rocket() {
		connection1 = new SB70(45, 46, 57600, Parity.NONE, StopBits.ONE);
		connection2 = new SB70( 9, 10, 57600, Parity.NONE, StopBits.ONE);
		
		ignitor = new RelayIgnitor(new Relay(21 /* pin */), 3.0f /* duration (seconds) */);
		fuelValve = new RelayValve(new Relay(20 /* pin */));
		breakWire = new BreakWire(9 /* pin */);
		
		// max voltage for analog input = 3.3V
		// calibrated May 12, 2013
		tankPressureLOX     = new P51500AA1365V(41 /* pin */, 179.0827f /* slope */, -145.268f /* bias */);
		tankPressureEthanol = new P51500AA1365V(42 /* pin */, 181.8296f /* slope */, -144.22f /* bias */);
		tankPressureEngine  = new P51500AA1365V(43 /* pin */, 179.7781f /* slope */, -140.324f /* bias */);
		
		loxValve     = new RelayValve(new Relay(13 /* pin */));
		ethanolValve = new ServoValve(new PS050(14 /* pin */, 100 /* frequency */), ACTION_DURATION);
		
		// twiNum 1 = pin 1 (SDA) and 2 (SCL)
		// VCC = 3.3V
		barometer = new MS5611(1 /* twiNum */, MS5611.ADD_CSB_LOW /* address */, 30 /* sample rate */);
		
		/*
		 * Devices internal to the Android phone (not connected via the IOIO).
		 */
		
		accelerometer = new PhoneAccelerometer(SensorManager.SENSOR_DELAY_FASTEST);
	}
	
}
