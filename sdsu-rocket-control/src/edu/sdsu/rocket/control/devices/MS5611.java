package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import edu.sdsu.rocket.control.App;

/**
 * Barometer
 * {@link http://www.embeddedadventures.com/barometric_pressure_sensor_module_mod-1009.html}
 * 
 * The MS5611-01BA has only five basic commands:
 *   1. Reset
 *   2. Read PROM (128 bit of calibration words)
 *   3. D1 conversion
 *   4. D2 conversion
 *   5. Read ADC result (24 bit pressure / temperature)
 */
public class MS5611 implements Device {
	
	public interface MS5611Listener {
		public void onMS5611Values(float pressure /* mbar */, float temperature /* C */);
	}

	
	// CSB = chip select, device address (7 bit)
	public static final byte ADD_CSB_LOW  = 0x76; // CSB to GND
	public static final byte ADD_CSB_HIGH = 0x77; // CSB to VCC
	
	// commands per page 9 of MS5611-01BA03 datasheet
	static final byte CMD_RESET     = 0x1E; // ADC reset command
	static final byte CMD_ADC_READ  = 0x00; // ADC read command
	static final byte CMD_ADC_PRESS = 0x40; // ADC D1 conversion
	static final byte CMD_ADC_TEMP  = 0x50; // ADC D2 conversion
	static final byte CMD_ADC_256   = 0x00; // ADC OSR=256
	static final byte CMD_ADC_512   = 0x02; // ADC OSR=512
	static final byte CMD_ADC_1024  = 0x04; // ADC OSR=1024
	static final byte CMD_ADC_2048  = 0x06; // ADC OSR=2048
	static final byte CMD_ADC_4096  = 0x08; // ADC OSR=4096
	static final int  CMD_PROM_RD   = 0xA0; // PROM read command
	
	static final byte oversampling = CMD_ADC_4096;
	
	private byte[] requestCaldata = new byte[1];
	
	private final byte[] requestReset = new byte[] {
		CMD_RESET
	};
	
	private final byte[] requestTemp = new byte[] {
		CMD_ADC_TEMP + oversampling
	};
	
	private final byte[] requestPress = new byte[] {
		CMD_ADC_PRESS + oversampling
	};
	
	private final byte[] requestValue = new byte[] {
		CMD_ADC_READ
	};
	
	private byte[] response = new byte[3];
	
	/**
	 * MS5611 Pressure calibration coefficients, C1s and C5s are left shifted from C1 and C5.
	 */
	private long C1s = 0, C2s = 0, C3 = 0, C4 = 0;
	
	/**
	 * MS5611 Temperature calibration coefficients.
	 */
	private int	C5s = 0;
	private long C6	= 0;
	
	private byte[] dummy = new byte[0];
	private int sleep_time;

	
	private MS5611Listener listener;
	
	private TwiMaster twi;
	private int twiNum;
	private byte address;
	private int sample_rate;
	
	
	private static int readU16BE(byte[] data, int offset) {
		return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
	}
	
	private static int readU24BE(byte[] data, int offset) {
		return ((data[offset] & 0xff) << 16) | ((data[offset + 1] & 0xff) << 8)
				| (data[offset + 2] & 0xff);
	}
	
	
	public MS5611(int twiNum, byte address, int sample_rate) {
		this.twiNum = twiNum;
		this.address = address;
		this.sample_rate = sample_rate;
	}
	
	public void setListener(MS5611Listener listener) {
		this.listener = listener;
	}
	
	private void reset() throws ConnectionLostException, InterruptedException {
		twi.writeRead(
			address,
			false, /* tenBitAddr */
			requestReset,
			requestReset.length,
			dummy,
			0
		);
		
		Thread.sleep(5);
	}
	
