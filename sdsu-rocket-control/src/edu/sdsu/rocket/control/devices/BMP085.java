package edu.sdsu.rocket.control.devices;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import edu.sdsu.rocket.control.App;

/**
 * Barometer
 */
public class BMP085 implements Device {
	
	public interface BMP085Listener {
		public void onBMP085Values(float pressure /* Pa */, double temperature /* C */);
	}
	
	
	static final byte BMP085_DEVICE_ID = 0x77;
	static final byte BMP085_CHIP_ID = 0x55;
	static final byte BMP085_CALIBRATION_DATA_START = (byte)0xaa;
	static final int BMP085_CALIBRATION_DATA_LENGTH = 11;
	static final byte BMP085_CHIP_ID_REG = (byte)0xd0;
	static final byte BMP085_CTRL_REG = (byte)0xf4;
	static final byte BMP085_TEMP_MEASUREMENT = 0x2e;
	static final byte BMP085_PRESSURE_MEASUREMENT = 0x34;
	static final byte BMP085_CONVERSION_REGISTER_MSB = (byte)0xf6;
	static final byte BMP085_CONVERSION_REGISTER_LSB = (byte)0xf7;
	static final byte BMP085_CONVERSION_REGISTER_XLSB = (byte)0xf8;
	static final int BMP085_TEMP_CONVERSION_TIME = 5;

	static final int SMD500_PARAM_MG = 3038;
	static final int SMD500_PARAM_MH = -7357;
	static final int SMD500_PARAM_MI = 3791;

	private static final byte[] requestChipID = new byte[] {
		BMP085_CHIP_ID_REG
	};

	private static final byte[] requestParameters = new byte[] {
		BMP085_CALIBRATION_DATA_START
	};

	private byte[] responseParameters = new byte[BMP085_CALIBRATION_DATA_LENGTH * 2];

	private static final byte[] readTemperature = new byte[] {
		BMP085_CTRL_REG, BMP085_TEMP_MEASUREMENT
	};

	private byte[] readPressure = new byte[] {
		BMP085_CTRL_REG, BMP085_PRESSURE_MEASUREMENT
	};

	private static final byte[] requestValue = new byte[] {
		BMP085_CONVERSION_REGISTER_MSB
	};

	private byte[] response = new byte[3];
	
	
	private BMP085Listener listener;
	
	private TwiMaster twi;
	public final int twiNum;
	
	private DigitalInput eoc;
	private int eocPin;
	
	private final int oversampling;
	
	/**
	 * Pressure calibration coefficients.
	 */
	private int ac1, ac2, ac3, ac4, b1, b2;

	/**
	 * Temperature calibration coefficients.
	 */
	private int ac5, ac6, mc, md;
	
	
	private static int readU16BE(byte[] data, int offset) {
		return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
	}

	private static int readS16BE(byte[] data, int offset) {
		return (short)readU16BE(data, offset);
	}

	private static int readU24BE(byte[] data, int offset) {
		return ((data[offset] & 0xff) << 16)
				| ((data[offset + 1] & 0xff) << 8)
				| (data[offset + 2] & 0xff);
	}
	
	
	public static float p0 = 101325f; // pressure at sea level (Pa)
	
	/**
	 * Converts pressure (Pa) to altitude (m).
	 * 
	 * @param pressure Pressure to convert (Pa)
	 * @param p0       Pressure at sea level (Pa)
	 * @return Altitude in meters
	 */
	public static float altitude(float pressure, float p0) {
		return (float) (44330f * (1 - Math.pow((pressure / p0), 0.190295f)));
	}
	
	
	/**
	 * Oversampling
	 * 0 = ultra low power
	 * 1 = standard
	 * 2 = high resolution
	 * 3 = ultra high resolution
	 * 
	 * More details about oversampling can be found at:
	 * {@link https://www.sparkfun.com/tutorials/253}
	 * 
	 * @param twiNum
	 * @param eocPin
	 * @param oversampling 0, 1, 2 or 3
	 */
	public BMP085(int twiNum, int eocPin, int oversampling) {
		this.twiNum = twiNum;
		this.eocPin = eocPin;
		this.oversampling = oversampling;
		
		readPressure[1] += oversampling << 6;
	}
	
