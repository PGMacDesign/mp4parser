package com;

public class LoggingCore {
	
	private static boolean shouldLog;
	private static LoggingCore instance;
	
	public static LoggingCore getInstance(){
		if(instance == null){
			instance = new LoggingCore();
		}
		return instance;
	}
	
	private LoggingCore(){
		shouldLog = true;
	}
	
	public static boolean getShouldLog() {
		getInstance();
		return shouldLog;
	}
	
	public static void setShouldLog(boolean shouldLog) {
		getInstance();
		LoggingCore.shouldLog = shouldLog;
	}
}
