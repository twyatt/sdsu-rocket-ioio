package edu.sdsu.rocket.helpers;

public class LooperRunnable extends ThreadTimer implements Runnable {

	public interface LooperRunnableListener {
		public void loop();
		public void interrupted();
	}
	
	private LooperRunnableListener listener;

	public LooperRunnable(LooperRunnableListener listener) {
		if (listener == null)
			throw new NullPointerException();
		this.listener = listener;
	}
	
	@Override
	public void run() {
		// http://stackoverflow.com/questions/141560/should-try-catch-go-inside-or-outside-a-loop
		try {
			while (!Thread.currentThread().isInterrupted()) {
				listener.loop();
				sleep();
			}
		} catch (InterruptedException e) {
			listener.interrupted();
		}
	}

}
