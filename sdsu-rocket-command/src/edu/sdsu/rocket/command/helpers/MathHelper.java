package edu.sdsu.rocket.command.helpers;

import java.awt.Point;

public class MathHelper {
	
	/**
	 * http://stackoverflow.com/questions/929103/convert-a-number-range-to-another-range-maintaining-ratio
	 */
	public static float linearConversion(float oldMin, float oldMax, float newMin, float newMax, float value) {
		return ((value - oldMin) / (oldMax - oldMin)) * (newMax - newMin) + newMin;
	}
	
	public static Point getPointOnOval(float a, float b, float angle) {
		int x = (int) Math.round(a * Math.cos(angle));
		int y = (int) Math.round(b * Math.sin(angle));
		return new Point(x, y);
	}
	
}
