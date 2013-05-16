package edu.sdsu.rocket;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
	
	public static final int LOG_LEVEL_TRACE = 1;
	public static final int LOG_LEVEL_DEBUG = 2;
	public static final int LOG_LEVEL_INFO  = 3;
	public static final int LOG_LEVEL_WARN  = 4;
	public static final int LOG_LEVEL_ERROR = 5;
	public static final int LOG_LEVEL_NONE  = 6;
	
	public static final int UDP_PORT = 47050;
	public static final int TCP_PORT = 47250;
	
	public static final String AUTHENTICATION_KEY = "a";
	
	public static final String LAUNCH_OBJECTIVE = "launch";
	public static final String LAUNCH_COMMAND = "launch";
	public static final String ABORT_COMMAND = "abort";
	
	public static final String FILL_TANKS_OBJECTIVE = "tanks";
	public static final String OPEN_LOX_TANK_COMMAND = "openlox";
	public static final String CYCLE_LOX_TANK_COMMAND = "cyclelox";
	public static final String CLOSE_LOX_TANK_COMMAND = "closelox";
	public static final String OPEN_ETHANOL_TANK_COMMAND = "openeth";
	public static final String CYCLE_ETHANOL_TANK_COMMAND = "cycleeth";
	public static final String CLOSE_ETHANOL_TANK_COMMAND = "closeeth";
	
	public static final String FLIGHT_OBJECTIVE = "flight";

	static public void register(EndPoint endPoint) {
		Kryo kyro = endPoint.getKryo();
		
		kyro.register(AuthenticationRequest.class);
		kyro.register(AuthenticationResponse.class);
		kyro.register(LoggingRequest.class);
		kyro.register(DataCollectionRequest.class);
		kyro.register(LogMessage.class);
		kyro.register(SetObjectiveRequest.class);
		kyro.register(SetObjectiveResponse.class);
		kyro.register(CommandRequest.class);
		kyro.register(SensorRequest.class);
		kyro.register(SensorResponse.class);
	}
	
	static public class AuthenticationRequest {
		public String key;
	}
	
	static public class AuthenticationResponse {
		public boolean success;
	}
	
	static public class LoggingRequest {
		public boolean enable;
	}
	
	static public class DataCollectionRequest {
		public boolean enable;
	}
	
	static public class LogMessage {
		public int level;
		public String tag;
		public String message;
	}
	
	static public class SetObjectiveRequest {
		public String name;
	}
	
	static public class SetObjectiveResponse {
		public boolean success;
	}
	
	static public class CommandRequest {
		public String command;
	}
	
	static public class SensorRequest {}
	
	static public class SensorResponse {
		public float loxTransducerVoltage;
		public float loxTransducerPressure;
		
		public float ethanolTransducerVoltage;
		public float ethanolTransducerPressure;
		
		public float engineTransducerVoltage;
		public float engineTransducerPressure;
		
		public boolean breakWireIsBroken;
		
		public int barometer1Pressure;
		public double barometer1Temperature;
		
		public float barometer2Pressure;
		public float barometer2Temperature;
	}
	
}
