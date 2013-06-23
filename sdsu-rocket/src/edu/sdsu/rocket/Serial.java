package edu.sdsu.rocket;

public class Serial {
	
	public static final byte[] START_BYTES = { (byte) 0xF0, (byte) 0x0D };
	
	// | MESSAGE (String) |
	public static final byte LOG_MESSAGE              = (byte) 0x01;
	
	// | STATE (byte) |
	public static final byte DATA_COLLECTION_REQUEST  = (byte) 0x02;
	
	// | STATE (byte) |
	public static final byte DATA_COLLECTION_RESPONSE = (byte) 0x03;
	
	// | OBJECTIVE (byte) |
	public static final byte SET_OBJECTIVE_REQUEST    = (byte) 0x04;
	
	// | OBJECTIVE (byte) | STATUS (byte) |
	public static final byte SET_OBJECTIVE_RESPONSE   = (byte) 0x05;
	
	// | COMMAND (byte) |
	public static final byte COMMAND_REQUEST          = (byte) 0x06;
	
	// | COMMAND (byte) | STATUS (byte) |
	public static final byte COMMAND_RESPONSE         = (byte) 0x07;
	
	// | FREQUENCY (float) |
	public static final byte SENSOR_REQUEST           = (byte) 0x08;
	
	// | FREQUENCY (float) |
	public static final byte SENSOR_RESPONSE          = (byte) 0x09;
	
	
	public static final byte STATE_OFF   = (byte) 0x00;
	public static final byte STATE_ON    = (byte) 0x01;
	public static final byte STATE_RESET = (byte) 0x02;
	
	public static final byte STATUS_FAILURE = (byte) 0x00;
	public static final byte STATUS_SUCCESS = (byte) 0x01;
	
	public static final byte COMMAND_ABORT  = (byte) 0x01;
	public static final byte COMMAND_LAUNCH = (byte) 0x02;
	
	
	public static final byte OBJECTIVE_LAUNCH     = (byte) 0x01;
	public static final byte OBJECTIVE_FILL_TANKS = (byte) 0x02;
	public static final byte OBJECTIVE_FLIGHT     = (byte) 0x03;
	
	public static final String OBJECTIVE_NAME_LAUNCH     = "launch";
	public static final String OBJECTIVE_NAME_FILL_TANKS = "tanks";
	public static final String OBJECTIVE_NAME_FLIGHT     = "flight";
	
}
