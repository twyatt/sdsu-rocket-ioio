package edu.sdsu.rocket.control.models;

import ioio.lib.api.SpiMaster;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import android.hardware.SensorManager;
import edu.sdsu.rocket.control.devices.ADXL345;
import edu.sdsu.rocket.control.devices.Arduino;
import edu.sdsu.rocket.control.devices.BreakWire;
import edu.sdsu.rocket.control.devices.MAX31855;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.MS5611.OversamplingRatio;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.devices.PS050;
import edu.sdsu.rocket.control.devices.PhoneAccelerometer;
import edu.sdsu.rocket.control.devices.Relay;
import edu.sdsu.rocket.control.devices.RelayIgnitor;
import edu.sdsu.rocket.control.devices.RelayValve;
import edu.sdsu.rocket.control.devices.SB70;
import edu.sdsu.rocket.control.devices.ServoValve;

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
//	public Arduino arduino;
	
	public RelayIgnitor ignitor;
	public volatile float ignitorTemperature; // value is received from the Arduino
	
	public BreakWire breakWire;
	public RelayValve fuelValve;
	
	public RelayValve loxValve;
	public P51500AA1365V loxPressure;
	public MAX31855 loxTemperature;
	
	public P51500AA1365V ethanolPressure;
	public ServoValve ethanolValve;
	
	public P51500AA1365V enginePressure;
	
	public ADXL345 accelerometer;
	public MS5611 barometer;
	
	public PhoneAccelerometer internalAccelerometer;
	
	public Rocket() {
		connection1 = new SB70(45 /* RX */, 46 /* TX */, 57600, Parity.NONE, StopBits.ONE);
		connection2 = new SB70( 9 /* RX */, 10 /* TX */, 57600, Parity.NONE, StopBits.ONE);
//		arduino = new Arduino(35 /* RX */, 34 /* TX */, 9600, Parity.NONE, StopBits.ONE);
		
		ignitor = new RelayIgnitor(new Relay(12 /* pin */), IGNITOR_SIGNAL_DURATION);
		fuelValve = new RelayValve(new Relay(13 /* pin */));
		breakWire = new BreakWire(3 /* pin */);
		
		// max voltage for analog input = 3.3V
		// calibrated Aug 15, 2013
		loxPressure     = new P51500AA1365V(41 /* pin */, 175.89706f /* slope */, -141.0809f /* bias */);
		ethanolPressure = new P51500AA1365V(42 /* pin */, 176.27741f /* slope */, -135.2379f /* bias */);
		enginePressure  = new P51500AA1365V(43 /* pin */, 168.9382f  /* slope */, -125.1707f /* bias */);
		
		ethanolValve = new ServoValve(new PS050(11 /* pin */, 100 /* frequency */), SERVO_SIGNAL_DURATION);
		loxValve     = new RelayValve(new Relay(14 /* pin */));
		
		accelerometer = new ADXL345(29 /* miso */, 28 /* mosi */, 27 /* scl */, 30 /* cs */, SpiMaster.Rate.RATE_31K);
		
		// DA0 = pin 4, CL0 = pin 5
		barometer = new MS5611(0 /* twiNum */, TwiMaster.Rate.RATE_100KHz, OversamplingRatio.OSR_4096);
		
		loxTemperature = new MAX31855(7 /* miso */, 40 /* mosi */, 6 /* clk */, 8 /* cs */, SpiMaster.Rate.RATE_31K);
		
		/*
		 * Devices internal to the Android phone (not connected via the IOIO).
		 */
		
		internalAccelerometer = new PhoneAccelerometer(SensorManager.SENSOR_DELAY_FASTEST);
	}
	
}
