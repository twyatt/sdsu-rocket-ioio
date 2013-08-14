package edu.sdsu.rocket.command.models;

public class MAX31855 {
	
	volatile public float internal;
	volatile public float thermocouple;
	
	public float getInternalTemperature() {
		return internal;
	}
	
	public float getTemperature() {
		return thermocouple;
	}
	
	public String toString() {
		return getClass().getSimpleName() + ": internal=" + getInternalTemperature() + "C, temperature=" + getTemperature() + " C";
	}
}
