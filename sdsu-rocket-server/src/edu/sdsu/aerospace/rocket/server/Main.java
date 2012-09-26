package edu.sdsu.aerospace.rocket.server;

public class Main {
	
	private static Server server;
	public final static int PORT = 12161;
	
	public static void main(String[] args) {
		server = new Server();
		server.listen(PORT);
	}
	
}