	public void setListener(BMP085Listener listener) {
		this.listener = listener;
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		twi = ioio.openTwiMaster(twiNum, TwiMaster.Rate.RATE_1MHz, false /* SMBus */);
		eoc = ioio.openDigitalInput(eocPin);
		
		byte[] responseChipID = new byte[1];
		try {
			twi.writeRead(
				BMP085_DEVICE_ID,
				false, /* tenBitAddr */
				requestChipID,
				requestChipID.length,
				responseChipID,
				responseChipID.length
			);
		} catch (InterruptedException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "Failed to query BMP085");
			return;
		}
		if (responseChipID[0] != BMP085_CHIP_ID) {
			App.log.i(App.TAG, "Not a BMP085: " + Integer.toHexString(responseChipID[0] & 0xff));
			return;
		}

		/* read the calibration coefficients */
		try {
			twi.writeRead(
				BMP085_DEVICE_ID,
				false, /* tenBitAddr */
				requestParameters,
				requestParameters.length,
				responseParameters,
				responseParameters.length
			);
		} catch (InterruptedException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "Failed to read calibration coefficients from BMP085");
			return;
		}

		ac1 = readS16BE(responseParameters, 0);
		ac2 = readS16BE(responseParameters, 2);
		ac3 = readS16BE(responseParameters, 4);
		ac4 = readU16BE(responseParameters, 6);
		ac5 = readU16BE(responseParameters, 8);
		ac6 = readU16BE(responseParameters, 10);
		b1 = readS16BE(responseParameters, 12);
		b2 = readS16BE(responseParameters, 14);
		mc = readS16BE(responseParameters, 18);
		md = readS16BE(responseParameters, 20);
	}
	
	private void readSensor(byte[] request, byte[] response, int responseLength) throws ConnectionLostException, InterruptedException {
		twi.writeRead(
			BMP085_DEVICE_ID,
			false,
			request,
			request.length,
			response,
			0
		);

		/* wait until the new value becomes ready */
		eoc.waitForValue(true);

		twi.writeRead(
			BMP085_DEVICE_ID,
			false,
			requestValue,
			requestValue.length,
			response,
			responseLength
		);
	}

	private int getB5(int ut) {
		int x1 = ((ut - ac6) * ac5) >> 15;
		int x2 = (mc << 11) / (x1 + md);
		return x1 + x2;
	}

	private int getB3(int b6) {
		int x1 = (((b6 * b6) >> 12) * b2) >> 11;
		int x2 = (ac2 * b6) >> 11;

		int x3 = x1 + x2;

		return (((ac1 * 4 + x3) << oversampling) + 2) >> 2;
	}

	private int getB4(int b6) {
		int x1 = (ac3 * b6) >> 13;
		int x2 = (b1 * ((b6 * b6) >> 12)) >> 16;
		int x3 = (x1 + x2 + 2) >> 2;
		return (ac4 * (x3 + 32768)) >> 15;
	}

	/**
	 * @return pressure [Pa]
	 */
	private int getPressure(int up, int param_b5) {
		int b6 = param_b5 - 4000;
		int b3 = getB3(b6);
		int b4 = getB4(b6);

		int b7 = ((up - b3) * (50000 >> oversampling));

		int pressure;
		if (b7 < 0x80000000)
		pressure = (b7 << 1) / b4;
		else
		pressure = (b7 / b4) << 1;

		int x1 = pressure >> 8;
		x1 *= x1;

		x1 = (x1 * SMD500_PARAM_MG) >> 16;
		int x2 = (pressure * SMD500_PARAM_MH) >> 16;
		pressure += (x1 + x2 + SMD500_PARAM_MI) >> 4;

		return pressure;
	}

	public void loop() throws ConnectionLostException, InterruptedException {
		readSensor(readTemperature, response, 2);
		int b5 = getB5(readU16BE(response, 0));
		double temperature_c = (double)((b5 + 8) >> 4) / 10.0;

		readSensor(readPressure, response, 3);
		int up = readU24BE(response, 0) >> (8 - oversampling);
		int pressure_pa = getPressure(up, b5);

		if (listener != null) {
			listener.onBMP085Values(pressure_pa, temperature_c);
		}
	}

	// TODO do we need to twi.close and eoc.close on disconnect of IOIO?
	
	/*
	 * Device interface methods.
	 */
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": twi=" + twiNum + ", eoc=" + eocPin;
	}
	
}
