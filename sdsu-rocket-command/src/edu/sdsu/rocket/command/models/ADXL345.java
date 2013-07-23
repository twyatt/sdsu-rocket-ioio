package edu.sdsu.rocket.command.models;

public class ADXL345 {
	
	volatile public float multiplier;
	volatile public int x;
	volatile public int y;
	volatile public int z;
	
	public float getX() {
		return (float) x * multiplier * 9.8f;
	}
	
	public float getY() {
		return (float) y * multiplier * 9.8f;
	}
	
	public float getZ() {
		return (float) z * multiplier * 9.8f;
	}
	
	public String toString() {
		return "[" + getX() + ", " + getY() + ", " + getZ() + "]";
	}

}
