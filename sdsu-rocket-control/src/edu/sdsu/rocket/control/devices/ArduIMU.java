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
import edu.sdsu.rocket.control.Debug;

/**
 * Arduino based IMU
 * 
 * MPU-6000 Accelerometer
 * HMC5883L Magnetometer (I2C)
 */
public class ArduIMU implements Device {

	private ArduIMUListener listener;
	
	private Uart uart;
	private int rxPin;

	private InputStream in;

	public interface ArduIMUListener {
		public void onArduIMUValues(String values);
	}
	
	public void setListener(ArduIMUListener listener) {
		this.listener = listener;
	}
	
	public ArduIMU(int rxPin) {
		this.rxPin = rxPin;
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		uart = ioio.openUart(rxPin /* RX pin */, IOIO.INVALID_PIN /* TX pin */, 38400 /* baud */, Parity.NONE /* parity */, StopBits.ONE /* stopbits */);
		in = uart.getInputStream();
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		App.log.i(App.TAG, "starting uart read");
		byte[] buffer = new byte[16];
		int read;
		
		try {
			in.read(buffer);
//			while ((read = in.read()) != -1) {
////				int s = ((read&0xff)<<24)+((read&0xff00)<<8)+((read&0xff0000)>>8)+((read>>24)&0xff);
////				App.log.i(App.TAG, "in=" + s);
//				App.log.i(App.TAG, "in=" + read);
//			}
		} catch (IOException e1) {
			App.log.i(App.TAG, "uart read failed");
			e1.printStackTrace();
			return;
		}
		App.log.i(App.TAG, "debug=" + Debug.bytesToHex(buffer));
		
//		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//		StringBuilder builder = new StringBuilder();
//		String line;
//		
//		try {
//			while ((line = reader.readLine()) != null) {
//			    builder.append(line);
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			App.log.i(App.TAG, "uart string build failed");
//			e.printStackTrace();
//			return;
//		}
		
//		if (listener != null) {
//			listener.onArduIMUValues(builder.toString());
//		}
		
		if (listener != null) {
			String values;
			try {
				values = new String(buffer, "US-ASCII");
			} catch (UnsupportedEncodingException e) {
				App.log.i(App.TAG, "unsupported encoding");
				e.printStackTrace();
				return;
			}
			
			listener.onArduIMUValues(values);
		}
		
		Thread.sleep(1000);
	}

	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + rxPin;
	}

}
