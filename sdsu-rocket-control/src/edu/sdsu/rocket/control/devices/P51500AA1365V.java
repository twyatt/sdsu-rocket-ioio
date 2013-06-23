package edu.sdsu.rocket.control.devices;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Pressure transducer
 * {@link http://www.digikey.com/product-detail/en/P51-500-A-A-I36-5V-000-000/734-1063-ND/1665825}
 */
public class P51500AA1365V implements Device {

	public interface P51500AA1365VListener {
		public void onP51500AA1365VValue(float voltage);
	}
	
	public float voltage;
	
	private P51500AA1365VListener listener;
	
	private AnalogInput input;
	public final int pin;
	
	private float slope;
	private float bias;

	public P51500AA1365V(int pin, float slope, float bias) {
		this.pin = pin;
		this.slope = slope;
		this.bias = bias;
	}
	
	public void setListener(P51500AA1365VListener listener) {
		this.listener = listener;
	}
	
	public float getPressure() {
		float psi = slope * voltage + bias;
		return psi;
	}
	
	/*
	 * Device interface methods.
	 */
	
	public void setup(IOIO ioio) throws ConnectionLostException {
		input = ioio.openAnalogInput(pin);
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		voltage = input.getVoltage();
		
		if (listener != null) {
			listener.onP51500AA1365VValue(voltage);
		}
	}
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": pin=" + pin;
	}

	@Override
	public void disconnected() {
		// TODO Auto-generated method stub
	}
	
}
