package edu.sdsu.rocket.control.models;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.Units;
import edu.sdsu.rocket.control.devices.ArduIMU;
import edu.sdsu.rocket.control.devices.BMP085;
import edu.sdsu.rocket.control.devices.DMO063;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.devices.PS050;

public class Rocket {

	public DMO063 ignitor;
	
	public P51500AA1365V tankPressureLOX;
	public P51500AA1365V tankPressureEthanol;
	public P51500AA1365V tankPressureEngine;
	
	public PS050 servoLOX;
	public PS050 servoEthanol;
	
	public BMP085 barometer1;
	public MS5611 barometer2;
	
	public ArduIMU imu;
	
	public Rocket() {
		/*
		 * IOIO Pin Overview:
		 * 
		 * 1 = MS5611 SDA
		 * 2 = MS5611 SCL
		 * 3 = BMP085 EOC
		 * 4 = BMP085 SDA
		 * 5 = BMP085 SCL
		 * 10 = ArduIMU TX
		 * 13 = Servo PWM LOX = Screw-down Connector #5 Port #3
		 * 14 = Servo PWM Ethanol = Screw-down Connector #6 Port #3
		 * 19 = DMO063 #1 Control +
		 * 20 = DMO063 #2 Control +
		 * 21 = DMO063 #3 Control +
		 * 41 = P51500AA1365V #1 White Wire (Screw-down Connector #1 Port #3)
		 * 42 = P51500AA1365V #2 White Wire (Screw-down Connector #2 Port #3)
		 * 43 = P51500AA1365V #3 White Wire (Screw-down Connector #3 Port #3)
		 * 
		 * 3.3V = MS5611 VCC
		 *        BMP085 VCC
		 * 3.3V GND = MS5611 GND
		 *            BMP085 GND
		 *            DMO063 #1 Control -
		 *            DMO063 #2 Control -
		 *            DMO063 #3 Control -
		 * 
		 * 5V = P51500AA1365V #1 Red Wire (Screw-down Connector #1 Port #1)
		 *      P51500AA1365V #2 Red Wire (Screw-down Connector #2 Port #1)
		 *      P51500AA1365V #3 Red Wire (Screw-down Connector #3 Port #1)
		 *      Servo Signal VCC LOX (Screw-down Connector #5 Port #1)
		 *      Servo Signal VCC Ethanol (Screw-down Connector #6 Port #1)
		 *      10k Resistor #1
		 *      10k Resistor #2
		 * 5V GND = P51500AA1365V #1 Black Wire (Screw-down Connector #1 Port #2)
		 *          P51500AA1365V #1 Black Wire (Screw-down Connector #2 Port #2)
		 *          P51500AA1365V #1 Black Wire (Screw-down Connector #3 Port #2)
		 *          Servo Signal GND LOX (Screw-down Connector #5 Port #2)
		 *          Servo Signal GND Ethanol (Screw-down Connector #6 Port #2)
		 * 
		 * 10k Resistor #1 = Screw-down Connector #5 Port #3
		 * 10k Resistor #2 = Screw-down Connector #6 Port #3
		 */
		
		ignitor = new DMO063(19 /* pin */, 3000L /* duration (milliseconds) */);
		
		// max voltage for analog input = 3.3V
		tankPressureLOX     = new P51500AA1365V(41 /* pin */, 175.94f /* slope */, -149.6f /* bias */);
		tankPressureEthanol = new P51500AA1365V(42 /* pin */, 175.94f /* slope */, -149.6f /* bias */);
		tankPressureEngine  = new P51500AA1365V(43 /* pin */, 175.94f /* slope */, -149.6f /* bias */);
		
		servoLOX = new PS050(3 /* pin */, 100 /* frequency */);
//		servoEthanol = new PS050(3 /* pin */, 100 /* frequency */); // FIXME which pin?
		
		// TODO test oversampling of 3 (max)
		// twiNum 0 = pin 4 (SDA) and 5 (SCL)
		// VCC = 3.3V
		barometer1 = new BMP085(0 /* twiNum */, 3 /* eocPin */, 0 /* oversampling */);
		barometer1.setListener(new BMP085.BMP085Listener() {
			@Override
			public void onBMP085Values(float pressure, double temperature) {
				float alt = BMP085.altitude(pressure, BMP085.p0);
				alt = Units.convertMetersToFeet(alt);
				double temp = Units.convertCelsiusToFahrenheit(temperature);
				
				App.log.i(App.TAG, "Pressure: " + pressure + ", Temperature: " + temperature);
//				updateTextView(ioioStatusTextView, "A: " + alt + ", \nT: " + temp);
			}
		});
		
		// twiNum 1 = pin 1 (SDA) and 2 (SCL)
		// VCC = 3.3V
		barometer2 = new MS5611(1 /* twiNum */, MS5611.ADD_CSB_LOW /* address */, 30 /* sample rate */);
		barometer2.setListener(new MS5611.MS5611Listener() {
			@Override
			public void onMS5611Values(float pressure /* mbar */, float temperature /* C */) {
				temperature += 273.15f; // K
				
				/*
				 * http://en.wikipedia.org/wiki/Density_altitude
				 */
				float Psl = 1013.25f; // standard sea level atmospheric pressure (hPa)
				float Tsl = 288.15f; // ISA standard sea level air temperature (K)
				float b = 0.234969f;
//				float altitude = 145442.156f * (1f - (float)Math.pow((pressure / Psl) / (temperature / Tsl), b));
				
				float altitude = (((float)Math.pow(Psl / pressure, 1f/5.257f) - 1f) * temperature) / 0.0065f;
				
//				updateTextView(ioioStatusTextView, "P: " + pressure + " mbar\nT: " + (temperature - 273.15f) + " C\nA: " + altitude + " ft");
			}
		});
		
		imu = new ArduIMU(10 /* RX pin */);
		imu.setListener(new ArduIMU.ArduIMUListener() {
			@Override
			public void onArduIMUValues(String values) {
				App.log.i(App.TAG, "uart_rx=" + values);
			}
		});
	}
	
}
