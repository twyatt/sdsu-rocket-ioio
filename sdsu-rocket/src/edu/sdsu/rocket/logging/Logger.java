package edu.sdsu.rocket.logging;

public interface Logger {
	
	public enum Level {
		NONE  ( 0 ),
		TRACE ( 1 ),
		DEBUG ( 2 ),
		INFO  ( 3 ),
		WARN  ( 4 ),
		ERROR ( 5 ),
		;
		int value;
		private Level(int value) {
			this.value = value;
		}
	}

	public void i(String tag, String msg);
	public void e(String tag, String msg);
	public void e(String tag, String msg, Throwable e);
	
}
