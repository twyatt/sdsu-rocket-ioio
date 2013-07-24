package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;

import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketInputStream;
import edu.sdsu.rocket.io.PacketInputStream.PacketException;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketOutputStream;
import edu.sdsu.rocket.io.PacketWriter;

public class SB70 extends DeviceAdapter implements PacketWriter {
	
	private static final int MAX_DATA_LENGTH = 1024000;
	
	private PacketListener listener;
	
	private Uart uart;
	private int rxPin;
	private int txPin;
	private int baud;
	private Parity parity;
	private StopBits stopbits;
	
	private IOIO ioio;
	private PacketInputStream in;
	private PacketOutputStream out;
	
	public SB70(int rxPin, int txPin, int baud, Parity parity, StopBits stopbits) {
		this.rxPin = rxPin;
		this.txPin = txPin;
		this.baud = baud;
		this.parity = parity;
		this.stopbits = stopbits;
		setSleep(0L);
	}
	
	public SB70 setListener(PacketListener listener) {
		this.listener = listener;
		return this;
	}
	
	public void write(Packet packet) throws IOException {
		writePacket(packet.messageId, packet.data);
	}
	
	public void write(byte id, byte data) throws IOException {
		writePacket(id, new byte[] { data });
	}
	
	public void writePacket(byte id, byte[] data) throws IOException {
		try {
			ioio.beginBatch();
			out.writePacket(id, data);
			ioio.endBatch();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Device interface methods.
	 */
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		this.ioio = ioio;
		uart = ioio.openUart(rxPin, txPin, baud, parity, stopbits);
		
		in = new PacketInputStream(uart.getInputStream(), Packet.START_BYTES, MAX_DATA_LENGTH);
		out = new PacketOutputStream(uart.getOutputStream(), Packet.START_BYTES);
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		try {
//			System.out.println("avail = " + in.available());
			Packet packet = in.readPacket();
			if (listener != null)
				listener.onPacketReceived(packet);
		} catch (PacketException pe) {
			System.out.println("dropped packet");
			// TODO increment dropped packet counter
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Thread.sleep(500L);
			e.printStackTrace();
		}
		super.loop();
	}
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": rx=" + rxPin + ", tx=" + txPin + ", baud=" + baud + ", parity=" + parity + ", stopbits=" + stopbits;
	}

}
