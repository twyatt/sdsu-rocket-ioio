package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.TwiMaster.Rate;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Barometer
 * 
 * High resolution module, 10 cm
 * Supply voltage 1.8 to 3.6 V
 * Operating range: 10 to 1200 mbar, -40 to +85 C
 * I2C interface up to 20 Mhz
 * 
 * The MS5611-01BA has only five basic commands:
 *   1. Reset
 *   2. Read PROM (128 bit of calibration words)
 *   3. D1 conversion
 *   4. D2 conversion
 *   5. Read ADC result (24 bit pressure / temperature)
 * 
 * http://www.embeddedadventures.com/barometric_pressure_sensor_module_mod-1009.html
 */
public class MS5611 extends DeviceAdapter {
	
	public static class ConversionTime {
		public final float min; // minimum conversion time (ms)
		public final float typ; // typical conversion time (ms)
		public final float max; // maximum conversion time (ms)
		public ConversionTime(float min, float typ, float max) {
			this.min = min;
			this.typ = typ;
			this.max = max;
		}
	}

	public enum OversamplingRatio {
		OSR_4096(CONVERT_D1_OSR_4096, CONVERT_D2_OSR_4096, new ConversionTime(7.40f, 8.22f, 9.04f)),
		OSR_2048(CONVERT_D1_OSR_2048, CONVERT_D2_OSR_2048, new ConversionTime(3.72f, 4.13f, 4.54f)),
		OSR_1024(CONVERT_D1_OSR_1024, CONVERT_D2_OSR_1024, new ConversionTime(1.88f, 2.08f, 2.28f)),
		OSR_512 (CONVERT_D1_OSR_512,  CONVERT_D2_OSR_512,  new ConversionTime(0.95f, 1.06f, 1.17f)),
		OSR_256 (CONVERT_D1_OSR_256,  CONVERT_D2_OSR_256,  new ConversionTime(0.48f, 0.54f, 0.60f)),
		;
		public final byte D1;
		public final byte D2;
		public final ConversionTime time;
		OversamplingRatio(byte D1, byte D2, ConversionTime time) {
			this.D1 = D1;
			this.D2 = D2;
			this.time = time;
		}
	}
	
	public interface MS5611Listener {
		public void onData(int P /* mbar */, int TEMP /* C */);
		public void onError(String message);
	}
	
	private MS5611Listener listener;
	
	/**
	 * Duration to sleep thread after a reset command, per AN520 example C-code.
	 */
	public static final long RESET_DELAY = 3L; // ms
	
	public static final byte CSB_LOW  = 0x00; // CSB to GND
	public static final byte CSB_HIGH = 0x01; // CSB to VCC
	
	/**
	 * CSB, chip select
	 */
	public static final byte CSB = CSB_LOW;
	
	/**
	 * Address, where C is the complementary value of the pin CSB.
	 */
	public static final byte ADDRESS = 0x76 | CSB; // 0111011C
	
	/**
	 * The reset can be sent at any time. In the event that there is not a
	 * successful power on reset this may be caused by the SDA being blocked by
	 * the module in the acknowledge state. The only way to get the MS5611-01BA
	 * to function is to send several SCLKs followed by a reset sequence or to
	 * repeat power on reset.
	 */
	public static final byte RESET_SEQUENCE = 0x1E;
	
	/**
	 * A conversion can be started by sending the command to MS5611-01BA. When
	 * command is sent to the system it stays busy until conversion is done.
	 * When conversion is finished the data can be accessed by sending a Read
	 * command, when an acknowledge appears from the MS5611-01BA, 24 SCLK cycles
	 * may be sent to receive all result bits. Every 8 bit the system waits for
	 * an acknowledge signal.
	 */
	public static final byte CONVERT_D1_OSR_256  = (byte) 0x40;
	public static final byte CONVERT_D1_OSR_512  = (byte) 0x42;
	public static final byte CONVERT_D1_OSR_1024 = (byte) 0x44;
	public static final byte CONVERT_D1_OSR_2048 = (byte) 0x46;
	public static final byte CONVERT_D1_OSR_4096 = (byte) 0x48;
	public static final byte CONVERT_D2_OSR_256  = (byte) 0x50;
	public static final byte CONVERT_D2_OSR_512  = (byte) 0x52;
	public static final byte CONVERT_D2_OSR_1024 = (byte) 0x54;
	public static final byte CONVERT_D2_OSR_2048 = (byte) 0x56;
	public static final byte CONVERT_D2_OSR_4096 = (byte) 0x58;
	
