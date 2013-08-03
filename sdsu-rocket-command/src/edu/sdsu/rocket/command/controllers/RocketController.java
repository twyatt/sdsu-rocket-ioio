package edu.sdsu.rocket.command.controllers;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.command.models.Rocket;
import edu.sdsu.rocket.helpers.Threaded;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketWriter;

public class RocketController extends Threaded implements PacketListener {
	
	public interface RocketControllerListener {
		public void onChange();
	}
	
	private RocketControllerListener listener;
	
	final private Rocket rocket;
	private PacketWriter writer;

	public RocketController(Rocket rocket) {
		this.rocket = rocket;
		setFrequency(1f /* Hz */);
	}
	
	public RocketController setListener(RocketControllerListener listener) {
		this.listener = listener;
		return this;
	}
	
	public RocketController setWriter(PacketWriter writer) {
		this.writer = writer;
		return this;
	}
	
	public void sendIdentRequest() throws IOException {
		writer.writePacket(Packet.IDENT_REQUEST, null);
	}
	
	private void onIdentResponse(Packet packet) {
		if (packet.data.length > 0) {
			rocket.ident = new String(packet.data);
			onChange();
		}
	}
	
	public void sendSensorRequest() throws IOException {
		writer.writePacket(Packet.SENSOR_REQUEST, null);
	}
	
	public void sendIgniteRequest() throws IOException {
		writer.writePacket(Packet.IGNITE_REQUEST, null);
	}
	
	private void onSensorResponse(Packet packet) {
//		rocket.waitingForSensorData.set(false);
		
		ByteBuffer buffer = ByteBuffer.wrap(packet.data);
		try {
			// for ADXL345 accelerometer
			rocket.accelerometer.multiplier = buffer.getFloat();
			rocket.accelerometer.x = buffer.getInt();
			rocket.accelerometer.y = buffer.getInt();
			rocket.accelerometer.z = buffer.getInt();
			
			// for phone's internal accelerometer
			rocket.internalAccelerometer.x = buffer.getFloat();
			rocket.internalAccelerometer.y = buffer.getFloat();
			rocket.internalAccelerometer.z = buffer.getFloat();
		} catch (BufferUnderflowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		onChange();
	}
	
	private void onChange() {
		if (listener != null) {
			listener.onChange();
		}
	}

	/*
	 * PacketListener interface methods.
	 */
	
	@Override
	public void onPacketReceived(Packet packet) {
		switch (packet.messageId) {
		case Packet.IDENT_RESPONSE:
			onIdentResponse(packet);
			break;
		case Packet.SENSOR_RESPONSE:
			onSensorResponse(packet);
			break;
		}
	}
	
	/*
	 * Threaded interface methods.
	 */

	@Override
	public void loop() {
//		if (!rocket.waitingForSensorData.get() || hasTimedOut) {
			try {
				sendSensorRequest();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		}
	}

	@Override
	public void interrupted() {
		// silently ignore
	}

}
