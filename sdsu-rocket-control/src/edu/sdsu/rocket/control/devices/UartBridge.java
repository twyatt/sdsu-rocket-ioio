package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UartBridge implements Device {

	private static final int BUFFER_SIZE = 1024; // bytes
	public static final byte[] SYNC_BYTES = { (byte) 0xF0, (byte) 0x0D };
	public static final int DATA_LENGTH_LIMIT = 1024000;
	
	public enum Mode {
		SYNC_WAIT,
		READ_LENGTH,
		READ_DATA,
	}
	
	private Mode mode = Mode.SYNC_WAIT;
	
	private Uart uart;
	private int rxPin;
	private int txPin;
	private int baud;
	private Parity parity;
	private StopBits stopbits;
	
	protected static final byte[] readBuffer = new byte[BUFFER_SIZE];
	ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	
	ByteBuffer syncBuffer = ByteBuffer.allocate(SYNC_BYTES.length);
	ByteBuffer lengthBuffer = ByteBuffer.allocate(4 /* int is 4-bytes */).order(ByteOrder.BIG_ENDIAN); // TODO byte order?
	ByteBuffer dataBuffer = ByteBuffer.allocate(8192);
	
	protected InputStream in;
	protected OutputStream out;

	public UartBridge(int rxPin, int txPin, int baud, Parity parity, StopBits stopbits) {
		this.rxPin = rxPin;
		this.txPin = txPin;
		this.baud = baud;
		this.parity = parity;
		this.stopbits = stopbits;
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		uart = ioio.openUart(rxPin, txPin, baud, parity, stopbits);
		in = uart.getInputStream();
		out = uart.getOutputStream();
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		int read = 0;
		
		try {
			read = in.read(readBuffer);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
//		System.out.println("Read " + read + " bytes.");
		
		if (read > 0) {
			buffer.put(readBuffer, 0, read);
			parseBuffer();
		}
	}
	
	/**
	 * Communication protocol:
	 * SYNC_BYTES | length (int, 4-byte) | data
	 */
	private void parseBuffer() {
//		System.out.println("position = " + buffer.position());
		
		for (int i = 0; i < buffer.position(); i++) {
			if (Mode.SYNC_WAIT.equals(mode)) {
				if (buffer.get(i) == SYNC_BYTES[syncBuffer.position()]) {
					// another part of sync bytes
					syncBuffer.put(buffer.get(i));
				} else {
					// not part of sync bytes
					syncBuffer.rewind();
				}
				
				if (syncBuffer.position() == syncBuffer.capacity()) {
					System.out.println("SYNC");
					// sync buffer is full
					syncBuffer.rewind();
					mode = Mode.READ_LENGTH;
				}
			} else if (Mode.READ_LENGTH.equals(mode)) {
				lengthBuffer.put(buffer.get(i));
				
				if (lengthBuffer.position() == lengthBuffer.capacity()) {
					// length buffer is full
					lengthBuffer.rewind();
					int length = lengthBuffer.getInt(0); // TODO unsigned?
					
//					for (byte b : lengthBuffer.array()) {
//						System.out.println("b = " + b);
//					}
					
					System.out.println("length = " + length);
					
					if (length > 0 && length <= DATA_LENGTH_LIMIT) {
						if (length > dataBuffer.capacity()) {
							dataBuffer = ByteBuffer.allocate(length); // TODO byte order?
						}
						
						dataBuffer.limit(length);
						mode = Mode.READ_DATA;
					} else {
						System.err.println("Invalid data length, discarding.");
						mode = Mode.SYNC_WAIT;
					}
				}
			} else if (Mode.READ_DATA.equals(mode)) {
				int remaining = dataBuffer.remaining();
				
				if (remaining > buffer.position() - i) {
					/*
					 * Remaining data bytes to read exceeds bytes available in
					 * buffer, so we'll just read all available bytes in buffer.
					 */
					remaining = buffer.position() - i;
				}
				
				dataBuffer.put(buffer.array(), i, remaining);
				
				if (dataBuffer.remaining() == 0) {
					byte[] data = new byte[dataBuffer.limit()];
					
					dataBuffer.rewind();
					dataBuffer.get(data);
					dataBuffer.clear();
					
					mode = Mode.SYNC_WAIT;
					onReceivedData(data);
				}
			}
		}
		
		buffer.rewind();
	}

	private void onReceivedData(byte[] data) {
		System.out.println("Received " + data.length + " bytes of data.");
		for (byte b : data) {
			System.out.println("b = " + b);
		}
	}

	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + rxPin + ", tx=" + txPin + ", baud=" + baud + ", parity=" + parity + ", stopbits=" + stopbits;
	}

}
