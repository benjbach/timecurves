package fr.aviz.progresio.server.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.python.util.PythonInterpreter;
import org.python.core.*;


/**
 * Wrapper to the Python difflib based on Ratcliff/Obershelp pattern matching algorithm:
 * 
 *   John W. Ratcliff and David Metzener, Pattern Matching: The Gestalt Approach,
 *   Dr. Dobb's Journal, page 46, July 1988. 
 * 
 * From Python source code:
 * 
 * SequenceMatcher is a flexible class for comparing pairs of sequences of
 * any type, so long as the sequence elements are hashable.  The basic
 * algorithm predates, and is a little fancier than, an algorithm
 * published in the late 1980's by Ratcliff and Obershelp under the
 * hyperbolic name "gestalt pattern matching".  The basic idea is to find
 * the longest contiguous matching subsequence that contains no "junk"
 * elements (R-O doesn't address junk).  The same idea is then applied
 * recursively to the pieces of the sequences to the left and to the right
 * of the matching subsequence.  This does not yield minimal edit
 * sequences, but does tend to yield matches that "look right" to people.
 * 
 * @author dragice
 *
 */
public class RatcliffDiff implements Diff {

	static PythonInterpreter python = null;
	
    ArrayList<Operation> details = new ArrayList<Operation>();

    /**
     * Computes the difference between two strings.
     * @param o the old string
     * @param n the new string
     */
    public RatcliffDiff(String o, String n) {
    	if (python == null) {
    		python = new PythonInterpreter();
    		python.exec("from difflib import SequenceMatcher");
    	}
    	build(o,n);
    }

    private static String escape(String s) {
		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		s = s.replace("\n", "\\n");
		s = s.replace("\t", "\\t");
		return s;
    }
    
    private static void exec(String code) {
//System.out.println(code);
    	python.exec(code);
    }
    
    public void build(String o, String n) {
    	
		exec("o = \"" + escape(o) + "\"");
		exec("n = \"" + escape(n) + "\"");
		String lambda = "lambda x: x == \"\\n\""; // "None";
		exec("opcodes = SequenceMatcher(" + lambda + ", o, n).get_opcodes()");
		
//		String code = "opcodes = SequenceMatcher(lambda x: x == \" \", \"" + o_ + "\", \"" + n_ + "\").get_opcodes()";
		PyObject opcodes = python.get("opcodes");
	
		details.clear();
		PyObject op;
		for (Iterator<PyObject> it = opcodes.asIterable().iterator(); it.hasNext(); ) {
			op = it.next();
			for (Iterator<PyObject> it2 = op.asIterable().iterator(); it2.hasNext(); ) {
				String opType = it2.next().asString();
				int i1 = it2.next().asInt();
				int i2 = it2.next().asInt();
				int j1 = it2.next().asInt();
				int j2 = it2.next().asInt();
				if (opType.equals("equal")) {
					// do nothing
				} else if (opType.equals("delete")) {
					Operation newop = new Operation(Type.DELETE, i1, i2-i1, -1, null);
					details.add(newop);
				} else if (opType.equals("insert")) {
					String content = n.substring(j1, j2);
					Operation newop = new Operation(Type.INSERT, i1, j2-j1, -1, content);
					details.add(newop);
				} else if (opType.equals("replace")) {
					Operation newop = new Operation(Type.DELETE, i1, i2-i1, -1, null);
					details.add(newop);
					String content = n.substring(j1, j2);
					newop = new Operation(Type.INSERT, i1, j2-j1, -1, content);
					details.add(newop);
				}
			}
		}
    	
        Collections.sort(details);

    }
    
    public Operation[] getDiffs() {
        Operation[] ops = new Operation[details.size()];
        return details.toArray(ops);
    }
    
	public String toString() {
		return DiffUtils.toString(getDiffs());
	}

    // TEST
    public static void main(String[] args) {
    	String o = "The last triple is a DELETE dummy, and has the v��lue BLAH";
    	String n = "The last triple INSERT is a dummy, and has REPLACE v��lue";
    	
    	PonsDiff diff1 = new PonsDiff(o, n);
    	System.out.println(diff1);
    	//System.out.println(DiffChecker.applyOperations(o, diff1.getDiffs()));
    	System.out.println("Correct: " + DiffChecker.check(o, n, diff1.getDiffs()));
    	System.out.println();

    	RatcliffDiff diff2 = new RatcliffDiff(o, n);
    	System.out.println(diff2);
    	System.out.println("Correct: " + DiffChecker.check(o, n, diff2.getDiffs()));
    	//System.out.println(DiffChecker.applyOperations(o, diff1.getDiffs()));

    }

}
