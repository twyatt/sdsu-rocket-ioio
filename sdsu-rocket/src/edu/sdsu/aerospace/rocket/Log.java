package edu.sdsu.aerospace.rocket;

public class Log {

	public static void i(String message) {
		System.out.println(message);
	}
	
	public static void e(String message) {
		System.err.println(message);
	}
	
	public static void e(String message, Throwable exception) {
		System.err.println(message);
		exception.printStackTrace(System.err);
	}

}
