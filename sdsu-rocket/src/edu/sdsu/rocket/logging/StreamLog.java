package edu.sdsu.rocket.logging;

import java.io.IOException;
import java.io.OutputStream;

public class StreamLog implements Logger {

	private OutputStream out;

	public StreamLog(OutputStream out) {
		this.out = out;
	}
	
	@Override
	public void i(String tag, String msg) {
		String text = msg + "\n";
		try {
			out.write(text.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void e(String tag, String msg) {
		i(tag, msg);
	}

	@Override
	public void e(String tag, String msg, Throwable e) {
		String text = msg + "\n" + e.getMessage();
		e(tag, text);
	}

}
