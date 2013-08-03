package edu.sdsu.rocket.command.models;

public class Rocket {

	public String ident;
	
	public BreakWire breakWire = new BreakWire();
	
	public ADXL345 accelerometer = new ADXL345();
	public PhoneAccelerometer internalAccelerometer = new PhoneAccelerometer();
	
}
