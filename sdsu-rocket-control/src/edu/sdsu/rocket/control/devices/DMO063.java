package edu.sdsu.rocket.control.devices;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.Timer;
import java.util.TimerTask;

public class DMO063 implements Device {

	public long duration; // milliseconds
	public boolean value;
	
	private DigitalOutput output;
	private int pin;
	
	private Timer timer;
	
	public DMO063(int pin, long duration) {
		this.pin = pin;
		setIgnitionDuration(duration);
	}
	
	public void setIgnitionDuration(long duration) {
		this.duration = duration;
	}
	
	public void ignite() {
		value = true;
		
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				value = false;
				
				timer.cancel();
				timer = null;
			}
		}, duration);
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
		output.write(value);
	}
	

	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": pin=" + pin;
	}
	
}
