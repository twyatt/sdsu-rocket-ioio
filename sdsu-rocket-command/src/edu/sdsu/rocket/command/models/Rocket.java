package edu.sdsu.rocket.command.models;

public class Rocket {

	public String ident;
	
	public Ignitor ignitor = new Ignitor();
	public BreakWire breakWire = new BreakWire();
	
	public P51500AA1365V pressureLOX = new P51500AA1365V();
	public P51500AA1365V pressureEthanol = new P51500AA1365V();
	public P51500AA1365V pressureEngine = new P51500AA1365V();
	
	public ADXL345 accelerometer = new ADXL345();
	public PhoneAccelerometer internalAccelerometer = new PhoneAccelerometer();
	
}
