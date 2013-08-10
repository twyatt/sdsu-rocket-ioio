package edu.sdsu.rocket.control.models;

import ioio.lib.api.SpiMaster;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import android.hardware.SensorManager;
import edu.sdsu.rocket.control.devices.ADXL345;
import edu.sdsu.rocket.control.devices.Arduino;
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
import edu.sdsu.rocket.control.devices.MS5611.OversamplingRatio;

public class Rocket {
	
	private static final float SERVO_SIGNAL_DURATION = 3.0f; // seconds
	private static final float IGNITOR_SIGNAL_DURATION = 3.0f; // seconds

	public enum Mode {
		
	}
	
	public enum SensorPriority {
		SENSOR_PRIORITY_LOW,
		SENSOR_PRIORITY_MEDIUM,
		SENSOR_PRIORITY_HIGH,
	}
	
	public SB70 connection1;
	public SB70 connection2;
	public Arduino arduino;
	
	public RelayIgnitor ignitor;
	public BreakWire breakWire;
	public RelayValve fuelValve;
	
	public P51500AA1365V tankPressureLOX;
	public P51500AA1365V tankPressureEthanol;
	public P51500AA1365V tankPressureEngine;
	
	public RelayValve loxValve;
	public ServoValve ethanolValve;
	
	public ADXL345 accelerometer;
	public MS5611 barometer;
	
	public PhoneAccelerometer internalAccelerometer;
	
	DeviceRunnable tankPressureLOXRunnable;
	DeviceRunnable tankPressureEthanolRunnable;
	DeviceRunnable tankPressureEngineRunnable;
	DeviceRunnable barometerRunnable;
	
	public Rocket() {
		connection1 = new SB70(45 /* RX */, 46 /* TX */, 57600, Parity.NONE, StopBits.ONE);
		connection2 = new SB70( 9 /* RX */, 10 /* TX */, 57600, Parity.NONE, StopBits.ONE);
		arduino = new Arduino(35 /* RX */, 34 /* TX */, 9600, Parity.NONE, StopBits.ONE);
		
		ignitor = new RelayIgnitor(new Relay(12 /* pin */), IGNITOR_SIGNAL_DURATION);
		fuelValve = new RelayValve(new Relay(13 /* pin */));
		breakWire = new BreakWire(3 /* pin */);
		
		// max voltage for analog input = 3.3V
		// calibrated May 12, 2013
		tankPressureLOX     = new P51500AA1365V(41 /* pin */, 179.0827f /* slope */, -145.268f /* bias */);
		tankPressureEthanol = new P51500AA1365V(42 /* pin */, 181.8296f /* slope */, -144.22f /* bias */);
		tankPressureEngine  = new P51500AA1365V(43 /* pin */, 179.7781f /* slope */, -140.324f /* bias */);
		
		ethanolValve = new ServoValve(new PS050(11 /* pin */, 100 /* frequency */), SERVO_SIGNAL_DURATION);
		loxValve     = new RelayValve(new Relay(14 /* pin */));
		
		accelerometer = new ADXL345(29 /* miso */, 28 /* mosi */, 27 /* scl */, 30 /* cs */, SpiMaster.Rate.RATE_31K);
		
		// DA0 = pin 4, CL0 = pin 5
		barometer = new MS5611(0 /* twiNum */, TwiMaster.Rate.RATE_100KHz, OversamplingRatio.OSR_4096);
		
		
		/*
		 * Devices internal to the Android phone (not connected via the IOIO).
		 */
		
		internalAccelerometer = new PhoneAccelerometer(SensorManager.SENSOR_DELAY_FASTEST);
	}
	
}
