package edu.sdsu.rocket.control;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Environment;
import edu.sdsu.rocket.control.devices.ADXL345;
import edu.sdsu.rocket.control.devices.ITG3205;
import edu.sdsu.rocket.control.devices.MAX31855;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.devices.PhoneAccelerometer;
import edu.sdsu.rocket.control.models.Rocket;

public class DataLogger {
	
	public enum Event {
		ENABLE       ((byte) 0x01),
		IGNITE       ((byte) 0x02),
		LAUNCH       ((byte) 0x03),
		ABORT        ((byte) 0x04),
		ACCELEROMETER_MULTIPLIER((byte) 0x05),
		ETHANOL_OPEN ((byte) 0x06),
		ETHANOL_CLOSE((byte) 0x07),
		LOX_OPEN     ((byte) 0x08),
		LOX_CLOSE    ((byte) 0x09),
		;
		private byte value;
		Event(byte value) {
			this.value = value;
		}
		public byte getValue() {
			return value;
		}
	}

	public static final String EVENT               = "event";
	
	public static final String ENGINE_PRESSURE     = "eng";
	public static final String LOX_PRESSURE        = "lox";
	public static final String ETHANOL_PRESSURE    = "eth";
	
	public static final String BAROMETER           = "baro";
	public static final String ACCELEROMETER       = "accel";
	public static final String GYRO                = "gyro";
	
	public static final String LOX_TEMPERATURE     = "loxtemp"; // TODO
	public static final String IGNITOR_TEMPERATURE = "igntemp"; // TODO
	
	public static final String INTERNAL_ACCELEROMETER = "intaccel";
	
	private static final int EVENT_BUFFER_SIZE         = 512;
	private static final int BAROMETER_BUFFER_SIZE     = 512;
	private static final int PRESSURE_BUFFER_SIZE      = 512;
	private static final int ACCELEROMETER_BUFFER_SIZE = 512;
	private static final int GYRO_BUFFER_SIZE          = 512;
	private static final int TEMPERATURE_BUFFER_SIZE   = 512;
	
	private boolean enabled;
	public final Map<String, DataOutputStream> out = new HashMap<String, DataOutputStream>();

	private Rocket rocket;

