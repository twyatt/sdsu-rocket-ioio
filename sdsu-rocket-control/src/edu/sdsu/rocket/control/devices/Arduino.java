package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.sdsu.rocket.control.App;

public class Arduino extends DeviceAdapter {
	
	public interface ArduinoListener {
		public void onRequest(char request);
	}
	
	private ArduinoListener listener;
	
	private Uart uart;
	private int rxPin;
	private int txPin;
	private int baud;
	private Parity parity;
	private StopBits stopbits;
	
	private IOIO ioio;
	private DataInputStream in;
	private DataOutputStream out;
	
	public Arduino(int rxPin, int txPin, int baud, Parity parity, StopBits stopbits) {
		this.rxPin = rxPin;
		this.txPin = txPin;
		this.baud = baud;
		this.parity = parity;
		this.stopbits = stopbits;
		setSleep(0L);
	}
	
	public Arduino setListener(ArduinoListener listener) {
		this.listener = listener;
		return this;
	}
	
	public DataOutputStream getOutputStream() {
		return out;
	}
	
	/*
	 * Device interface methods.
	 */

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		this.ioio = ioio;
		uart = ioio.openUart(rxPin, txPin, baud, parity, stopbits);
		
		in = new DataInputStream(uart.getInputStream());
		out = new DataOutputStream(uart.getOutputStream());
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		try {
			char character = in.readChar();
			if (listener != null) {
				listener.onRequest(character);
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
