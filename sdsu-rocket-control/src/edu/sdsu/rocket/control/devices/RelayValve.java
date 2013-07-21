package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Valve controlled by a relay.
 */
public class RelayValve extends DeviceAdapter implements Valve {

	private Relay relay;

	public RelayValve(Relay relay) {
		this.relay = relay;
	}
	
	/*
	 * Valve interface methods.
	 */

	@Override
	public void open() {
		relay.high();
	}

	@Override
	public void close() {
		relay.low();
	}

	@Override
	public boolean isOpen() {
		return relay.isHigh();
	}

	/*
	 * IOIOLooper interface methods.
	 */
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		relay.setup(ioio);
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		relay.loop();
	}
	
	/*
	 * Device interface methods.
	 */

	@Override
	public String info() {
		return relay.info();
	}


}
