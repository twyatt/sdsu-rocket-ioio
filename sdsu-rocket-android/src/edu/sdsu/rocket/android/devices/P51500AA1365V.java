package edu.sdsu.rocket.android.devices;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Pressure transducer
 * {@link http://www.digikey.com/product-detail/en/P51-500-A-A-I36-5V-000-000/734-1063-ND/1665825}
 */
public class P51500AA1365V implements Device {

	private AnalogInput input;
	private int pin;
	
	private float slope;
	private float bias;

	public P51500AA1365V(int pin, float slope, float bias) {
		this.pin = pin;
		this.slope = slope;
		this.bias = bias;
	}
	
	public void setup(IOIO ioio) throws ConnectionLostException {
		input = ioio.openAnalogInput(pin);
	}
	
	public float readPressure() throws InterruptedException, ConnectionLostException {
		float voltage = input.getVoltage();
		float psi = slope * voltage + bias;
		return psi;
	}

	@Override
	public void loop() {
		// TODO Auto-generated method stub
		
	}
	
}
