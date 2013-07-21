package edu.sdsu.rocket.control.devices;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Servo
 * 
 * Supports being disabled which closes the IOIO PWM output to help prevent
 * burning out servos.
 * 
 * Servo is disabled by default and must be enabled prior to use.
 */
public class PS050 extends DeviceAdapter {

	private IOIO ioio;
	
	private PwmOutput pwm;
	private int pwmPin;
	private int pwmFrequency; // Hz
	private int pulseWidth;
	
	public PS050(int pwmPin, int pwmFrequency) {
		this.pwmPin = pwmPin;
		this.pwmFrequency = pwmFrequency;
	}
	
	public void enable() throws ConnectionLostException {
		if (pwm == null)
			pwm = ioio.openPwmOutput(new DigitalOutput.Spec(pwmPin, Mode.OPEN_DRAIN), pwmFrequency);
	}
	
	public void disable() {
		if (pwm != null) {
			pwm.close();
			pwm = null;
		}
	}
	
	/**
	 * Sets servo position.
	 * 
	 * @param percent Between 0 and 100
	 */
	public void setPositionPercent(int percent) {
		setPulseWidth(percent * 20);
	}
	
	/**
	 * Sets the pulse width (beyond base pulse) to send to the servo.
	 * 
	 * @param width Between 0 and 2000
	 */
	public void setPulseWidth(int width) {
		pulseWidth = 500 + width;
	}

	/*
	 * Device interface methods.
	 */
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		this.ioio = ioio;
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		if (pwm != null)
			pwm.setPulseWidth(pulseWidth);
		
		super.loop();
	}
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + pwmPin + ", frequency=" + pwmFrequency;
	}
	
}
