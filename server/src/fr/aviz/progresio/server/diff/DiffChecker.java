package fr.aviz.progresio.server.diff;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.aviz.progresio.server.diff.Diff.Operation;
import fr.aviz.progresio.server.diff.Diff.Type;

public class DiffChecker {
	
	/**
	 * FIXME: Does not take moves into account.
	 * @param o
	 * @param n
	 * @param ops
	 * @return
	 */
	public static boolean check(String o, String n, Operation[] ops) {
		String o2 = applyOperations(o, ops);
		return n.equals(o2);
	}
	
	public static String applyOperations(String s, Operation[] ops) {
		
		String s2 = "";
		int index = 0;
		
		for (int i=0; i<ops.length; i++) {
			Operation op = ops[i];
			if (op.from > index) {
				s2 += s.substring(index, op.from);
				index = op.from;
			}
			if (op.type == Type.INSERT) {
				s2 += op.contents;
			} else if (op.type == Type.DELETE) {
				index += op.length;
			} else if (op.type == Type.MOVE) {
				// NOT SUPPORTED
			}
		}
		if (index >= 0 && index < s.length())
			s2 += s.substring(index);
		
		return s2;
	}
	
	///////////////////// LOG
	
	static int counter = 0;
	static {
		(new File("./data/debug/differrors/")).mkdir();
		//clearFolder("./data/debug/differrors/");
	}
	
	public static void logDiffError(String info, String o, String n, Operation[] ops) {
		Date now = new Date();
		info += "\n\n";
		log(now, info + n);
		counter ++;
		log(now, info + applyOperations(o, ops));
		counter += 9;
	}
	
	private static void log(Date now, String s) {
		try {
	        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd'T'HHmmss");
	        String time = sdf.format( now );
	        PrintWriter out = new PrintWriter(new FileWriter("./data/debug/differrors/" + time + "_" + counter + ".txt"));
	        out.print(s);
	        out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Clears folder from the mess we make
	public static void clearFolder(String strFolder)
	{
		
		try {
			// Declare variables
			File fLogDir = new File(strFolder);
	
			// Get all BCS files
			File[] fLogs = fLogDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return true;
				}
			});
	
			// Delete all files
			if (fLogs == null)
				return;
			for (int i = 0; i < fLogs.length; i++)
			{
				new File((fLogs[i].getAbsolutePath())).delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
