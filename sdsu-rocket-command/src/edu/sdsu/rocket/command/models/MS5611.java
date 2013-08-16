package edu.sdsu.rocket.command.models;

public class MS5611 {

	volatile public int temperature;
	volatile public int pressure;
	
	/**
	 * Temperature (degrees C).
	 * 
	 * @return
	 */
	public float getTemperature() {
		return temperature / 100f;
	}
	
	/**
	 * Pressure (mbar).
	 * 
	 * @return
	 */
	public float getPressure() {
		return pressure / 100f;
	}
	
	public String toString() {
		return getClass().getSimpleName() + ": temperature=" + getTemperature() + " C, pressure=" + getPressure() + " mbar";
	}
	
}
