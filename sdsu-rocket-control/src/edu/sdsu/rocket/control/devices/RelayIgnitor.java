package edu.sdsu.rocket.control.devices;

import edu.sdsu.rocket.control.App;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class RelayIgnitor extends DeviceAdapter {
	
	private final Relay relay;
	
	/**
	 * Duration to keep the ignitor on (in seconds).
	 */
	private final float duration;
	
	/**
	 * Time when ignition starts (in seconds since application start).
	 */
	private float startTime;

	public RelayIgnitor(Relay relay, float duration) {
		this.relay = relay;
		this.duration = duration;
	}
	
	@Override
	public void setSleep(long sleep) {
		if (relay != null)
			relay.setSleep(sleep);
	}
	
	public void ignite() {
		startTime = App.elapsedTime();
		relay.high();
	}
	
	public boolean isActive() {
		return relay.isHigh();
	}
	
	public void cancel() {
		relay.low();
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
		float elapsed = App.elapsedTime() - startTime;
		
		if (elapsed > duration) {
			relay.low();
		}
		
		relay.loop();
	}
	
	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return relay.info() + ", duration=" + duration + "s";
	}

}
