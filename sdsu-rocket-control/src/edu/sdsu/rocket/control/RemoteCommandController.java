package edu.sdsu.rocket.control;

import java.net.InetAddress;
import java.util.StringTokenizer;

import edu.sdsu.rocket.control.logging.AndroidLog;
import edu.sdsu.rocket.control.logging.UDPLog;
import edu.sdsu.rocket.control.network.UDPServer;
import edu.sdsu.rocket.control.network.UDPServer.UDPServerListener;

public class RemoteCommandController implements UDPServerListener {

	private static final String COMMAND_DELIMITER = "\n";

	private static final String COMMAND_LAUNCH = "LAUNCH";
	private static final String COMMAND_ADMIN  = "ADMIN";
	private static final String COMMAND_UDP_LOG_ON  = "UDP_LOG_ON";
	private static final String COMMAND_UDP_LOG_OFF = "UDP_LOG_OFF";

	private static final int UDP_LOG_PORT = 10001;
	
	private UDPServer server;
	private String buffer = new String();
	private InetAddress administrator;
	
	public void listen(int port) {
		server = new UDPServer();
		server.listen(port);
		server.setListener(this);
		
		App.log.i(App.TAG, "Listening on port " + port + ".");
	}

	@Override
	public void onReceivedPacket(byte[] data, InetAddress inetAddress, int port) {
		String text = new String(data);
		buffer += text;
		
		if (buffer.contains(COMMAND_DELIMITER)) {
			String parse;
			
			if (buffer.endsWith(COMMAND_DELIMITER)) {
				parse = buffer;
				buffer = "";
			} else {
				int lastIndexOf = buffer.lastIndexOf(COMMAND_DELIMITER);
				parse = buffer.substring(0, lastIndexOf);
				buffer = buffer.substring(lastIndexOf + 1);
			}
			
			StringTokenizer tokenizer = new StringTokenizer(parse, COMMAND_DELIMITER);
			while (tokenizer.hasMoreTokens()) {
				onCommand(tokenizer.nextToken(), inetAddress);
			}
		}
	}

	private void onCommand(String command, InetAddress inetAddress) {
//		String arguments;
//		if (command.contains(" ")) {
//			arguments = command.substring(command.indexOf(" ") + 1);
//		} else {
//			arguments = "";
//		}
		
		System.out.println("command='" + command + "'");
		
		if (command.equalsIgnoreCase(COMMAND_LAUNCH)) {
			// FIXME implement
		} else if (command.equalsIgnoreCase(COMMAND_ADMIN)) {
			administrator = inetAddress;
		} else if (command.equalsIgnoreCase(COMMAND_UDP_LOG_ON)) {
			if (administrator != null) {
				App.log = new UDPLog(administrator, UDP_LOG_PORT);
			}
		} else if (command.equalsIgnoreCase(COMMAND_UDP_LOG_OFF)) {
			App.log = new AndroidLog();
		} else {
			// TODO log unrecognized command
		}
	}
	
}
