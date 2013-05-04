package edu.sdsu.rocket.control.devices;

import edu.sdsu.rocket.control.App;
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
	
	public void setup(IOIO ioio) throws ConnectionLostException {
		input = ioio.openAnalogInput(pin);
	}
	
	public float getPressure(float voltage) {
		float psi = slope * voltage + bias;
		return psi;
	}

	@Override
	public void loop() {
		try {
			float v = input.getVoltage();
			
			if (listener != null) {
				listener.onP51500AA1365VValue(v);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": pin=" + pin;
	}
	
}
