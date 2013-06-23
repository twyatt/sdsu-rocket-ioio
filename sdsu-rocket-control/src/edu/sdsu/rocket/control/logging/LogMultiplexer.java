package edu.sdsu.rocket.control.logging;

import java.util.ArrayList;
import java.util.List;

import edu.sdsu.rocket.logging.Logger;

public class LogMultiplexer implements Logger {
	
	List<Logger> loggers = new ArrayList<Logger>();
	
	public LogMultiplexer(Logger ... loggers) {
		for (Logger logger : loggers) {
			this.loggers.add(logger);
		}
	}
	
	public void addLogger(Logger logger) {
		loggers.add(logger);
	}
	
	public List<Logger> getLoggers() {
		return loggers;
	}

	@Override
	public void i(String tag, String msg) {
		for (Logger logger : loggers) {
			logger.i(tag, msg);
		}
	}

	@Override
	public void e(String tag, String msg) {
		for (Logger logger : loggers) {
			logger.e(tag, msg);
		}
	}

	@Override
	public void e(String tag, String msg, Exception e) {
		for (Logger logger : loggers) {
			logger.e(tag, msg, e);
		}
	}

}
