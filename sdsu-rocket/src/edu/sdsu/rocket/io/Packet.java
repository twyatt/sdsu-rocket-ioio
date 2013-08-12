package edu.sdsu.rocket.io;

public class Packet {
	
	public static final byte[] START_BYTES = { (byte) 0xF0, (byte) 0x0D };
	
	/*
	 * Requests
	 */
	
	public static final byte IDENT_REQUEST           = (byte) 0x01;
	public static final byte SENSOR_REQUEST          = (byte) 0x02;
	public static final byte VALVE_REQUEST           = (byte) 0x03;
	public static final byte IGNITE_REQUEST          = (byte) 0x04;
	public static final byte LAUNCH_REQUEST          = (byte) 0x05;
	public static final byte ABORT_REQUEST           = (byte) 0x06;
	public static final byte DATA_COLLECTION_REQUEST = (byte) 0x07;
	public static final byte IOIO_REQUEST            = (byte) 0xFF;
	
	public static final byte VALVE_REQUEST_ETHANOL = (byte) 0x01;
	public static final byte VALVE_REQUEST_LOX     = (byte) 0x02;
	
	public static final byte VALVE_REQUEST_CLOSE = (byte) 0x00;
	public static final byte VALVE_REQUEST_OPEN  = (byte) 0xFF;
	
	public static final byte DATA_COLLECTION_REQUEST_OFF   = (byte) 0x00;
	public static final byte DATA_COLLECTION_REQUEST_ON    = (byte) 0xFF;
	public static final byte DATA_COLLECTION_REQUEST_RESET = (byte) 0x80;
	
	public static final byte IOIO_REQUEST_DISCONNECT = (byte) 0x00;
	public static final byte IOIO_REQUEST_SOFT_RESET = (byte) 0x80;
	public static final byte IOIO_REQUEST_HARD_RESET = (byte) 0xFF;
	
	/*
	 * Responses
	 */
	
	public static final byte IDENT_RESPONSE           = (byte) 0x80;
	public static final byte SENSOR_RESPONSE          = (byte) 0x81;
	
	public static final byte DATA_COLLECTION_RESPONSE = (byte) 0x82;
	public static final byte DATA_COLLECTION_RESPONSE_OFF = (byte) 0x00;
	public static final byte DATA_COLLECTION_RESPONSE_ON  = (byte) 0xFF;
	
	
	public static final byte LOG_MESSAGE = (byte) 0xFF;

	
	
	public byte messageId;
	public byte[] data;
	
	public Packet(byte messageId, byte[] data) {
		this.messageId = messageId;
		this.data = data;
	}
	
	@Override
	public String toString() {
		return super.toString() + ": id=" + messageId + ", data.length=" + data.length;
	}
	
}
