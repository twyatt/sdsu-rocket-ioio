package edu.sdsu.rocket.control.network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.AuthenticationRequest;
import edu.sdsu.rocket.Network.AuthenticationResponse;
import edu.sdsu.rocket.Network.CommandRequest;
import edu.sdsu.rocket.Network.LoggingRequest;
import edu.sdsu.rocket.Network.SetObjectiveRequest;
import edu.sdsu.rocket.Network.SetObjectiveResponse;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.ObjectiveController;
import edu.sdsu.rocket.control.logging.KryoNetLog;

public class RemoteCommandController {

	// FIXME replace kryo debug JARs with non-debug versions
	
	public Server server = new Server() {
		protected Connection newConnection() {
			return new RemoteCommandConnection();
		};
	};
	
	private int tcpPort;
	private int udpPort;

	private ObjectiveController objectiveController;
	
	public RemoteCommandController(int tcpPort, int udpPort, ObjectiveController objectiveController) {
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.objectiveController = objectiveController;
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
				
				if (object instanceof SetObjectiveRequest) {
					onSetObjectiveRequest(connection, (SetObjectiveRequest)object);
				}
				
				if (object instanceof CommandRequest) {
					onCommandRequest(connection, (CommandRequest)object);
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				if (App.log instanceof KryoNetLog) {
					((KryoNetLog)App.log).removeConnection(connection);
				}
				
				App.log.i(App.TAG, "Connection closed for ID " + connection.getID() + ".");
			}
		});
		
		App.log.i(App.TAG, "Listening on TCP port " + tcpPort + ", UDP port " + udpPort + ".");
	}
	
	protected void onCommandRequest(Connection connection, CommandRequest command) {
		if (isAuthenticated(connection)) {
			objectiveController.command(command.command);
		} else {
			App.log.i(App.TAG, "Ignoring '" + command.command + "' command from " + connection.getRemoteAddressTCP() + ".");
		}
	}

	protected void onAuthenticationRequest(Connection connection, AuthenticationRequest authentication) {
		if (connection instanceof RemoteCommandConnection) {
			AuthenticationResponse response = new AuthenticationResponse();
			
			if (Network.AUTHENTICATION_KEY.equals(authentication.key)) {
				((RemoteCommandConnection)connection).isAuthenticated = true;
				response.success = true;
				App.log.i(App.TAG, "Successfully authenticated " + connection.getRemoteAddressTCP() + ".");
			} else {
				response.success = false;
				App.log.i(App.TAG, "Failed to authenticate " + connection.getRemoteAddressTCP() + ".");
			}
			
			connection.sendTCP(response);
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
	
	/**
	 * Sets the active objective.
	 * Requires that the connection be authenticated.
	 * 
	 * @param connection
	 * @param setObjective
	 */
	protected void onSetObjectiveRequest(Connection connection, SetObjectiveRequest setObjective) {
		SetObjectiveResponse response = new SetObjectiveResponse();
		
		if (isAuthenticated(connection)) {
			boolean success = objectiveController.set(setObjective.name);
			if (!success) {
				App.log.i(App.TAG, "Failed to set to " + setObjective.name + " objective request from " + connection.getRemoteAddressTCP() + ".");
			}
			response.success = success;
		} else {
			App.log.i(App.TAG, "Ignoring set objective request from " + connection.getRemoteAddressTCP() + ".");
			response.success = false;
		}
		
		connection.sendTCP(response);
	}

	public static boolean isAuthenticated(Connection connection) {
		if (connection instanceof RemoteCommandConnection) {
			return ((RemoteCommandConnection)connection).isAuthenticated;
		}
		
		return false;
	}
	
	public class RemoteCommandConnection extends Connection {
		public boolean isAuthenticated;
	}
	
}
