package fr.aviz.progresio.server.logs;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Debug {
	
	/**
	 * A global flag: should we show "file:line#" indicating where the info is 
	 * displayed.
	 */	
	public static boolean SHOW_LINE_NUM = true;
	
	/**
	 * A global flag: should we even need to show all the debug info?
	 */
	public static boolean SHOW_DEBUG = true;

	/**
	 * 
	 */
	public static int   PRIORITY = 1;
	
	public static void tag(int indent, String type, String message){
		print(indent, type, message.toString(), System.out);
	}

	public static final String  LOG = " LOG";
	public static final String INFO = "INFO";
	public static final String PROB = "PROB";
	
	public static final List<String> PRIORITY_LIST = new ArrayList<String>();
	static{
		PRIORITY_LIST.add(LOG);
		PRIORITY_LIST.add(INFO);
		PRIORITY_LIST.add(PROB);
	}
	
	public static void msg(Object msg){
		msg(0, msg);
	}
	public static void log(Object msg){
		log(0, msg);
	}
	public static void info(Object msg){
		info(0, msg);
	}
	public static void prob(Object msg){
		prob(0, msg);
	}
	public static void msg(int indent, Object msg){
		print(indent,   null, msg, System.out);
	}
	public static void log(int indent, Object msg){
		print(indent,  LOG, msg, System.out);
	}
	public static void info(int indent, Object msg){
		print(indent, INFO, msg, System.out);
	}
	public static void prob(int indent, Object msg){
		print(indent, PROB, msg, System.err);
	}
	public static String line(String message){
		return line(0,null,message);
	}
	public static String line(int indent, String type, String message){
		if (PRIORITY_LIST.indexOf(type) <PRIORITY) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<indent; i++){
			sb.append("[    ]");
		}
		if (type != null) {
			sb.append("["+type+"]");
		}
		sb.append(message);
		if (SHOW_LINE_NUM) {
			try {
				throw new IllegalStateException("");
			}
			catch (Exception ex) {
				int length = 50;
				while (length < message.length()){
					length += 25;
				}
				if (message.length() < length) {
					for (int i = message.length(); i< length; i++){
						sb.append(" ");
					}
				}
				int index = 0;
				for (index = 0; index<ex.getStackTrace().length; index++){
					StackTraceElement ste = ex.getStackTrace()[index];
					if (!ste.getFileName().equals("Debug.java")) {						
						break;
					}
				}
				StackTraceElement e = ex.getStackTrace()[index];
				sb.append("\tat ("+e.getFileName()+":"+e.getLineNumber()+")");
			}
		}
		return sb.toString();
	}
	private static void print(int tabs, String tag, Object msg, PrintStream ps){
		if (SHOW_DEBUG) {
			String line = line(tabs, tag, msg==null? "null":msg.toString());
			if (line == null) {
				return;
			}
			else {
				ps.println(line);	
			}
		}
	}
}