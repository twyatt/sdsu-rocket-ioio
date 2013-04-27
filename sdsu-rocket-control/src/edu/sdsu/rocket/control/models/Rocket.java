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
		ignitor = new DMO063(3 /* pin */, 3000L /* duration (milliseconds) */);
		
		// max voltage for pin 35 = 3.3V
		tankPressureLOX     = new P51500AA1365V(35 /* pin */, 175.94f /* slope */, -149.6f /* bias */);
		tankPressureEthanol = new P51500AA1365V(36 /* pin */, 175.94f /* slope */, -149.6f /* bias */);
		tankPressureEngine  = new P51500AA1365V(40 /* pin */, 175.94f /* slope */, -149.6f /* bias */);
		
		servoLOX = new PS050(3 /* pin */, 100 /* frequency */);
//		servoEthanol = new PS050(3 /* pin */, 100 /* frequency */); // FIXME which pin?
		
		// TODO test oversampling of 3 (max)
		// twiNum 0 = pin 4 (SDA) and 5 (SCL)
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
		
		barometer2 = new MS5611(0 /* twiNum */, MS5611.ADD_CSB_LOW /* address */, 30 /* sample rate */);
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
