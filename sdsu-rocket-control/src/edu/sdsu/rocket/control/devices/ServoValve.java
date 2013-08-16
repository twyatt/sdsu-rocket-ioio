package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import edu.sdsu.rocket.control.App;

public class ServoValve extends DeviceAdapter implements Valve {
	
	public static final int OPEN_POSITION_PERCENT  = 90;
	public static final int CLOSE_POSITION_PERCENT = 25;

	private PS050 servo;
	
	private boolean isOpen;
	
	/**
	 * Duration to send open or close signal in seconds.
	 */
	private float duration;
	
	/*
	 * Timestamp of last action.
	 */
	private float lastTime;

	public ServoValve(PS050 servo, float duration) {
		this.servo = servo;
		this.duration = duration;
	}
	
	@Override
	public void setSleep(long sleep) {
		if (servo != null)
			servo.setSleep(sleep);
	}
	
	/*
	 * Valve interface methods.
	 */

	@Override
	public void open() {
		lastTime = App.elapsedTime();
		servo.setPositionPercent(OPEN_POSITION_PERCENT);
		isOpen = true;
	}

	@Override
	public void close() {
		lastTime = App.elapsedTime();
		servo.setPositionPercent(CLOSE_POSITION_PERCENT);
		isOpen = false;
	}
	
	@Override
	public boolean isOpen() {
		return isOpen;
	}
	
	/*
	 * IOIOLooper interface methods.
	 */

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		servo.setup(ioio);
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		if (App.elapsedTime() - lastTime > duration) {
			servo.disable();
		} else {
			servo.enable();
			servo.loop();
		}
	}
	
	/*
	 * Device interface methods.
	 */

	@Override
	public String info() {
		return servo.info() + ", duration=" + duration;
	}

}
