package fr.aviz.progresio.server.logs;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * 
 * This formatter simply display "[LEVEL]blabla... (file:line#)".
 * 
 * @author weeny
 * 
 */
public class LineFormatter extends SimpleFormatter{
	@Override
	public synchronized String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(record.getLevel().getLocalizedName()).append("]");
		sb.append(formatMessage(record));

		StackTraceElement[] stack = new Exception().getStackTrace();
		for (int index = 4; index<stack.length; index++) {
			StackTraceElement ste = stack[index];
			if (ste.getFileName() == null) {
				//in case the source files are not linked
				continue;
			}
			else if (!ste.getFileName().startsWith("Logger.java")) {
				sb.append(" (").append(ste.getFileName()).append(":");
				sb.append(ste.getLineNumber()).append(")");
				break;
			}
		}
		sb.append("\n");
		return sb.toString();
	}
}
