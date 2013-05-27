import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import edu.sdsu.rocket.control.DataLogger;


public class Main {
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Missing instance argument.");
			return;
		}
		
		String instanceId = args[0];
		
		String[] sensors = {
			DataLogger.ACCELEROMETER,
			DataLogger.BAROMETER1,
			DataLogger.BAROMETER,
			DataLogger.ENGINE_PRESSURE,
			DataLogger.ETHANOL_PRESSURE,
			DataLogger.LOX_PRESSURE,
		};
		
		for (String sensor : sensors) {
			if (!convert(sensor, instanceId)) {
				System.err.println("failed on " + sensor);
				return;
			}
		}
		
		System.out.println("done");
	}
	
	private static boolean convert(String sensor, String instanceId) {
		File inFile = fileFor(sensor, instanceId, "sensor");
		File outFile = fileFor(sensor, instanceId, "csv");
		
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(inFile));
		} catch (FileNotFoundException oe) {
			oe.printStackTrace();
			return false;
		}
		
		FileWriter out = null;
		try {
			out = new FileWriter(outFile);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		
		try {
			// headers
			if (DataLogger.ACCELEROMETER.equals(sensor)) {
				out.write("Timestamp (s),X (m/s^2),Y (m/s^2), Z (m/s^2)\n");
			} else if (DataLogger.ENGINE_PRESSURE.equals(sensor) ||
					DataLogger.ETHANOL_PRESSURE.equals(sensor) ||
					DataLogger.LOX_PRESSURE.equals(sensor)) {
				out.write("Timestamp (s),Voltage (V)\n");
			} else if (DataLogger.BAROMETER1.equals(sensor)) {
				out.write("Timestamp (s),Pressure (Pa),Temperature (C)\n");
			} else if (DataLogger.BAROMETER.equals(sensor)) {
				out.write("Timestamp (s),Pressure (mbar),Temperature (C)\n");
			}
		} catch (IOException e) {
			System.err.println("failed to write headers for " + sensor);
			e.printStackTrace();
			
			try {
				out.close();
			} catch (IOException he) {
				System.err.println("failed to close out for " + sensor);
				he.printStackTrace();
				return false;
			} finally {
				try {
					in.close();
				} catch (IOException he) {
					System.err.println("failed to close in for " + sensor);
					he.printStackTrace();
					return false;
				}
			}
			
			return false;
		}
		
		while (true) {
			try {
				float timestamp = in.readFloat();
				
				String values = String.valueOf(timestamp);
				
				if (DataLogger.ACCELEROMETER.equals(sensor)) {
					values += "," + in.readFloat(); // x
					values += "," + in.readFloat(); // y
					values += "," + in.readFloat(); // z
				} else if (DataLogger.ENGINE_PRESSURE.equals(sensor) ||
					DataLogger.ETHANOL_PRESSURE.equals(sensor) ||
					DataLogger.LOX_PRESSURE.equals(sensor)) {
						values += "," + in.readFloat(); // voltage
				} else if (DataLogger.BAROMETER1.equals(sensor)) {
					values += "," + in.readFloat(); // pressure (Pa)
					values += "," + in.readDouble(); // temperature (C)
				} else if (DataLogger.BAROMETER.equals(sensor)) {
					values += "," + in.readFloat(); // pressure (mbar)
					values += "," + in.readFloat(); // temperature (C)
				}
				
				out.write(values + "\n");
			} catch (EOFException eof) {
				// finished
				System.out.println("finished reading " + inFile);
				try {
					out.close();
				} catch (IOException e) {
					System.err.println("failed to close out for " + sensor);
					e.printStackTrace();
					return false;
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						System.err.println("failed to close in for " + sensor);
						e.printStackTrace();
						return false;
					}
				}
				
				return true;
			} catch (IOException e) {
				System.err.println("error reading " + inFile);
				e.printStackTrace();
			}
		}
	}
	
	private static File fileFor(String sensor, String instanceId, String extension) {
		String filename = sensor + "-" + instanceId + "." + extension;
		File file = new File(System.getProperty("user.dir"), filename);
		return file;
	}
}
