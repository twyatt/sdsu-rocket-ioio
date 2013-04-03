package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import edu.sdsu.rocket.control.App;

public class UARTPing implements Device {
	
	private Uart txUart;
	private OutputStream out;
	private int txPin;
	
	private long sleep;
	
	public UARTPing(int txPin, long sleep) {
		this.txPin = txPin;
		this.sleep = sleep;
	}

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		int baud = 38400;
		Parity parity = Parity.NONE;
		StopBits stopbits = StopBits.ONE;
		
		txUart = ioio.openUart(
			IOIO.INVALID_PIN /* RX pin */,
			txPin /* TX pin */,
			baud,
			parity,
			stopbits
		);
		
		out = txUart.getOutputStream();
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		try {
			String string = "ping\0";
			App.log.i(App.TAG, "uartping write=" + string);
			byte[] bytes = string.getBytes("US-ASCII");
			out.write(bytes);
			out.flush();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			App.log.i(App.TAG, "uartping unsupported encoding");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			App.log.i(App.TAG, "uartping write io exception");
			e.printStackTrace();
			return;
		}
		
		Thread.sleep(sleep);
	}

	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": tx=" + txPin;
	}

}
