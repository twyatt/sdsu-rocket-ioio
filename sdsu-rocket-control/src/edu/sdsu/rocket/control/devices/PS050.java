package edu.sdsu.rocket.control.devices;

import edu.sdsu.rocket.control.App;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Servo
 */
public class PS050 implements Device {

	private static final float ACTION_DURATION = 3.0f; // seconds
	
	private IOIO ioio;
	
	private PwmOutput pwm;
	private int pwmPin;
	private int pwmFrequency; // Hz
	private int pulseWidth;
	
	/*
	 * Timestamp of last action.
	 */
	private float lastActionTime;

	public PS050(int pwmPin, int pwmFrequency) {
		this.pwmPin = pwmPin;
		this.pwmFrequency = pwmFrequency;
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		this.ioio = ioio;
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		if (App.elapsedTime() - lastActionTime > ACTION_DURATION) {
			if (pwm != null) {
				pwm.close();
				pwm = null;
			}
		} else {
			if (pwm == null) {
				pwm = ioio.openPwmOutput(new DigitalOutput.Spec(pwmPin, Mode.OPEN_DRAIN), pwmFrequency);
			}
			pwm.setPulseWidth(pulseWidth);
		}
	}
	
	public void open() {
		setPositionPercent(90);
//		pulseWidth = 500 + 200;
		
		lastActionTime = App.elapsedTime();
	}
	
	public void close() {
		setPositionPercent(10);
//		pulseWidth = 500 + 1000 - 200;
		
		lastActionTime = App.elapsedTime();
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
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + pwmPin + ", frequency=" + pwmFrequency;
	}
	
}
