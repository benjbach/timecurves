package fr.aviz.progresio.server.logs;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import weeny.util.logs.LineFormatter;


public class Logs {
	
	private static Map<String, Level> levelMapper = new HashMap();
	
	private static Map<String, Logger> loggerMapper = new HashMap();
	
	/**
	 * Change the level of the log related to the given class name
	 */
	public static void setLevel(String className, Level level){
		levelMapper.put(className, level);
	}
	
	/**
	 * Change the level of the log related to the given class
	 */
	public static void setLevel(Class cla, Level level){
		levelMapper.put(cla.getName(), level);
	}
	
	/**
	 * Get the log related to the given class
	 * @see #log(String)
	 */
	public static Logger log(Class cla){
		return log(cla.getName());
	}
	
	/**
	 * Get the log related to the given class name. By default, the log has the
	 * system default level, and use the the formatter {@link LineFormatter}.
	 * And, you can change the level by calling {@link #setLevel(Class, Level)}.
	 */
	public static Logger log(String className){
		if (loggerMapper.containsKey(className)) {
			Logger logger = loggerMapper.get(className);
			if (levelMapper.containsKey(className)) {
				logger.setLevel(levelMapper.get(className));
			}			
			return logger;
		}
		Logger logger = Logger.getLogger(className);
		logger.setUseParentHandlers(false);
		for(Handler h : logger.getHandlers()){
			logger.removeHandler(h);
		}
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new LineFormatter());
		logger.addHandler(handler);
		
		loggerMapper.put(className, logger);
		return logger;
	}
}
