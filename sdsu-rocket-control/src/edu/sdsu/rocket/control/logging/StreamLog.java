package edu.sdsu.rocket.control.logging;

import java.io.IOException;
import java.io.OutputStream;

import edu.sdsu.rocket.logging.Logger;

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
	public void e(String tag, String msg, Exception e) {
		String text = msg + "\n" + e.getMessage();
		e(tag, text);
	}

}
