package edu.sdsu.rocket.command.models;

public class PhoneAccelerometer implements Accelerometer {
	
	volatile public float x;
	volatile public float y;
	volatile public float z;
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getZ() {
		return z;
	}
	
	public String toString() {
		return "[" + getX() + ", " + getY() + ", " + getZ() + "]";
	}
}
