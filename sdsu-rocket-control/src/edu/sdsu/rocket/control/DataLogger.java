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
import edu.sdsu.rocket.control.devices.ArduIMU;
import edu.sdsu.rocket.control.devices.BMP085;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.models.Rocket;

public class DataLogger {

	private static boolean LOG = true;
	
	private static final String IMU = "imu";
	private static final String BAROMETER1 = "baro1";
	private static final String BAROMETER2 = "baro2";
	private static final String ENGINE_PRESSURE = "eng";
	private static final String LOX_PRESSURE = "lox";
	private static final String ETHANOL_PRESSURE = "eth";
	
	private static final int BAROMETER_BUFFER_SIZE = 128;
	private static final int PRESSURE_BUFFER_SIZE = 64;
	private static final int IMU_BUFFER_SIZE = 1024;
	
	private boolean enabled;
	public final Map<String, DataOutputStream> out = new HashMap<String, DataOutputStream>();

	public DataLogger(final Rocket rocket) {
		setup(rocket);
	}
	
	public void setup(final Rocket rocket) {
		makeStream(BAROMETER1, BAROMETER_BUFFER_SIZE);
		rocket.barometer1.setListener(new BMP085.BMP085Listener() {
			@Override
			public void onBMP085Values(float pressure /* Pa */, double temperature /* C */) {
				if (enabled) {
					DataOutputStream stream = out.get(BAROMETER1);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + BAROMETER1 + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(pressure);
							stream.writeDouble(temperature);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + BAROMETER1 + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
				
				if (LOG) {
					App.log.i(App.TAG, BAROMETER1 + " = P: " + pressure + " Pa, T: " + temperature + " C");
					
					float alt = BMP085.altitude(pressure, BMP085.p0);
					alt = Units.convertMetersToFeet(alt);
					double temp = Units.convertCelsiusToFahrenheit(temperature);
					
					App.log.i(App.TAG, BAROMETER1 + " = A: " + alt + " ft, T: " + temp + " F");
				}
			}
		});
		
		makeStream(BAROMETER2, BAROMETER_BUFFER_SIZE);
		rocket.barometer2.setListener(new MS5611.MS5611Listener() {
			@Override
			public void onMS5611Values(float pressure /* mbar */, float temperature /* C */) {
				if (enabled) {
					DataOutputStream stream = out.get(BAROMETER2);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + BAROMETER2 + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeFloat(pressure);
							stream.writeFloat(temperature);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + BAROMETER2 + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
				
				if (LOG) {
					App.log.i(App.TAG, BAROMETER2 + " = P: " + pressure + " mbar, T: " + temperature + " C");
					
					float temperatureK = temperature + 273.15f; // C -> K
					double tempF = Units.convertCelsiusToFahrenheit(temperature);
					
					/*
					 * http://en.wikipedia.org/wiki/Density_altitude
					 */
					float Psl = 1013.25f; // standard sea level atmospheric pressure (hPa)
					float Tsl = 288.15f; // ISA standard sea level air temperature (K)
					float b = 0.234969f;
					
//					float altitude = 145442.156f * (1f - (float)Math.pow((pressure / Psl) / (temperature / Tsl), b));
					float altitude = (((float)Math.pow(Psl / pressure, 1f/5.257f) - 1f) * temperatureK) / 0.0065f;
					
					App.log.i(App.TAG, BAROMETER2 + " = A: " + altitude + " ft, T: " + tempF + " F");
				}
			}
		});
		
		makeStream(LOX_PRESSURE, PRESSURE_BUFFER_SIZE);
		rocket.tankPressureLOX.setListener(new P51500AA1365V.P51500AA1365VListener() {
			@Override
			public void onP51500AA1365VValue(float voltage) {
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
			public void onP51500AA1365VValue(float voltage) {
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
			public void onP51500AA1365VValue(float voltage) {
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
		
		makeStream(IMU, IMU_BUFFER_SIZE);
		rocket.imu.setListener(new ArduIMU.ArduIMUListener() {
			@Override
			public void onArduIMUValues(String values) {
				if (enabled) {
					DataOutputStream stream = out.get(IMU);
					
					if (stream == null) {
						App.log.e(App.TAG, "Output stream not available for " + IMU + ".");
					} else {
						try {
							stream.writeFloat(App.elapsedTime());
							stream.writeChars(values);
						} catch (IOException e) {
							App.log.e(App.TAG, "Failed to write " + IMU + " values to output stream.");
							e.printStackTrace();
							return;
						}
					}
				}
				
				if (LOG) {
					App.log.i(App.TAG, IMU + " = " + values);
				}
			}
		});
	}
	
	public void enable() {
		enabled = true;
		App.log.i(App.TAG, "Enabled data logging.");
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
	
	public void close() {
		for (Entry<String, DataOutputStream> entry : out.entrySet()) {
			try {
				entry.getValue().close();
			} catch (IOException e) {
				App.log.e(App.TAG, "Failed to close output stream for " + entry.getKey() + ".");
				e.printStackTrace();
			}
		}
	}
	
	private DataOutputStream makeStream(String name, int bufferSize) {
		File sd = Environment.getExternalStorageDirectory();
		File file = new File(sd, name + "-" + App.getNanoTime() + ".sensor");
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
