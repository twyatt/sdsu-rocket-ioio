package edu.sdsu.rocket.control.network;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.AuthenticationRequest;
import edu.sdsu.rocket.Network.LaunchRequest;
import edu.sdsu.rocket.Network.LoggingRequest;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.logging.KryoNetLog;

public class RemoteCommandController {

	// FIXME replace and kryo debug JARs with non-debug versions
	
	private static final String AUTHENTICATION_KEY = "gimme$";

	private Server server = new Server() {
		protected Connection newConnection() {
			return new RemoteCommandConnection();
		};
	};
	
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
				App.log.i(App.TAG, "Established connection with " + connection.getRemoteAddressTCP() + ", ID: " + connection.getID() + ".");
			}
			
			@Override
			public void received(Connection connection, Object object) {
//				App.log.i(App.TAG, "Received object from " + connection.getID());
				
				if (object instanceof LoggingRequest) {
					onLoggingRequest(connection, (LoggingRequest)object);
				}
				
				if (object instanceof AuthenticationRequest) {
					onAuthenticationRequest(connection, (AuthenticationRequest)object);
				}
				
				if (object instanceof LaunchRequest) {
					onLaunchRequest(connection, (LaunchRequest)object);
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				if (App.log instanceof KryoNetLog) {
					((KryoNetLog)App.log).removeConnection(connection);
				}
				
				App.log.i(App.TAG, "Connection closed for " + connection.getRemoteAddressTCP() + ".");
			}
		});
		
		App.log.i(App.TAG, "Listening on TCP port " + tcpPort + ", UDP port " + udpPort + ".");
	}
	
	protected void onAuthenticationRequest(Connection connection, AuthenticationRequest authentication) {
		if (connection instanceof RemoteCommandConnection) {
			if (AUTHENTICATION_KEY.equals(authentication.key)) {
				((RemoteCommandConnection)connection).isAuthenticated = true;
				App.log.i(App.TAG, "Successfully authenticated " + connection.getRemoteAddressTCP() + ".");
			} else {
				App.log.i(App.TAG, "Failed to authenticate " + connection.getRemoteAddressTCP() + ".");
			}
		}
	}

	private void onLoggingRequest(Connection connection, LoggingRequest logging) {
		if (logging.enable) {
			if (App.log == null || !(App.log instanceof KryoNetLog)) {
				App.log = new KryoNetLog();
			}
			((KryoNetLog)App.log).addConnection(connection);
			
			App.log.i(App.TAG, "Logging enabled for " + connection.getRemoteAddressTCP() + ".");
		} else {
			if (App.log != null && App.log instanceof KryoNetLog) {
				((KryoNetLog)App.log).removeConnection(connection);
			}
			
			App.log.i(App.TAG, "Logging disabled for " + connection.getRemoteAddressTCP() + ".");
		}
	}
	
	protected void onLaunchRequest(Connection connection, LaunchRequest object) {
		if (connection instanceof RemoteCommandConnection) {
			if (((RemoteCommandConnection)connection).isAuthenticated) {
				App.log.i(App.TAG, "Initiating launch request from " + connection.getRemoteAddressTCP() + ".");
			} else {
				App.log.i(App.TAG, "Ignoring launch request from " + connection.getRemoteAddressTCP() + ".");
			}
		}
	}

	public class RemoteCommandConnection extends Connection {
		public boolean isAuthenticated;
	}
	
}
