package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import edu.sdsu.rocket.control.App;

/**
 * Arduino based IMU
 * 
 * MPU-6000 Accelerometer
 * HMC5883L Magnetometer (I2C)
 */
public class ArduIMU implements Device {

	private static final int BUFFER_SIZE = 256;
	private static final String VALUES_DELIMITER = "\n";
//	private static final String VALUES_DELIMITER = "!!!VER";

	private ArduIMUListener listener;
	
	private Uart uart;
	private int rxPin;

	private InputStream in;

	private String buffer = "";

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
//		App.log.i(App.TAG, "starting uart read");
		
		byte[] buffer = new byte[BUFFER_SIZE];
		
		try {
			in.read(buffer);
		} catch (IOException ioException) {
			App.log.i(App.TAG, "IMU uart read failed.");
			ioException.printStackTrace();
			Thread.sleep(1000);
			return; // TODO disable device instead
		}
		
		try {
			String text = new String(buffer, "US-ASCII");
			onReceivedText(text);
		} catch (UnsupportedEncodingException encodingException) {
			App.log.i(App.TAG, "IMU unsupported encoding.");
			encodingException.printStackTrace();
			Thread.sleep(1000);
			return; // TODO disable device instead
		}
		
//		App.log.i(App.TAG, "debug=" + Debug.bytesToHex(buffer));
		
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
	}

	private void onReceivedText(String text) {
		if (listener != null) {
			listener.onArduIMUValues(text);
		}
	}
	
//	private void onReceivedText(String text) {
//		buffer += text;
//		
//		if (buffer.contains(VALUES_DELIMITER)) {
//			String parse;
//			
//			if (buffer.endsWith(VALUES_DELIMITER)) {
//				parse = buffer;
//				buffer = "";
//			} else {
//				int lastIndexOf = buffer.lastIndexOf(VALUES_DELIMITER);
//				parse = buffer.substring(0, lastIndexOf);
//				buffer = buffer.substring(lastIndexOf + 1);
//			}
//			
//			StringTokenizer tokenizer = new StringTokenizer(parse, VALUES_DELIMITER);
//			while (tokenizer.hasMoreTokens()) {
//				parseValues(tokenizer.nextToken());
//			}
//		}
//	}

	private void parseValues(String values) {
		values = values.trim();
		
//		if (!values.startsWith("!!!")) { // not values
//			App.log.i(App.TAG, "not imu values: '" + values.substring(0, 3) + "'");
////			return;
//		}
//		
//		// strip trailing text that we don't use
//		if (values.endsWith("***")) {
//			App.log.i(App.TAG, "stripping imu ***");
//			values = values.substring(0, values.length() - 3);
//		}
//		if (values.contains("*$PGCMD")) {
//			App.log.i(App.TAG, "stripping imu cmd");
//			values = values.substring(0, values.indexOf("*$PGCMD"));
//		}
		
		if (listener != null) {
			listener.onArduIMUValues(values);
		}
	}

	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + rxPin;
	}

}