	private boolean readPROM() throws ConnectionLostException, InterruptedException {
		reset();
		
		App.log.i(App.TAG, "Reading PROM");
		int[] prom = new int [8];

		// until checksum is implemented: too many zero results from PROM means no barometer present
		int zeros = 0;

		// read Caldata from PROM
		for (int i = 0; i < 8; i++) {
			requestCaldata[0] = (byte)(CMD_PROM_RD + 2*i);
			
			twi.writeRead(
				address,
				false, /* tenBitAddr */
				requestCaldata,
				requestCaldata.length,
				dummy,
				0
			);
			
			response[0] = 0;
			response[1] = 0;
			response[2] = 0;
			twi.writeRead(
				address,
				false, /* tenBitAddr */
				dummy,
				0,
				response,
				2
			);
			
			prom[i] = readU16BE(response, 0);
			if (prom[i] == 0) {
				zeros++;
			}
		}

		// TODO checksum
		if (zeros > 1) {
			App.log.i(App.TAG, "Read PROM with " + zeros + " zero(s)");
			// FIXME throw exception instead?
			return false;
		}

		C1s = ((long)prom[1]) * 32768L;
		C2s = ((long)prom[2]) * 65536L;
		C3	= prom[3];
		C4	= prom[4];
		C5s = prom[5] * 256;
		C6	= prom[6];
		
		App.log.i(App.TAG, "Reading PROM complete");

		return true;
	}
	
	private int readSensor() throws ConnectionLostException, InterruptedException {
		twi.writeRead(
			address,
			false, /* tenBitAddr */
			requestValue,
			requestValue.length,
			dummy,
			0
		);
		
		twi.writeRead(
			address,
			false, /* tenBitAddr */
			dummy,
			0,
			response,
			3
		);
		
		return readU24BE(response, 0);
	}
	
	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": twi=" + twiNum;
	}
	
	/*
	 * Device interface methods.
	 */

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException {
		twi = ioio.openTwiMaster(twiNum, TwiMaster.Rate.RATE_100KHz, false /* SMBus */);
		
		/*
		 * conversion times (ms)
		 * 
		 * OSR	max
		 * 4096	9.04
		 * 2048	4.54
		 * 1024	2.28
		 * 512	1.17
		 * 256	0.60
		 */
		
		sleep_time = 1000 / sample_rate - 11; // - conversion time
		if (sleep_time < 1) sleep_time = 1;
		
		try {
			readPROM();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int loop_count = 0;
	private int TEMP;
	private long dT;
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		// for some reason it does not work using shifts on long variables

		int D1, PRES;

		// start temp conversion but only every 10th loop
		if (loop_count % 10 == 0) {
			twi.writeRead(
				address,
				false, /* tenBitAddr */
				requestTemp,
				requestTemp.length,
				dummy,
				0
			);
			
			Thread.sleep(11);
			
			// read temp value and compute temperature
			dT = readSensor() - C5s;
			TEMP = (int)(2000L + ((dT * C6) >> 23));
		}

		// start pressure conversion
		twi.writeRead(address, false, requestPress, requestPress.length, dummy, 0);
		Thread.sleep(11);

		// read pressure value
		D1 = readSensor();

		long OFF = C2s + (C4 * dT) / 128L;
		long SENS = C1s + (C3 * dT) / 256L;

		int off2 = 0, sens2 = 0;

		if (TEMP < 2000) {
			// only pressure 2nd order effects implemented
			
			int d2 = TEMP - 2000; d2 = d2 * d2;
			off2 = (5 * d2) / 2;
			sens2 = (5 * d2) / 4;
			if (TEMP < -1500) {
				d2 = TEMP + 1500; d2 = d2 * d2;
				off2 += (7 * d2);
				sens2 += (11 * d2) / 2;
			}
		}
		
		OFF -= off2;
		SENS -= sens2;
		PRES = (int)(((D1 * SENS) / 2097152L /* 2^21 */ - OFF) / 32768L /* 2^15 */);

		float pressure = (float)PRES / 100f; // mbar
		float temperature = (float)TEMP / 100f; // degrees C
		
		if (listener != null) {
			listener.onMS5611Values(pressure, temperature);
		}

		loop_count++;

		Thread.sleep(sleep_time);
	}

}
