package edu.sdsu.rocket.io;

public class Packet {
	
	public static final byte[] START_BYTES = { (byte) 0xF0, (byte) 0x0D };
	
	
	public static final byte IDENT_REQUEST = (byte) 0x01;
	public static final byte SENSOR_REQUEST = (byte) 0x02;
	
	public static final byte IDENT_RESPONSE = (byte) 0x03;
	public static final byte SENSOR_RESPONSE = (byte) 0x04;
	
	// | MESSAGE (String) |
	public static final byte LOG_MESSAGE              = (byte) 0x01;
	
	// | SETTING (byte) | ARGUMENT(s) (varies) |
	public static final byte SETTING_REQUEST          = (byte) 0x02;
	
	// | COMMAND (byte) | ARGUMENT(s) (varies) |
	public static final byte COMMAND_REQUEST          = (byte) 0x03;
	
	// | COMMAND (byte) | STATUS (byte) |
	public static final byte COMMAND_RESPONSE         = (byte) 0x04;
	
	// | DATA (byte[]) |
	public static final byte STATUS_DATA              = (byte) 0x05;
	
	// | DATA (byte[]) |
	public static final byte SENSOR_DATA              = (byte) 0x06;
	
	
	public static final byte STATE_OFF   = (byte) 0x00;
	public static final byte STATE_ON    = (byte) 0x01;
	public static final byte STATE_RESET = (byte) 0x02;
	
	public static final byte STATUS_FAILURE = (byte) 0x00;
	public static final byte STATUS_SUCCESS = (byte) 0x01;
	
	public static final byte SETTING_SENSOR_RATE = (byte) 0x01;
	
	public static final byte COMMAND_ABORT             = (byte) 0x01;
	public static final byte COMMAND_LAUNCH            = (byte) 0x02;
	public static final byte COMMAND_DATA_LOGGER_ON    = (byte) 0x03;
	public static final byte COMMAND_DATA_LOGGER_OFF   = (byte) 0x04;
	public static final byte COMMAND_DATA_LOGGER_RESET = (byte) 0x05;

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
