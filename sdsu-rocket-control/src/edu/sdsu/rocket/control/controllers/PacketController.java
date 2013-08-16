package edu.sdsu.rocket.control.controllers;

import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.io.Packet;
import edu.sdsu.rocket.io.PacketListener;
import edu.sdsu.rocket.io.PacketWriter;

public class PacketController implements PacketListener {
	
	private static final int     QUEUE_CAPACITY = 15;
	private static final boolean QUEUE_FIFO     = true;
	
	private static final int BUFFER_SIZE = 1024; // bytes
	
	private ArrayBlockingQueue<Packet> queue = new ArrayBlockingQueue<Packet>(QUEUE_CAPACITY, QUEUE_FIFO);
	private Thread flushThread;
	
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private PacketWriter writer;
	
	public PacketController(PacketWriter writer) {
		this.writer = writer;
	}
	
	public void start() {
		queue.clear();
		flushThread = new Thread(new FlushRunnable());
		flushThread.setName("Packet Flush");
		flushThread.start();
	}
	
	public void stop() {
		flushThread.interrupt();
		try {
			flushThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	
	private void onValveRequest(Packet request) {
		if (request.data == null || request.data.length < 2) {
			App.stats.network.packetsDropped.incrementAndGet();
			App.log.e(App.TAG, "Invalid valve request.");
			return;
		}
		
		byte valve  = request.data[0];
		byte action = request.data[1];
		
		boolean error = false;
		
		switch (valve) {
		case Packet.VALVE_REQUEST_ETHANOL:
			if (action == Packet.VALVE_REQUEST_OPEN) {
				App.rocketController.openEthanolVent();
			} else if (action == Packet.VALVE_REQUEST_CLOSE) {
				App.rocketController.closeEthanolVent();
			} else {
				error = true;
			}
			break;
		case Packet.VALVE_REQUEST_LOX:
			if (action == Packet.VALVE_REQUEST_OPEN) {
				App.rocketController.openLOXVent();
			} else if (action == Packet.VALVE_REQUEST_CLOSE) {
				App.rocketController.closeLOXVent();
			} else {
				error = true;
			}
			break;
		default:
			error = true;
		}
		
		if (error) {
			App.stats.network.packetsDropped.incrementAndGet();
			App.log.e(App.TAG, "Unknown valve request: " + valve + ", " + action);
		}
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
		App.rocketController.ignite();
	}
	
	private void onLaunchRequest(Packet packet) {
		App.rocketController.launch();
	}
	
	private void onAbortRequest(Packet packet) {
		App.rocketController.abortLaunch();
	}
	
	private void onIOIORequest(Packet request) {
		if (request.data == null || request.data.length == 0) {
			App.stats.network.packetsDropped.incrementAndGet();
			App.log.e(App.TAG, "Invalid IOIO reset request.");
			return;
		}
		
		if (request.data[0] == Packet.IOIO_REQUEST_DISCONNECT) {
			if (App.ioio == null) {
				App.log.e(App.TAG, "Failed to disconnect IOIO.\nIOIO not connected.");
			} else {
				App.log.i(App.TAG, "Performing IOIO disconnect.");
				stop();
				App.ioio.disconnect();
				start();
			}
		} else if (request.data[0] == Packet.IOIO_REQUEST_SOFT_RESET) {
			if (App.ioio == null) {
				App.log.e(App.TAG, "Failed to soft reset IOIO.\nIOIO not connected.");
			} else {
				try {
					App.log.i(App.TAG, "Performing IOIO soft reset.");
					App.ioio.softReset();
				} catch (ConnectionLostException e) {
					App.log.e(App.TAG, "Connection lost during IOIO soft reset.", e);
				}
			}
		} else if (request.data[0] == Packet.IOIO_REQUEST_HARD_RESET) {
			if (App.ioio == null) {
				App.log.e(App.TAG, "Failed to hard reset IOIO.\nIOIO not connected.");
			} else {
				try {
					App.log.i(App.TAG, "Performing IOIO hard reset.");
					App.ioio.hardReset();
				} catch (ConnectionLostException e) {
					App.log.e(App.TAG, "Connection lost during IOIO hard reset.", e);
				}
			}
		} else {
			App.stats.network.packetsDropped.incrementAndGet();
			App.log.e(App.TAG, "Unknown IOIO reset request: " + request.data[0]);
		}
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
		
		buffer.put((byte) (rocket.ignitor.isActive() ? 1 : 0));
		buffer.put((byte) (rocket.breakWire.isBroken() ? 1 : 0));
		
		buffer.putFloat(rocket.loxPressure.getPressure());
		buffer.putFloat(rocket.ethanolPressure.getPressure());
		buffer.putFloat(rocket.enginePressure.getPressure());
		
		// used for calibration:
//		buffer.putFloat(rocket.loxPressure.getVoltage());
//		buffer.putFloat(rocket.ethanolPressure.getVoltage());
//		buffer.putFloat(rocket.enginePressure.getVoltage());
		
		buffer.putFloat(rocket.loxTemperature.getInternalTemperature());
		buffer.putFloat(rocket.loxTemperature.getTemperature());
		
		buffer.putInt(rocket.barometer.getTemperature());
		buffer.putInt(rocket.barometer.getPressure());
		
		buffer.putFloat(rocket.accelerometer.getMultiplier());
		buffer.putInt(rocket.accelerometer.getX());
		buffer.putInt(rocket.accelerometer.getY());
		buffer.putInt(rocket.accelerometer.getZ());
		
		buffer.putFloat(rocket.internalAccelerometer.getX());
		buffer.putFloat(rocket.internalAccelerometer.getY());
		buffer.putFloat(rocket.internalAccelerometer.getZ());
		
		buffer.putFloat(rocket.ignitorTemperature);
		
		buffer.flip();
		byte[] data = new byte[buffer.limit()];
		buffer.get(data);
		
		send(Packet.SENSOR_RESPONSE, data);
	}
	
	private void sendDataCollectionResponse(boolean on) {
		 byte data = on ? Packet.DATA_COLLECTION_RESPONSE_ON : Packet.DATA_COLLECTION_RESPONSE_OFF;
		 send(Packet.DATA_COLLECTION_RESPONSE, new byte[] { data });
	}
	
	public void send(Packet packet) {
		if (!queue.offer(packet)) {
			App.log.e(App.TAG, "Send packet queue overflow.");
		}
	}
	
	public void send(byte id, byte[] data) {
		send(new Packet(id, data));
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
		case Packet.VALVE_REQUEST:
			onValveRequest(packet);
			break;
		case Packet.IGNITE_REQUEST:
			onIgniteRequest(packet);
			break;
		case Packet.LAUNCH_REQUEST:
			onLaunchRequest(packet);
			break;
		case Packet.ABORT_REQUEST:
			onAbortRequest(packet);
			break;
		case Packet.DATA_COLLECTION_REQUEST:
			onDataCollectionRequest(packet);
			break;
		case Packet.IOIO_REQUEST:
			onIOIORequest(packet);
			break;
		default:
			App.log.e(App.TAG, "Unknown packet ID: " + packet.messageId);
		}
	}
	
	public class FlushRunnable implements Runnable {

		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Packet packet = queue.take();
					writer.write(packet);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				App.stats.network.packetsDropped.incrementAndGet();
				App.log.e(App.TAG, "Packet controller failed to send packet.", e);
			}
		}
		
	}

}
