package edu.sdsu.rocket.control.controllers;

import java.nio.ByteBuffer;

import edu.sdsu.rocket.Serial;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.devices.SB70;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.io.Packet;

public class CommandController {
	
	private static final int BUFFER_SIZE = 1024;
	
	private SB70 sb70;
	private Thread thread;
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

	private long threadSleep; // milliseconds

	public CommandController(SB70 sb70, long threadSleep) {
		this.sb70 = sb70;
		this.threadSleep = threadSleep;
	}
	
	public void start() {
		thread = new Thread(new CommandRunnable(threadSleep));
		thread.start();
	}
	
	public void stop() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	protected void onPacketReceived(Packet packet) {
		switch (packet.messageId) {
		case Serial.DATA_COLLECTION_REQUEST:
			onDataCollectionRequest(packet);
			break;
		case Serial.SENSOR_REQUEST:
			onSensorRequest(packet);
			break;
		case Serial.SET_OBJECTIVE_REQUEST:
			onSetObjectiveRequest(packet);
			break;
		}
	}
	
	private void onSensorRequest(Packet request) {
		
	}
	
	private void onSensorResponse() {
		Rocket rocket = App.rocket;
		buffer.clear();
		
		buffer.putFloat(rocket.tankPressureLOX.voltage);
		buffer.putFloat(rocket.tankPressureLOX.getPressure());
		buffer.putFloat(rocket.tankPressureEngine.voltage);
		buffer.putFloat(rocket.tankPressureEngine.getPressure());
		buffer.putFloat(rocket.tankPressureEthanol.voltage);
		buffer.putFloat(rocket.tankPressureEthanol.getPressure());
		buffer.putFloat(rocket.barometer.pressure);
		buffer.putFloat(rocket.barometer.temperature);
		buffer.put((byte) (rocket.breakWire.isBroken() ? 1 : 0));
		
		buffer.flip();
		byte[] data = new byte[buffer.limit()];
		buffer.get(data);
		
		sb70.send(Serial.SENSOR_RESPONSE, data);
	}

	private void onDataCollectionRequest(Packet request) {
		if (request.data == null || request.data.length == 0) {
			App.log.e(App.TAG, "Invalid data collection request.");
			return;
		}
		
		byte state = request.data[0];
		switch (state) {
		case Serial.STATE_OFF:
			App.data.disable();
			break;
		case Serial.STATE_ON:
			App.data.enable();
			break;
		case Serial.STATE_RESET:
			App.data.reset();
			break;
		}
		
		onDataCollectionResponse(App.data.isEnabled());
	}
	
	private void onDataCollectionResponse(boolean enabled) {
		byte state = enabled ? Serial.STATE_ON : Serial.STATE_OFF;
		sb70.send(Serial.DATA_COLLECTION_RESPONSE, state);
	}

	private void onSetObjectiveRequest(Packet packet) {
		if (packet.data == null || packet.data.length == 0) {
			App.stats.network.packetsDropped++;
			return;
		}
		
		String objective;
		switch (packet.data[0]) {
		case Serial.OBJECTIVE_FILL_TANKS:
			objective = Serial.OBJECTIVE_NAME_FILL_TANKS;
			break;
		case Serial.OBJECTIVE_LAUNCH:
			objective = Serial.OBJECTIVE_NAME_LAUNCH;
			break;
		case Serial.OBJECTIVE_FLIGHT:
			objective = Serial.OBJECTIVE_NAME_FLIGHT;
			break;
		default:
			App.log.e(App.TAG, "Unknown objective: " + packet.data[0]);
			return;
		}
		
		boolean success = App.objective.set(objective);
		if (!success)
			App.log.e(App.TAG, "Failed to set to " + objective + " objective.");
		
		onSetObjectiveResponse(packet.data[0], success);
	}
	
	private void onSetObjectiveResponse(byte objective, boolean success) {
		byte status = success ? Serial.STATUS_SUCCESS : Serial.STATUS_FAILURE;
		sb70.send(Serial.SET_OBJECTIVE_RESPONSE, new byte[] { objective, status });
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

	class CommandRunnable implements Runnable {
		
		private long sleep; // milliseconds

		public CommandRunnable(long sleep) {
			this.sleep = sleep;
		}

		@Override
		public void run() {
			Packet packet;
			while (!Thread.currentThread().isInterrupted()) {
				while ((packet = sb70.get()) != null)
					onPacketReceived(packet);
				
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
		
	}

}
