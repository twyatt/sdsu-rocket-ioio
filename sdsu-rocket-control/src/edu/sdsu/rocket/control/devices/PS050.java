package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Servo
 */
public class PS050 implements Device {

	private PwmOutput pwm;
	private int pwmPin;
	private int pwmFrequency; // Hz
	private int pulseWidth;

	public PS050(int pwmPin, int pwmFrequency) {
		this.pwmPin = pwmPin;
		this.pwmFrequency = pwmFrequency;
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		pwm = ioio.openPwmOutput(pwmPin, pwmFrequency);
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		// TODO check if lastPulseWidth != pulseWidth to decide if we should call setPulseWidth?
		pwm.setPulseWidth(pulseWidth);
		Thread.sleep(100);
	}
	
	/**
	 * Sets servo position.
	 * 
	 * @param percent Between 0 and 100
	 */
	public void setPositionPercent(int percent) {
		setPulseWidth(percent * 2);
	}
	
	/**
	 * Sets the pulse width (beyond base pulse) to send to the servo.
	 * 
	 * @param width Between 0 and 200
	 */
	public void setPulseWidth(int width) {
		pulseWidth = 500 + width;
	}

	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + pwmPin + ", frequency=" + pwmFrequency;
	}
	
}
