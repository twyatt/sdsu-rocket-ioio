package edu.sdsu.rocket.command.models;

public class ITG3205 {

	volatile public int x;
	volatile public int y;
	volatile public int z;
	volatile public int temperature;
	
	/**
	 * Temperature (degrees C).
	 * 
	 * @return
	 */
	public float getTemperature() {
		return (35f + (float) (temperature + 13200) / 280f);
	}
	
	public float getX() {
		return ((float) x / 14.375f);
	}
	
	public float getY() {
		return ((float) y / 14.375f);
	}
	
	public float getZ() {
		return ((float) z / 14.375f);
	}
	
	public String toString() {
		return getClass().getSimpleName() + ": x=" + getX() + ", y=" + getY() + ", z=" + getZ() + "temperature=" + getTemperature() + " C";
	}
	
}