	public static final byte ADC_READ = (byte) 0x00;
	
	public static final byte PROM_READ_SEQUENCE = (byte) 0xA0; // 1010[Ad2][Ad1][Ad0]0
	
	private static final int READ_BUFFER_SIZE  = 4; // byte(s)
	private static final int WRITE_BUFFER_SIZE = 1; // byte(s)
	
	private byte[] readBuffer  = new byte[READ_BUFFER_SIZE];
	private byte[] writeBuffer = new byte[WRITE_BUFFER_SIZE];
	
	private long C1; // C1
	private long C2; // C2
	private long C3; // C3
	private long C4; // C4
	private long C5; // C5
	private long C6; // C6
	private int CRC;
	
	private long SENS_T1;
	private long OFF_T1;
	private long TCS;
	private long TCO;
	private long T_REF;
	
	private int D1; // 24 bit unsigned int
	private int D2; // 24 bit unsigned int
	
	private long conversionDelay;
	
	private int twiNum;
	private Rate rate;
	private OversamplingRatio osr;
	private TwiMaster twi;

	private volatile int TEMP;
	private volatile int P;

	/**
	 * CRC4 algorithm ported from AN520 example C-code.
	 * 
	 * @param prom
	 * @return
	 */
	public static byte crc4(int prom[]) {
		int cnt;      // simple counter
		int n_rem;    // crc reminder
		int crc_read; // original value of the crc
		byte n_bit;
		
		n_rem = 0x00;
		crc_read = prom[7]; // save read CRC
		prom[7] &= 0xFF00;  // CRC byte is replaced by 0
		
		for (cnt = 0; cnt < 16; cnt++) { // operation is performed on bytes
			// choose LSB or MSB
			if (cnt % 2 == 1) {
				n_rem ^= prom[cnt >> 1] & 0x00FF;
			} else {
				n_rem ^= prom[cnt >> 1] >> 8;
			}
			
			for (n_bit = 8; n_bit > 0; n_bit--) {
				if ((n_rem & 0x8000) == 0x8000) {
					n_rem = (n_rem << 1) ^ 0x3000;
				} else {
					n_rem <<= 1;
				}
			}
		}
		
		n_rem = 0x000F & (n_rem >> 12); // final 4-bit reminder is CRC code
		prom[7] = crc_read; // restore the crc_read to its original place
		return (byte) (n_rem ^ 0x00);
	}
	
	/**
	 * Method that verifies that the CRC function has been implemented properly.
	 * 
	 * @return True if CRC function works as expected, false otherwise.
	 */
	public static boolean testCRC() {
		int prom[] = { 0x3132, 0x3334, 0x3536, 0x3738, 0x3940, 0x4142, 0x4344, 0x4500 };
		return (crc4(prom) == 0x0B);
	}

	public MS5611(int twiNum, Rate rate, OversamplingRatio osr) {
		this.twiNum = twiNum;
		this.rate = rate;
		setOversamplingRatio(osr);
	}
	
	public MS5611 setListener(MS5611Listener listener) {
		this.listener = listener;
		return this;
	}
	
	public MS5611 setOversamplingRatio(OversamplingRatio osr) {
		this.osr = osr;
		conversionDelay = (long) Math.ceil(osr.time.max);
		return this;
	}
	
	/**
	 * Returns the MS5611's internal temperature. The returned int must be
	 * divided by 100 to get the actual temperature.
	 * 
	 * @return Temperature in C.
	 */
	public int getTemperature() {
		return TEMP;
	}
	
	/**
	 * Returns the MS5611's pressure reading. The returned int must be
	 * divided by 100 to get the actual pressure.
	 * 
	 * @return Pressure in mbar.
	 */
	public int getPressure() {
		return P;
	}
	
	protected void write(byte command) throws ConnectionLostException, InterruptedException {
		writeBuffer[0] = command;
		flush(1);
	}
	
	protected void write(byte command, byte[] values) throws ConnectionLostException, InterruptedException {
		writeBuffer[0] = command;
		System.arraycopy(values, 0, writeBuffer, 1, values.length);
		flush(values.length);
	}
	
