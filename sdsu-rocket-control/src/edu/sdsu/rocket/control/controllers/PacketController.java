package edu.sdsu.rocket.control.controllers;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketWriter;

public class PacketController implements PacketListener {
	
	private static final int BUFFER_SIZE = 1024;
	
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private PacketWriter writer;
	
	public PacketController(PacketWriter writer) {
		this.writer = writer;
	}

	/*
	 * Requests
	 */
	
	private void onIdentRequest(Packet request) {
		sendIdent(App.TAG);
	}
	
	private void onSensorRequest(Packet request) {
		sendSensorData();
	}
	
	private void onDataCollectionRequest(Packet request) {
		if (request.data == null || request.data.length == 0) {
			App.stats.network.packetsDropped.incrementAndGet();
			App.log.e(App.TAG, "Invalid data collection request.");
			return;
		}
		
		byte state = request.data[0];
		
		switch (state) {
		case Packet.DATA_COLLECTION_REQUEST_OFF:
			App.data.disable();
			break;
		case Packet.DATA_COLLECTION_REQUEST_ON:
			App.data.enable();
			break;
		case Packet.DATA_COLLECTION_REQUEST_RESET:
			App.data.reset();
			break;
		}
		
		sendDataCollectionResponse(App.data.isEnabled());
	}
	
	private void onIgniteRequest(Packet packet) {
		Rocket rocket = App.rocketController.getRocket();
		rocket.ignitor.ignite();
	}
	
	/*
	 * Responses
	 */
	
	public void sendIdent(String ident) {
		send(Packet.IDENT_RESPONSE, ident.getBytes());
	}
	
	public void sendSensorData() {
		Rocket rocket = App.rocketController.getRocket();
		buffer.clear();
		
//		buffer.putFloat(rocket.tankPressureLOX.getVoltage());
//		buffer.putFloat(rocket.tankPressureEngine.getVoltage());
//		buffer.putFloat(rocket.tankPressureEthanol.getVoltage());
//		buffer.putFloat(rocket.barometer.pressure);
//		buffer.putFloat(rocket.barometer.temperature);
		
		buffer.put((byte) (rocket.ignitor.isActive() ? 1 : 0));
		buffer.put((byte) (rocket.breakWire.isBroken() ? 1 : 0));
		
		buffer.putFloat(rocket.accelerometer.getMultiplier());
		buffer.putInt(rocket.accelerometer.getX());
		buffer.putInt(rocket.accelerometer.getY());
		buffer.putInt(rocket.accelerometer.getZ());
		
		buffer.putFloat(rocket.internalAccelerometer.getX());
		buffer.putFloat(rocket.internalAccelerometer.getY());
		buffer.putFloat(rocket.internalAccelerometer.getZ());
		
		buffer.flip();
		byte[] data = new byte[buffer.limit()];
		buffer.get(data);
		
		send(Packet.SENSOR_RESPONSE, data);
	}

	private void sendDataCollectionResponse(boolean on) {
		 byte data = on ? Packet.DATA_COLLECTION_RESPONSE_ON : Packet.DATA_COLLECTION_RESPONSE_OFF;
		 send(Packet.DATA_COLLECTION_RESPONSE, new byte[] { data });
	}

	protected void onCommandRequest(Packet packet) {
//		objectiveController.command(command.command);
//		
//		if (Network.ABORT_COMMAND.equals(command.command)) {
//			/*
//			 * Allow the abort command to close fuel valves and open tank
//			 * vents no matter what objective is currently active.
//			 */
//			if (App.rocket != null) {
//				App.log.i(App.TAG, "Closing fuel valves and opening tank vents!");
//				App.rocket.servoLOX.open();
//				App.rocket.servoEthanol.open();
//				App.rocket.fuelValve.deactivate();
//			}
//			
//			App.objective.set(Network.LAUNCH_OBJECTIVE);
//		}
	}
	
	public void send(Packet packet) {
		send(packet.messageId, packet.data);
	}
	
	public void send(byte id, byte[] data) {
		try {
			writer.writePacket(id, data);
		} catch (IOException e) {
			App.stats.network.packetsDropped.incrementAndGet();
			App.log.e(App.TAG, "Packet controller failed to send packet.", e);
		}
	}
	
	/*
	 * PacketListener interface methods.
	 */
	
	public void onPacketReceived(Packet packet) {
		switch (packet.messageId) {
		case Packet.IDENT_REQUEST:
			onIdentRequest(packet);
			break;
		case Packet.SENSOR_REQUEST:
			onSensorRequest(packet);
			break;
		case Packet.IGNITE_REQUEST:
			onIgniteRequest(packet);
			break;
		case Packet.DATA_COLLECTION_REQUEST:
			onDataCollectionRequest(packet);
			break;
		default:
			App.log.e(App.TAG, "Unknown packet ID: " + packet.messageId);
		}
	}

}