	public DataLogger(final Rocket rocket) {
		this.rocket = rocket;
		setup();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	// FIXME return false if there are any failures
	public boolean setup() {
		makeStream(EVENT, EVENT_BUFFER_SIZE);
		
		makeStream(BAROMETER, BAROMETER_BUFFER_SIZE);
		rocket.barometer.setListener(new MS5611.MS5611Listener() {
			@Override
			public void onData(int P /* mbar */, int TEMP /* C */) {
				if (enabled) {
					DataOutputStream stream = out.get(BAROMETER);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + BAROMETER + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeInt(P);
							stream.writeInt(TEMP);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + BAROMETER + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
			}

			@Override
			public void onError(String message) {}
		});
		
//		makeStream(GYRO, GYRO_BUFFER_SIZE);
//		rocket.gyro.setListener(new ITG3205.ITG3205Listener() {
//			@Override
//			public void onDeviceId(byte deviceId) {}
//			
//			@Override
//			public void onData(int x, int y, int z, int temperature) {
//				if (enabled) {
//					DataOutputStream stream = out.get(GYRO);
//					
//					if (stream == null) {
//						App.log.e(App.TAG, "Output stream not available for " + GYRO + ".");
//					} else {
//						try {
//							stream.writeFloat(App.elapsedTime());
//							stream.writeInt(x);
//							stream.writeInt(y);
//							stream.writeInt(z);
//							stream.writeInt(temperature);
//						} catch (IOException e) {
//							App.log.e(App.TAG, "Failed to write " + GYRO + " values to output stream.");
//							e.printStackTrace();
//							return;
//						}
//					}
//				}
//			}
//			
//			@Override
//			public void onError(String message) {}
//		});
		
		makeStream(LOX_PRESSURE, PRESSURE_BUFFER_SIZE);
		rocket.loxPressure.setListener(new P51500AA1365V.P51500AA1365VListener() {
			@Override
			public void onVoltage(float voltage) {
				if (enabled) {
					DataOutputStream stream = out.get(LOX_PRESSURE);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + LOX_PRESSURE + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(voltage);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + LOX_PRESSURE + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
			}
		});
		
		makeStream(ETHANOL_PRESSURE, PRESSURE_BUFFER_SIZE);
		rocket.ethanolPressure.setListener(new P51500AA1365V.P51500AA1365VListener() {
			@Override
			public void onVoltage(float voltage) {
				if (enabled) {
					DataOutputStream stream = out.get(ETHANOL_PRESSURE);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + ETHANOL_PRESSURE + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(voltage);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + ETHANOL_PRESSURE + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
			}
		});
		
		makeStream(ENGINE_PRESSURE, PRESSURE_BUFFER_SIZE);
		rocket.enginePressure.setListener(new P51500AA1365V.P51500AA1365VListener() {
			@Override
			public void onVoltage(float voltage) {
				if (enabled) {
					DataOutputStream stream = out.get(ENGINE_PRESSURE);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + ENGINE_PRESSURE + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(voltage);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + ENGINE_PRESSURE + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
			}
		});
		
		makeStream(ACCELEROMETER, ACCELEROMETER_BUFFER_SIZE);
		rocket.accelerometer.setListener(new ADXL345.ADXL345Listener() {
			
			@Override
			public void onDeviceId(byte deviceId) {}
			
			@Override
			public void onMultiplier(float multiplier) {
				byte[] data = new byte[4];
				ByteBuffer.wrap(data).putFloat(multiplier);
				event(Event.ACCELEROMETER_MULTIPLIER, data);
			}
			
			@Override
			public void onData(int x, int y, int z) {
				if (enabled) {
					DataOutputStream stream = out.get(ACCELEROMETER);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + ACCELEROMETER + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeInt(x);
							stream.writeInt(y);
							stream.writeInt(z);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + ACCELEROMETER + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
			}
			
			@Override
			public void onError(String message) {}
			
		});
		
		makeStream(INTERNAL_ACCELEROMETER, ACCELEROMETER_BUFFER_SIZE);
		rocket.internalAccelerometer.setListener(new PhoneAccelerometer.PhoneAccelerometerListener() {
			@Override
			public void onPhoneAccelerometer(float x, float y, float z) {
				if (enabled) {
					DataOutputStream stream = out.get(INTERNAL_ACCELEROMETER);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + INTERNAL_ACCELEROMETER + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(x);
							stream.writeFloat(y);
							stream.writeFloat(z);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + INTERNAL_ACCELEROMETER + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
			}
		});
		
		makeStream(LOX_TEMPERATURE, TEMPERATURE_BUFFER_SIZE);
		rocket.loxTemperature.setListener(new MAX31855.MAX31855Listener() {
			@Override
			public void onFault(byte fault) {}
			
			@Override
			public void onData(float internal, float thermocouple) {
				if (enabled) {
					DataOutputStream stream = out.get(LOX_TEMPERATURE);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + LOX_TEMPERATURE + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(internal);
							stream.writeFloat(thermocouple);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + LOX_TEMPERATURE + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
			}
		});
		
		makeStream(IGNITOR_TEMPERATURE, TEMPERATURE_BUFFER_SIZE);
		rocket.ignitorTemperature.setListener(new MAX31855.MAX31855Listener() {
			@Override
			public void onFault(byte fault) {}
			
			@Override
			public void onData(float internal, float thermocouple) {
				if (enabled) {
					DataOutputStream stream = out.get(IGNITOR_TEMPERATURE);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + IGNITOR_TEMPERATURE + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(internal);
							stream.writeFloat(thermocouple);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + IGNITOR_TEMPERATURE + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
			}
		});
		
		return true;
	}
	
	public void enable() {
		logTime();
		
		enabled = true;
		App.log.i(App.TAG, "Enabled data logging.");
	}
	
	public void event(Event event) {
		event(event, null);
	}
	
	private void event(Event event, byte[] data) {
		DataOutputStream stream = out.get(EVENT);
		
		if (stream == null) {
			App.log.e(App.TAG, "Output stream not available for " + EVENT + ".");
		} else {
			try {
				float elapsedTime = App.elapsedTime();
				stream.writeFloat(elapsedTime);
				stream.write(event.getValue());
				if (data != null) {
					stream.write(data);
				}
			} catch (IOException e) {
				App.log.e(App.TAG, "Failed to write " + EVENT + " values to output stream.");
				e.printStackTrace();
				return;
			}
		}
	}
	
	private void logTime() {
		long currentTime = System.currentTimeMillis();
		
		byte[] data = new byte[8];
		ByteBuffer.wrap(data).putLong(currentTime);
		
		event(Event.ENABLE, data);
	}

	public void disable() {
		enabled = false;
		App.log.i(App.TAG, "Disabled data logging.");
	}
	
	public void flush() {
		for (Entry<String, DataOutputStream> entry : out.entrySet()) {
			try {
				entry.getValue().flush();
			} catch (IOException e) {
				App.log.e(App.TAG, "Failed to flush output stream for " + entry.getKey() + ".");
				e.printStackTrace();
			}
		}
	}
	
	public boolean close() {
		boolean success = true;
		for (Entry<String, DataOutputStream> entry : out.entrySet()) {
			try {
				entry.getValue().close();
			} catch (IOException e) {
				success = false;
				App.log.e(App.TAG, "Failed to close output stream for " + entry.getKey() + ".");
				e.printStackTrace();
			}
		}
		return success;
	}
	
	public boolean reset() {
		boolean success = true;
		disable();
		success = close() && success;
		success = setup() && success;
		enable();
		return success;
	}
	
	private DataOutputStream makeStream(String name, int bufferSize) {
		File sd = Environment.getExternalStorageDirectory();
		File path = new File(sd, "sensors");
		File file;
		if (path.isDirectory() || path.mkdirs()) {
			file = new File(path, name + "-" + App.getInstanceId() + ".sensor");
		} else {
			// failed to create dir, just use the SD root
			file = new File(sd, name + "-" + App.getInstanceId() + ".sensor");
		}
		
		App.log.i(App.TAG, "Making output stream for: " + file.getAbsolutePath());
		BufferedOutputStream stream = null;
		
		try {
			stream = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			App.log.e(App.TAG, "Failed to create output stream for " + name + ".");
			return null;
		}
		
		App.log.i(App.TAG, "Created output stream for " + name + ".");
		
		DataOutputStream dataOutputStream = new DataOutputStream(stream);
		out.put(name, dataOutputStream);
		
		return dataOutputStream;
	}
	
}
