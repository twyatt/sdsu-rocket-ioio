package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

import edu.sdsu.rocket.control.App;

public class Arduino extends DeviceAdapter {
	
	public static final int RESPONSE_LENGTH = 8;
	
	public static final DecimalFormat format = new DecimalFormat();
	
	public interface ArduinoListener {
		public void onRequest(int request, float value);
	}
	
	private ArduinoListener listener;
	
	private Uart uart;
	private int rxPin;
	private int txPin;
	private int baud;
	private Parity parity;
	private StopBits stopbits;
	
	private BufferedReader in;
	private BufferedWriter out;

	public Arduino(int rxPin, int txPin, int baud, Parity parity, StopBits stopbits) {
		this.rxPin = rxPin;
		this.txPin = txPin;
		this.baud = baud;
		this.parity = parity;
		this.stopbits = stopbits;
		setSleep(0L);
		format.setMaximumFractionDigits(2);
	}
	
	public Arduino setListener(ArduinoListener listener) {
		this.listener = listener;
		return this;
	}
	
	/**
	 * Sends a value to the Arduino.
	 * 
	 * The value is padded is spaces if length is less than 8 characters.
	 * 
	 * @param value
	 * @throws IOException 
	 */
	public void sendResponse(float value) throws IOException {
		String text = format.format(value);
		String message;
		if (text.length() < 8) {
			message = text;
		} else {
			message = text.substring(0, 7);
		}
		String send = " " + message;
//		System.out.println("Sending to Arduino: " + send);
		out.write(send);
		out.flush();
	}
	
	/*
	 * Device interface methods.
	 */

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		uart = ioio.openUart(rxPin, txPin, baud, parity, stopbits);
		
		try {
			in = new BufferedReader(new InputStreamReader(uart.getInputStream(), "ASCII"));
			out = new BufferedWriter(new OutputStreamWriter(uart.getOutputStream(), "ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		try {
			String line = in.readLine();
			System.out.println("Arduino: " + line);
			
			String[] parts = line.split(" ");
			if (parts.length < 2) {
				App.stats.ioio.errors.incrementAndGet();
				App.log.e(App.TAG, "Invalid Arduino message: " + line);
				return;
			}
			
			try {
				int request = Integer.parseInt(parts[0]);
				float value = Float.parseFloat(parts[1]);
				
				if (listener != null) {
					listener.onRequest(request, value);
				}
			} catch (NumberFormatException e) {
				App.stats.ioio.errors.incrementAndGet();
				App.log.e(App.TAG, "Invalid Arduino request: " + parts[0] + ", " + parts[1]);
				return;
			}
		} catch (IOException e) {
			App.stats.ioio.errors.incrementAndGet();
			App.log.e(App.TAG, e.getMessage(), e);
			throw new ConnectionLostException(e);
		}
		super.loop();
	}

	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + rxPin + ", tx=" + txPin + ", baud=" + baud + ", parity=" + parity + ", stopbits=" + stopbits;
	}

}
