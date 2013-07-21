package edu.sdsu.rocket.control.devices;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Pressure transducer
 * {@link http://www.digikey.com/product-detail/en/P51-500-A-A-I36-5V-000-000/734-1063-ND/1665825}
 */
public class P51500AA1365V extends DeviceAdapter  {

	public interface P51500AA1365VListener {
		public void onVoltage(float voltage);
	}
	
	volatile private float voltage;
	
	private P51500AA1365VListener listener;
	
	private AnalogInput input;
	public final int pin;
	
	final private float slope;
	final private float bias;

	public P51500AA1365V(int pin, float slope, float bias) {
		this.pin = pin;
		this.slope = slope;
		this.bias = bias;
	}
	
	public void setListener(P51500AA1365VListener listener) {
		this.listener = listener;
	}
	
	public float getVoltage() {
		return voltage;
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
		
		if (listener != null)
			listener.onVoltage(voltage);
		
		super.loop();
	}
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": pin=" + pin;
	}
	
}
