package edu.sdsu.rocket.logging;

public interface Logger {

	public void i(String tag, String msg);
	public void e(String tag, String msg, Exception e);
	
}
