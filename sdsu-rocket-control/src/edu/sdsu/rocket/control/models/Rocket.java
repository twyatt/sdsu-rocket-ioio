package edu.sdsu.rocket.control.models;

import android.hardware.SensorManager;
import edu.sdsu.rocket.control.devices.ArduIMU;
import edu.sdsu.rocket.control.devices.BMP085;
import edu.sdsu.rocket.control.devices.BreakWire;
import edu.sdsu.rocket.control.devices.DMO063;
import edu.sdsu.rocket.control.devices.PhoneAccelerometer;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.devices.PS050;

public class Rocket {

	public DMO063 ignitor;
	public BreakWire breakWire;
	public DMO063 fuelValve;
	
	public P51500AA1365V tankPressureLOX;
	public P51500AA1365V tankPressureEthanol;
	public P51500AA1365V tankPressureEngine;
	
	public PS050 servoLOX;
	public PS050 servoEthanol;
	
	public BMP085 barometer1;
	public MS5611 barometer2;
	
	public ArduIMU imu;
	
	public PhoneAccelerometer accelerometer;
	
	public Rocket() {
		/*
		 * IOIO Pin Overview:
		 * 
		 * 1 = MS5611 SDA
		 * 2 = MS5611 SCL
		 * 3 = BMP085 EOC
		 * 4 = BMP085 SDA
		 * 5 = BMP085 SCL
		 * 9 = Break Wire -
		 * 10 = ArduIMU TX
		 * 13 = Servo PWM LOX Orange Wire (Screw-down Connector #5 Port #3)
		 * 14 = Servo PWM Ethanol Orange Wire (Screw-down Connector #6 Port #3)
		 * 19 = DMO063 #1 Control +
		 * 20 = DMO063 #2 Control +
		 * 21 = DMO063 #3 Control +
		 * 41 = P51500AA1365V #1 White Wire (Screw-down Connector #1 Port #3)
		 * 42 = P51500AA1365V #2 White Wire (Screw-down Connector #2 Port #3)
		 * 43 = P51500AA1365V #3 White Wire (Screw-down Connector #3 Port #3)
		 * 
		 * 3.3V = MS5611 VCC
		 *        BMP085 VCC
		 *        Break Wire +
		 * 3.3V GND = MS5611 GND
		 *            BMP085 GND
		 *            DMO063 #1 Control -
		 *            DMO063 #2 Control -
		 *            DMO063 #3 Control -
		 * 
		 * 5V = P51500AA1365V #1 Red Wire (Screw-down Connector #1 Port #1)
		 *      P51500AA1365V #2 Red Wire (Screw-down Connector #2 Port #1)
		 *      P51500AA1365V #3 Red Wire (Screw-down Connector #3 Port #1)
		 *      Servo Signal VCC LOX Red Wire (Screw-down Connector #5 Port #1)
		 *      Servo Signal VCC Ethanol Red Wire (Screw-down Connector #6 Port #1)
		 *      10k Resistor #1
		 *      10k Resistor #2
		 * 5V GND = P51500AA1365V #1 Black Wire (Screw-down Connector #1 Port #2)
		 *          P51500AA1365V #1 Black Wire (Screw-down Connector #2 Port #2)
		 *          P51500AA1365V #1 Black Wire (Screw-down Connector #3 Port #2)
		 *          Servo Signal GND LOX Brown Wire (Screw-down Connector #5 Port #2)
		 *          Servo Signal GND Ethanol Brown Wire (Screw-down Connector #6 Port #2)
		 * 
		 * 10k Resistor #1 = Screw-down Connector #5 Port #3
		 * 10k Resistor #2 = Screw-down Connector #6 Port #3
		 * 
		 * DMO063 #1 DC Load + = Screw-down Connector #4 Port #1
		 * DMO063 #1 DC Load - = Screw-down Connector #4 Port #2
		 */
		
		// solid state relay on pin 19 seems to have failed
//		ignitor = new DMO063(19 /* pin */, 3.0f /* duration (seconds) */);
		ignitor = new DMO063(21 /* pin */, 3.0f /* duration (seconds) */);
		fuelValve = new DMO063(20 /* pin */, 10.0f /* duration (seconds) */);
		breakWire = new BreakWire(9 /* pin */);
		
		// max voltage for analog input = 3.3V
		// calibrated May 12, 2013
		tankPressureLOX     = new P51500AA1365V(41 /* pin */, 179.0827f /* slope */, -145.268f /* bias */);
		tankPressureEthanol = new P51500AA1365V(42 /* pin */, 181.8296f /* slope */, -144.22f /* bias */);
		tankPressureEngine  = new P51500AA1365V(43 /* pin */, 179.7781f /* slope */, -140.324f /* bias */);
		
		servoLOX = new PS050(13 /* pin */, 100 /* frequency */);
		servoEthanol = new PS050(14 /* pin */, 100 /* frequency */);
		
		// TODO test oversampling of 3 (max)
		// twiNum 0 = pin 4 (SDA) and 5 (SCL)
		// VCC = 3.3V
		barometer1 = new BMP085(0 /* twiNum */, 3 /* eocPin */, 0 /* oversampling */);
		
		// twiNum 1 = pin 1 (SDA) and 2 (SCL)
		// VCC = 3.3V
		barometer2 = new MS5611(1 /* twiNum */, MS5611.ADD_CSB_LOW /* address */, 30 /* sample rate */);
		
		imu = new ArduIMU(10 /* RX pin */);
		
		/*
		 * Devices internal to the Android phone (not connected via the IOIO).
		 */
		
		accelerometer = new PhoneAccelerometer(SensorManager.SENSOR_DELAY_FASTEST);
		
	}
	
}
