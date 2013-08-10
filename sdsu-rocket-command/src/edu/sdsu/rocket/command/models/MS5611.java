package edu.sdsu.rocket.command.models;

public class MS5611 {

	volatile public int temperature;
	volatile public int pressure;
	
	public float getTemperature() {
		return temperature / 100f;
	}
	
	public float getPressure() {
		return pressure / 100f;
	}
	
	public String toString() {
		return getClass().getSimpleName() + ": temperature=" + getTemperature() + " C, pressure=" + getPressure() + " mbar";
	}
	
}
