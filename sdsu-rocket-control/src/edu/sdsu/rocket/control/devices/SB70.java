package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketInputStream;
import edu.sdsu.rocket.io.PacketOutputStream;

public class SB70 implements Device {
	
	private static final byte[] START_BYTES = { (byte) 0xF0, (byte) 0x0D };
	private static final int MAX_DATA_LENGTH = 1024000;
	
	private Uart uart;
	private int rxPin;
	private int txPin;
	private int baud;
	private Parity parity;
	private StopBits stopbits;
	
	ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<Packet>();

	private PacketInputStream in;
	private PacketOutputStream out;
	
	public SB70(int rxPin, int txPin, int baud, Parity parity, StopBits stopbits) {
		this.rxPin = rxPin;
		this.txPin = txPin;
		this.baud = baud;
		this.parity = parity;
		this.stopbits = stopbits;
	}
	
	public void send(Packet packet) {
		send(packet.messageId, packet.data);
	}
	
	public void send(byte id, byte data) {
		send(id, new byte[] { data });
	}
	
	public void send(byte id, byte[] data) {
		try {
			out.writePacket(id, data);
		} catch (IOException e) {
			// TODO flash IOIO status LED to indicate error
//			App.log.e(App.TAG, "Failed to write packet.", e);
		}
	}
	
	public Packet get() {
		return queue.poll();
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		uart = ioio.openUart(rxPin, txPin, baud, parity, stopbits);
		
		in = new PacketInputStream(uart.getInputStream(), START_BYTES, MAX_DATA_LENGTH);
		out = new PacketOutputStream(uart.getOutputStream(), START_BYTES);
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		try {
			Packet packet;
			while ((packet = in.readPacket()) != null)
				queue.add(packet);
		} catch (IOException e) {
			// TODO flash IOIO status LED to indicate error
			App.log.e(App.TAG, "Failed to read packet.", e);
			return;
		}
	}

	@Override
	public void disconnected() {
		try {
			in.close();
			out.close();
		} catch (IOException e) {
			App.log.e(App.TAG, "Failed to close streams.", e);
		}
	}

	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + rxPin + ", tx=" + txPin + ", baud=" + baud + ", parity=" + parity + ", stopbits=" + stopbits;
	}

}
