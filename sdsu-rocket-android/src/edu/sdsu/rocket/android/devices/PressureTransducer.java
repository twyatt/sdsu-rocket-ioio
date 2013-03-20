package edu.sdsu.rocket.android.devices;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class PressureTransducer implements Device {

	private AnalogInput input;
	private int pin;
	
	private float slope;
	private float bias;

	public PressureTransducer(int pin, float slope, float bias) {
		this.pin = pin;
		this.slope = slope;
		this.bias = bias;
	}
	
	public void setup(IOIO ioio) throws ConnectionLostException {
		input = ioio.openAnalogInput(pin);
	}
	
	public float readPressure() throws InterruptedException, ConnectionLostException {
		float voltage = input.getVoltage();
		float psi = slope * voltage + bias;
		return psi;
	}

	@Override
	public void loop() {
		// TODO Auto-generated method stub
		
	}
	
}
