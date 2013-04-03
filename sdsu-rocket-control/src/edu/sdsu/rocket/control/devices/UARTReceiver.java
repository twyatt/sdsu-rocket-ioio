package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import edu.sdsu.rocket.control.App;

public class UARTReceiver implements Device {
	
	public interface UARTReceiverListener {
		public void onUARTMessage(String response);
	}
	
	private UARTReceiverListener listener;

	private Uart rxUart;
	private InputStream in;
	private int rxPin;
	
	public UARTReceiver(int rxPin) {
		this.rxPin = rxPin;
	}
	
	public void setListener(UARTReceiverListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		int baud = 38400;
		Parity parity = Parity.NONE;
		StopBits stopbits = StopBits.ONE;
		
		rxUart = ioio.openUart(
			rxPin /* RX pin */,
			IOIO.INVALID_PIN /* TX pin */,
			baud,
			parity,
			stopbits
		);
		
		in = rxUart.getInputStream();
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		byte[] buffer = new byte[32];
		
		try {
			App.log.i(App.TAG, "uart receiver read");
			in.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "uart receiver read io exception");
			Thread.sleep(1000);
			return;
		}
		
		try {
			String message = new String(buffer, "US-ASCII");
//			App.log.i(App.TAG, "uart receiver message=" + message);
			
			if (listener != null) {
				listener.onUARTMessage(message);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "uart receiver unsupported encoding");
			Thread.sleep(1000);
			return;
		}
	}

	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + rxPin;
	}

}
