package edu.sdsu.rocket.control.devices;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class BreakWire extends DeviceAdapter {

	volatile private boolean isBroken;
	
	private final int pin;
	private DigitalInput wire;

	public BreakWire(int pin) {
		this.pin = pin;
	}
	
	public boolean isBroken() {
		return isBroken;
	}

	/*
	 * IOIOLooper interface methods.
	 */
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		wire = ioio.openDigitalInput(pin, DigitalInput.Spec.Mode.PULL_UP);
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		isBroken = wire.read();
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
