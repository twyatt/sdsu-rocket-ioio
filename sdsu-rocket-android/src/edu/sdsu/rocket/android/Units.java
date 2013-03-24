package edu.sdsu.rocket.android;

public class Units {

	public static float convertMetersToFeet(float meters) {
		return meters * 3.28084f;
	}

	// (F - 32) * 5/9 = C
	public static double convertFahrenheitToCelsius(double fahrenheit) {
		return (fahrenheit - 32D) * 5D / 9D;
	}
	
	// (C * 9/5) + 32 = F
	public static double convertCelsiusToFahrenheit(double celsius) {
		return (celsius * 9D / 5D) + 32D;
	}
	
}
