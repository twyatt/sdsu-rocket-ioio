package edu.sdsu.rocket.control.network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.LoggingRequest;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.logging.KryoNetLog;

public class RemoteCommandController {

	private Server server = new Server();
	
	private int tcpPort;
	private int udpPort;
	
	public RemoteCommandController(int tcpPort, int udpPort) {
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
	}
	
	public int getTcpPort() {
		return tcpPort;
	}
	
	public int getUdpPort() {
		return udpPort;
	}
	
	public void start() {
		Network.register(server);
		
		server.start();
		
		try {
			server.bind(tcpPort, udpPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		server.addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				App.log.i(App.TAG, "Connected: " + connection.getID());
			}
			
			@Override
			public void received(Connection connection, Object object) {
//				App.log.i(App.TAG, "Received object from " + connection.getID());
				
				if (object instanceof LoggingRequest) {
					LoggingRequest logging = (LoggingRequest)object;
					
					if (logging.enable) {
						if (App.log == null || !(App.log instanceof KryoNetLog)) {
							App.log = new KryoNetLog();
						}
						((KryoNetLog)App.log).addConnection(connection);
						
						App.log.i(App.TAG, "Logging to " + connection.getRemoteAddressTCP() + " enabled.");
					} else {
						if (App.log != null && App.log instanceof KryoNetLog) {
							((KryoNetLog)App.log).removeConnection(connection);
						}
						
						App.log.i(App.TAG, "Logging to " + connection.getRemoteAddressTCP() + " disabled.");
					}
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				if (App.log instanceof KryoNetLog) {
					((KryoNetLog)App.log).removeConnection(connection);
				}
				
				App.log.i(App.TAG, "Disconnected: " + connection.getID());
			}
		});
		
		App.log.i(App.TAG, "Listening on TCP port " + tcpPort + ", UDP port " + udpPort + ".");
	}
	
}
