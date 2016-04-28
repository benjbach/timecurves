package fr.aviz.progresio.server.diff;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;

import fr.aviz.progresio.server.diff.Diff.Operation;
import fr.aviz.progresio.server.diff.Diff.Type;

public class DiffUtils {

	public static Diff createDiff(Class diffMethod, String textv1, String textv2) {
		try {
			Constructor constructor = diffMethod.getConstructor(new Class[]{String.class, String.class});
			return (Diff)constructor.newInstance(textv1, textv2);
		} catch (Exception e) {
			e.printStackTrace();
			return new PonsDiff(textv1, textv2);
		}
	}

	public static String[] getShortestDiffPath(String[] files, Class diffMethod) {

		//System.err.println("Finding shortest diff path...");

		ArrayList<String> shortestPath = new ArrayList<String>();

		int currentFile = files.length - 1;
		shortestPath.add(files[currentFile]);

		while (currentFile > 0) {
			int minEdits = Integer.MAX_VALUE;
			int closest_i = currentFile-1;
			String n = readFile(files[currentFile]);

			for (int i = currentFile-1; i>=0; i--) {
				String o = readFile(files[i]);
				if (Math.abs(o.length() - n.length()) <= minEdits) {
					Diff diff = createDiff(diffMethod, o, n);
					int editcount = getEditDistance(diff.getDiffs());
					if (editcount < minEdits || editcount == 0) {
						minEdits = editcount;
						closest_i = i;
					}
				}
			}
			currentFile = closest_i;
			shortestPath.add(0, files[currentFile]);
		}

		//System.err.println("\nDone.");

		return (String[])(shortestPath.toArray(new String[shortestPath.size()]));
	} 

	public static int getEditDistance(Operation[] ops) {
		int count = 0;
		for (int i=0; i<ops.length; i++)
			count += ops[i].length;
		return count;
	}

	public static String toString(Operation[] ops) {
		String s = "";
		for (Operation op : ops) {
			s += op.toString() + "\n";
		}
		return s;	
	}

	public static boolean equals(Operation[] ops1, Operation[] ops2) {
		if (ops1.length != ops2.length)
			return false;
		for (int i=0; i<ops1.length; i++) {
			if (ops1[i].type != ops1[i].type)
				return false;
			if (ops1[i].from != ops1[i].from)
				return false;
			if (ops1[i].length != ops1[i].length)
				return false;
		}
		return true;
	}

	// span[] contains the span in v1 and will return the new span in v2
	// edits[0] is incremented with the number of characters deleted in the span
	// edits[1] is incremented with the number of characters inserted in the span
	// edits[2] equals 0 if all edits are internal to the span
	// Returns the new string, or null if the diff failed.
	public static String trackText(String v1, String v2, int[] span, int[] edits, Class diffMethod) {
		
//System.err.print(span[0] + " " + span[1] + " -> ");
		
		int[] span0 = new int[]{span[0], span[1]};
		edits[2] = 0;
		
		Diff diff = createDiff(diffMethod, v1, v2);
		Operation[] ops = diff.getDiffs();
		if (!DiffChecker.check(v1, v2, ops))
			return null;
		for (int i=0; i<ops.length; i++) {
			Operation op = ops[i];
			if (op.type == Type.MOVE)
				return null;
			if (op.type == Type.INSERT) {
				if (op.from <= span0[0]) {
					span[0] += op.length;
					span[1] += op.length;
//System.out.println("         MOVED +" + op.length + " chars");
				} else if (op.from <= span0[1]) {
					span[1] += op.length;
					edits[1] += op.length;
					if (op.from == span0[0] || op.from >= span0[1] - 2) { // -2 is a tweak for sentences
						edits[2] = 1;
					}
//System.out.println("         INSERTED " + op.length + " chars (" + ")   [" + span[0] + " " + span[1] + " I " + op.from + "]");					
				}		
			}
			if (op.type == Type.DELETE) {
				/*if (op.from + op.length - 1 < span0[0]) {
					span[0] -= op.length;
					span[1] -= op.length;
				} else {*/
				// compute the left complement and maybe translate the span
				if (op.from < span0[0]) {
					int i0 = op.from;
					int i1 = Math.min(op.from + op.length - 1, span0[0] - 1);
					span[0] -= i1 - i0 + 1;
					span[1] -= i1 - i0 + 1;
//System.out.println("         MOVED -" + (i1-i0+1) + " chars");
				}
				// compute the intersection and maybe shorten the span
				int i0 = Math.max(op.from, span0[0]);
				int i1 = Math.min(op.from + op.length - 1, span0[1]);
				if (i1 >= i0) {
					if (i0 >= span0[0] && i0 <= span0[1] && i1 >= span0[0] && i1 <= span0[1]) {
						span[1] -= i1 - i0 + 1;
						edits[0] += i1 - i0 + 1;
//System.out.println("         DELETED " + (i1 - i0 + 1) + " chars");
						//}
					}
					if (i0 == span0[0] || i1 == span0[1]) {
//	System.err.println("n-i " + span0[0] + "," + span0[1] + " " + i0 + "," + i1);
						edits[2] = 1;
					}
				}
			}
		}
		
		if (span[1] <= span[0]) {
			span[0] = span[1] = 0;
		}
		
		if (span[0] >= v2.length() || span[1] >= v2.length()) 
			return null;
		
//System.err.println(span[0] + " " + span[1] + " I " + v2.length());
		return v2.substring(span[0], span[1]+1);
	}
	
	public static String readFile(String filename) {
		String text="";
		try{
			InputStream ips=new FileInputStream(filename); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String line;
			while ((line=br.readLine())!=null){
				text+= line + "\n";
			}
			br.close(); 
		}		
		catch (Exception e){
			System.out.println(e.toString());
			return null;
		}
		return text;
	}
}
