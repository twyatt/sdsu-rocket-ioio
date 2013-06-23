package edu.sdsu.rocket.control.models;

public class Statistics {

	public final NetworkStatistics network = new NetworkStatistics();
	
	public class NetworkStatistics {
		
		public long bytesReceived;
		public long bytesSent;
		public long packetsReceived;
		public long packetsSent;
		public long packetsDropped;
		
	}
	
}
