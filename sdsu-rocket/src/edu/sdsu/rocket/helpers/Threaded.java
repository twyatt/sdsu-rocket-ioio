package edu.sdsu.rocket.helpers;

import edu.sdsu.rocket.helpers.LooperRunnable.LooperRunnableListener;

public abstract class Threaded implements LooperRunnableListener {
	
	LooperRunnable runnable = new LooperRunnable(this);
	private Thread thread;
	
	/**
	 * Sets the duration to sleep between thread loops (in milliseconds).
	 * 
	 * @param sleep Thread sleep duration (milliseconds).
	 */
	public void setSleep(long sleep) {
		runnable.setSleep(sleep);
	}
	
	/**
	 * Sets the thread frequency (loops per minute).
	 * 
	 * @param frequency Loop frequency (Hz).
	 */
	public void setFrequency(float frequency) {
		runnable.setFrequency(frequency);
	}
	
	public void start() {
		thread = new Thread(runnable);
		thread.start();
	}
	
	public void stop() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
