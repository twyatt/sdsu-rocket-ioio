package edu.sdsu.rocket.control.controllers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketWriter;

public class PacketController implements PacketListener {
	
	private static final int BUFFER_SIZE = 1024;
	
	private static final int     PACKET_QUEUE_SIZE = 15;
	private static final boolean PACKET_QUEUE_FIFO = true;
	
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private PacketWriter writer;
	
	private Thread flushThread;
	private BlockingQueue<Packet> packetQueue = new ArrayBlockingQueue<Packet>(PACKET_QUEUE_SIZE, PACKET_QUEUE_FIFO);

	public PacketController(PacketWriter writer) {
		this.writer = writer;
		start();
	}
	
	public void start() {
		flushThread = new Thread(new FlushThread());
		flushThread.setName("Packet Controller Flush Thread");
		flushThread.start();
	}
	
	public void stop() {
		flushThread.interrupt();
		try {
			flushThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void onIdentRequest(Packet request) {
		sendIdent(App.TAG);
	}
	
	public void sendIdent(String ident) {
		try {
			send(Packet.IDENT_RESPONSE, ident.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void onSensorRequest(Packet request) {
		sendSensorData();
	}
	
	public void sendSensorData() {
		Rocket rocket = App.rocketController.getRocket();
		buffer.clear();
		
//		buffer.putFloat(rocket.tankPressureLOX.getVoltage());
//		buffer.putFloat(rocket.tankPressureEngine.getVoltage());
//		buffer.putFloat(rocket.tankPressureEthanol.getVoltage());
//		buffer.putFloat(rocket.barometer.pressure);
//		buffer.putFloat(rocket.barometer.temperature);
//		buffer.put((byte) (rocket.breakWire.isBroken() ? 1 : 0));
		
		buffer.putFloat(rocket.internalAccelerometer.getX());
		buffer.putFloat(rocket.internalAccelerometer.getY());
		buffer.putFloat(rocket.internalAccelerometer.getZ());
		
//		buffer.putFloat(rocket.accelerometer.getMultiplier());
//		buffer.putInt(rocket.accelerometer.getX());
//		buffer.putInt(rocket.accelerometer.getY());
//		buffer.putInt(rocket.accelerometer.getZ());
		
		buffer.flip();
		byte[] data = new byte[buffer.limit()];
		buffer.get(data);
		
		try {
			send(Packet.SENSOR_RESPONSE, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void onDataCollectionRequest(Packet request) {
		if (request.data == null || request.data.length == 0) {
			App.stats.network.packetsDropped.incrementAndGet();
			App.log.e(App.TAG, "Invalid data collection request.");
			return;
		}
		App.log.i(App.TAG, "onDataCollectionRequest");
		
		boolean status = false;
		byte state = request.data[0];
		
		switch (state) {
		case Packet.STATE_OFF:
			App.data.disable();
			status = true;
			break;
		case Packet.STATE_ON:
			App.data.enable();
			status = true;
			break;
		case Packet.STATE_RESET:
			status = App.data.reset();
			break;
		}
		
		sendDataCollectionResponse(status);
	}
	
	private void sendDataCollectionResponse(boolean status) {
		 byte data = status ? Packet.STATUS_SUCCESS : Packet.STATUS_FAILURE;
//		 messenger.write(Packet.DATA_COLLECTION_RESPONSE, data);
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
	
	public void send(byte id, byte[] data) throws IOException {
		send(new Packet(id, data));
	}
	
	public void send(Packet packet) {
		if (!packetQueue.offer(packet)) {
			App.log.e(App.TAG, "Packet controller queue overflow.");
			App.stats.network.packetsDropped.incrementAndGet();
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
		}
	}

	@Override
	public void onPacketError(Throwable e) {
		App.log.e(App.TAG, "Packet controller packet error.", e);
		e.printStackTrace();
	}
	
	public class FlushThread implements Runnable {

		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Packet packet = packetQueue.take();
					writer.write(packet);
				}
			} catch (IOException e) {
				App.log.e(App.TAG, "Flush thread failed to write packet.", e);
				App.stats.network.packetsDropped.incrementAndGet();
				e.printStackTrace();
			} catch (InterruptedException e) {
				App.log.e(App.TAG, "Flush thread interrupted.", e);
				App.stats.ioio.errors.incrementAndGet();
				e.printStackTrace();
			}
		}
		
		
	}

}
