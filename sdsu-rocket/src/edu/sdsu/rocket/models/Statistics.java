package edu.sdsu.rocket.models;

import java.util.concurrent.atomic.AtomicLong;

public class Statistics {

	final public NetworkStatistics network = new NetworkStatistics();
	final public IOIOStatistics ioio       = new IOIOStatistics();
	
	public class IOIOStatistics {
		
		final public AtomicLong connects    = new AtomicLong();
		final public AtomicLong disconnects = new AtomicLong();
		final public AtomicLong errors      = new AtomicLong();
		
	}
	
	public class NetworkStatistics {
		
		final public AtomicLong bytesReceived   = new AtomicLong();
		final public AtomicLong bytesSent       = new AtomicLong();
		final public AtomicLong packetsReceived = new AtomicLong();
		final public AtomicLong packetsSent     = new AtomicLong();
		final public AtomicLong packetsDropped  = new AtomicLong();
		
	}
	
}
