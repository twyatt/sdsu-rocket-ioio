package edu.sdsu.rocket.control.devices;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class Relay extends DeviceAdapter {

	private DigitalOutput output;
	private int pin;
	
	private boolean state;
	
	public Relay(int pin) {
		this.pin = pin;
	}
	
	public boolean getState() {
		return state;
	}
	
	public void high() {
		state = true;
	}
	
	public boolean isHigh() {
		return state;
	}
	
	public void low() {
		state = false;
	}
	
	public boolean isLow() {
		return !state;
	}
	
	/*
	 * Device interface methods.
	 */
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		output = ioio.openDigitalOutput(pin);
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		output.write(state);
		super.loop();
	}
	
	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": pin=" + pin;
	}
	
}
