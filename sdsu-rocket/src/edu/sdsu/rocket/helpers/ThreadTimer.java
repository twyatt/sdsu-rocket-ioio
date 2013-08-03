package edu.sdsu.rocket.helpers;

public class ThreadTimer {

	/**
	 * Default sleep is 1000 ms delay (1 Hz).
	 */
	private static final long DEFAULT_SLEEP = 1000L;
	
	/**
	 * Duration to sleep thread (in milliseconds).
	 */
	private volatile long sleep;
	
	public ThreadTimer() {
		setSleep(DEFAULT_SLEEP);
	}
	
	public ThreadTimer(float frequency) {
		setFrequency(frequency);
	}
	
	public ThreadTimer(long sleep) {
		setSleep(sleep);
	}
	
	/**
	 * Sets the thread frequency.
	 * 
	 * If frequency is either 0 or Float.POSITIVE_INFINITY then thread will not
	 * be slept.
	 * 
	 * @param frequency Thread frequency (Hz).
	 */
	public void setFrequency(float frequency) {
		if (frequency == 0f || frequency == Float.POSITIVE_INFINITY) {
			setSleep(0L);
		} else {
			setSleep(Math.round(1000f / frequency));
		}
	}
	
	/**
	 * Sets the duration to sleep between thread loops.
	 * 
	 * @param sleep Thread loop sleep duration (milliseconds).
	 */
	public void setSleep(long sleep) {
		this.sleep = sleep;
	}
	
	/**
	 * Sleeps the thread.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public void sleep() throws InterruptedException {
		if (sleep != 0L)
			Thread.sleep(sleep);
	}
	
}
