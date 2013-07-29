package edu.sdsu.rocket.control;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Environment;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.devices.PhoneAccelerometer;
import edu.sdsu.rocket.control.models.Rocket;

public class DataLogger {

	private static boolean LOG = false;
	
	public static final String STATUS = "status";
	public static final String BAROMETER = "baro";
	public static final String ENGINE_PRESSURE = "eng";
	public static final String LOX_PRESSURE = "lox";
	public static final String ETHANOL_PRESSURE = "eth";
	public static final String ACCELEROMETER = "accel";
	
	private static final int STATUS_BUFFER_SIZE = 1024;
	private static final int BAROMETER_BUFFER_SIZE = 512;
	private static final int PRESSURE_BUFFER_SIZE = 512;
	private static final int ACCELEROMETER_BUFFER_SIZE = 512;
	
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
		makeStream(STATUS, STATUS_BUFFER_SIZE);
		
		makeStream(BAROMETER, BAROMETER_BUFFER_SIZE);
		rocket.barometer.setListener(new MS5611.MS5611Listener() {
			@Override
			public void onMS5611Values(float pressure /* mbar */, float temperature /* C */) {
				if (enabled) {
					DataOutputStream stream = out.get(BAROMETER);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + BAROMETER + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(pressure);
							stream.writeFloat(temperature);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + BAROMETER + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
				
				if (LOG) {
					App.log.i(App.TAG, BAROMETER + " = P: " + pressure + " mbar, T: " + temperature + " C");
					
//					float temperatureK = temperature + 273.15f; // C -> K
//					double tempF = Units.convertCelsiusToFahrenheit(temperature);
//					
//					/*
//					 * http://en.wikipedia.org/wiki/Density_altitude
//					 */
//					float Psl = 1013.25f; // standard sea level atmospheric pressure (hPa)
//					float Tsl = 288.15f; // ISA standard sea level air temperature (K)
//					float b = 0.234969f;
//					
////					float altitude = 145442.156f * (1f - (float)Math.pow((pressure / Psl) / (temperature / Tsl), b));
//					float altitude = (((float)Math.pow(Psl / pressure, 1f/5.257f) - 1f) * temperatureK) / 0.0065f;
//					
//					App.log.i(App.TAG, BAROMETER + " = A: " + altitude + " ft, T: " + tempF + " F");
				}
			}
		});
		
		makeStream(LOX_PRESSURE, PRESSURE_BUFFER_SIZE);
		rocket.tankPressureLOX.setListener(new P51500AA1365V.P51500AA1365VListener() {
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
				
				if (LOG) {
					App.log.i(App.TAG, LOX_PRESSURE + " = " + rocket.tankPressureLOX.getPressure() + " PSI");
				}
			}
		});
		
		makeStream(ETHANOL_PRESSURE, PRESSURE_BUFFER_SIZE);
		rocket.tankPressureEthanol.setListener(new P51500AA1365V.P51500AA1365VListener() {
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
				
				if (LOG) {
					App.log.i(App.TAG, ETHANOL_PRESSURE + " = " + rocket.tankPressureEthanol.getPressure() + " PSI");
				}
			}
		});
		
		makeStream(ENGINE_PRESSURE, PRESSURE_BUFFER_SIZE);
		rocket.tankPressureEngine.setListener(new P51500AA1365V.P51500AA1365VListener() {
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
				
				if (LOG) {
					App.log.i(App.TAG, ENGINE_PRESSURE + " = " + rocket.tankPressureEngine.getPressure() + " PSI");
				}
			}
		});
		
		makeStream(ACCELEROMETER, ACCELEROMETER_BUFFER_SIZE);
		rocket.internalAccelerometer.setListener(new PhoneAccelerometer.PhoneAccelerometerListener() {
			@Override
			public void onPhoneAccelerometer(float x, float y, float z) {
				if (enabled) {
					DataOutputStream stream = out.get(ACCELEROMETER);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + ACCELEROMETER + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(x);
							stream.writeFloat(y);
							stream.writeFloat(z);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + ACCELEROMETER + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
				
				if (LOG) {
					App.log.i(App.TAG, ACCELEROMETER + " = [" + rocket.internalAccelerometer.getX() + ", " + rocket.internalAccelerometer.getY() + ", " + rocket.internalAccelerometer.getZ() + "]");
				}
			}
		});
		
		return true;
	}
	
	public void enable() {
		logTime();
		
		enabled = true;
		rocket.internalAccelerometer.start();
		App.log.i(App.TAG, "Enabled data logging.");
	}
	
	private void logTime() {
		DataOutputStream stream = out.get(STATUS);
		
		if (stream == null) {
			App.log.e(App.TAG, "Output stream not available for " + STATUS + ".");
		} else {
			try {
				stream.writeLong(System.currentTimeMillis());
				stream.writeFloat(App.elapsedTime());
			} catch (IOException e) {
				App.log.e(App.TAG, "Failed to write " + STATUS + " values to output stream.");
				e.printStackTrace();
				return;
			}
		}
	}

	public void disable() {
		enabled = false;
		rocket.internalAccelerometer.stop();
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
		// FIXME /mnt/sdcard/sd_external for Galaxy S2
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
