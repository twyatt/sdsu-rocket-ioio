package edu.sdsu.rocket.command.models;

public class Rocket {

	public String ident;
	
	public ADXL345 accelerometer = new ADXL345();
	public PhoneAccelerometer internalAccelerometer = new PhoneAccelerometer();
	
}
