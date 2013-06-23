package edu.sdsu.rocket.control.devices;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class BreakWire implements Device {

	private int pin;
	private DigitalInput wire;
	
	private boolean isBroken;

	public BreakWire(int pin) {
		this.pin = pin;
	}
	
	public boolean isBroken() {
		return isBroken;
	}

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		wire = ioio.openDigitalInput(pin, DigitalInput.Spec.Mode.PULL_UP);
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		isBroken = wire.read();
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
