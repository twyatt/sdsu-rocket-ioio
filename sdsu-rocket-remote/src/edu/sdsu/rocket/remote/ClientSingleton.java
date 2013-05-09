package edu.sdsu.rocket.remote;

import com.esotericsoftware.kryonet.Client;

import edu.sdsu.rocket.Network;

public class ClientSingleton extends Client {

	/*
	 * Singleton implementation.
	 * 
	 * Eager initialization
	 * http://en.wikipedia.org/wiki/Singleton_pattern
	 */
	private static final ClientSingleton instance = new ClientSingleton();

	private ClientSingleton() {
		super();
		Network.register(this);
		start();
	}

	public static ClientSingleton getInstance() {
		return instance;
	}

}