	/**
	 * Writes the write buffer to the SPI.
	 * 
	 * @param length Number of bytes of the buffer to write.
	 * @throws ConnectionLostException
	 * @throws InterruptedException
	 */
	protected void flush(int length) throws ConnectionLostException, InterruptedException {
		int writeSize = length;
		int readSize = 0;
		twi.writeRead(ADDRESS, false, writeBuffer, writeSize, readBuffer, readSize);
	}
	
	protected void read(byte command, int length, byte[] values) throws ConnectionLostException, InterruptedException {
	    writeBuffer[0] = command;
		int writeSize = 1;
		int readSize = length;
		twi.writeRead(ADDRESS, false, writeBuffer, writeSize, values, readSize);
	}
	
	private void reset() throws ConnectionLostException, InterruptedException {
		write(RESET_SEQUENCE);
		Thread.sleep(RESET_DELAY);
	}
	
	private void calibrate() throws ConnectionLostException, InterruptedException {
		int[] prom = readPROM();
		
		C1 = prom[1];
		C2 = prom[2];
		C3 = prom[3];
		C4 = prom[4];
		C5 = prom[5];
		C6 = prom[6];
		CRC = prom[7] & 0x0F;
		
		byte crc4 = crc4(prom);
		if (crc4 != CRC) {
			onError(this.getClass().getSimpleName() + " PROM CRC mismatch, " + CRC + " (read) != " + crc4 + " (calculated).");
		}
		
		SENS_T1 = C1 * 32768 /* 2^15 */;
		OFF_T1  = C2 * 65536 /* 2^16 */;
		TCS     = C3 / 256 /* 2^8 */;
		TCO     = C4 / 128 /* 2^7 */;
		T_REF   = C5 * 256 /* 2^8 */;
	}
	
	private void conversion() throws ConnectionLostException, InterruptedException {
		write(osr.D2);
		Thread.sleep(conversionDelay);
		read(ADC_READ, 3 /* 24 bit */, readBuffer);
		D2 = ((readBuffer[0] & 0xFF) << 16) |
			 ((readBuffer[1] & 0xFF) <<  8) |
			 ((readBuffer[2] & 0xFF) <<  0);
		
		write(osr.D1);
		Thread.sleep(conversionDelay);
		read(ADC_READ, 3 /* 24 bit */, readBuffer);
		D1 = ((readBuffer[0] & 0xFF) << 16) |
			 ((readBuffer[1] & 0xFF) <<  8) |
			 ((readBuffer[2] & 0xFF) <<  0);
	}
	
	/**
	 * Reads the MS5611 PROM.
	 * 
	 * prom[0] = 16 bit reserved for manufacturer
	 * prom[1] = Coefficient 1 (16 bit unsigned)
	 * prom[2] = Coefficient 2 (16 bit unsigned)
	 * prom[3] = Coefficient 3 (16 bit unsigned)
	 * prom[4] = Coefficient 4 (16 bit unsigned)
	 * prom[5] = Coefficient 5 (16 bit unsigned)
	 * prom[6] = Coefficient 6 (16 bit unsigned)
	 * prom[7] = 4 bit CRC
	 * 
	 * @throws InterruptedException 
	 * @throws ConnectionLostException 
	 */
	private int[] readPROM() throws ConnectionLostException, InterruptedException {
		int[] prom = new int[8];
		for (int i = 0; i < 8; i++) {
			read((byte) (PROM_READ_SEQUENCE + i * 2), 2, readBuffer);
			prom[i] = ((readBuffer[0] & 0xFF) << 8) |
					  ((readBuffer[1] & 0xFF) << 0);
		}
		return prom;
	}
	
	private void onError(String message) {
		if (listener != null) {
			listener.onError(message);
		}
	}
	
	/*
	 * IOIOLooper interface methods.
	 */
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		twi = ioio.openTwiMaster(twiNum, rate, false /* SMBus */);
		reset();
		calibrate();
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		conversion();
		
		long dT   = D2 - T_REF;
		long OFF  = OFF_T1  + dT * TCO;
		long SENS = SENS_T1 + dT * TCS;
		
		TEMP = 2000 + (int) (dT * C6 / 8388608 /* 2^23 */);
		P = (int) ((D1 * SENS / 2097152 /* 2^21 */ - OFF) / 32768 /* 2^15 */);
		
		if (listener != null) {
			listener.onData(P, TEMP);
		}
		
		// TODO sleep - (long) Math.ceil(osr.time.max) * 2
		super.loop();
	}

	@Override
	public String info() {
		return this.getClass().getSimpleName() + ": twi=" + twiNum;
	}

}
