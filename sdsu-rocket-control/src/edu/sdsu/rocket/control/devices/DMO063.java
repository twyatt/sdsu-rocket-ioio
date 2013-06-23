package edu.sdsu.rocket.control.devices;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import edu.sdsu.rocket.control.App;

public class DMO063 implements Device {

	public float duration; // seconds
	public boolean value;
	
	private DigitalOutput output;
	private int pin;
	
	private float startTime;
	
	public DMO063(int pin, float duration) {
		this.pin = pin;
		setIgnitionDuration(duration);
	}
	
	public void setIgnitionDuration(float duration) {
		this.duration = duration;
	}
	
	public void activate() {
		value = true;
		startTime = App.elapsedTime();
	}
	
	public void deactivate() {
		value = false;
		
		if (output != null) {
			/*
			 * Since deactivating the DMO063 for the fuel valves is very
			 * important we will try to write to the output right away.
			 */
			try {
				output.write(value);
			} catch (ConnectionLostException e) {
//				e.printStackTrace();
			}
		}
	}

	/**
	 * Methods for Device interface.
	 */
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		output = ioio.openDigitalOutput(pin);
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		float elapsed = App.elapsedTime() - startTime;
		if (elapsed > duration) {
			value = false;
		}
		
		output.write(value);
	}
	
	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": pin=" + pin;
	}

	@Override
	public void disconnected() {
		// TODO Auto-generated method stub
	}
	
}
