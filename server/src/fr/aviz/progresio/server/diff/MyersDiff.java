package fr.aviz.progresio.server.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import fr.aviz.progresio.server.diff.diff_match_patch.*;

/**
 * A wrapper to Google's diff code (see diff_match_patch.java) using Myer's diff algorithm:
 * 
 *   E. Myers (1986). "An O(ND) Difference Algorithm and Its Variations".
 *   Algorithmica 1 (2): 251���266.
 * 
 * @author dragice
 *
 */
public class MyersDiff implements Diff {

	static diff_match_patch dmp = null;
	
    ArrayList<Operation> details = new ArrayList<Operation>();

    /**
     * Computes the difference between two strings.
     * @param o the old string
     * @param n the new string
     */
    public MyersDiff(String o, String n) {
    	if (dmp == null) {
    		dmp = new diff_match_patch();
    		dmp.Diff_Timeout = 1;
    		//dmp.Diff_EditCost = 15;
    		//dmp.Match_Distance = 30000;
    		//dmp.Match_Threshold = 0.85f;
    		//dmp.Patch_Margin = 0;
    	}
    	build(o,n);
    }

    public void build(String o, String n) {
    	
    	details.clear();
    	
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(o, n, true);
        if (diffs.size() > 2) {
        	dmp.diff_cleanupSemantic(diffs);
        	dmp.diff_cleanupEfficiency(diffs);
        }
        LinkedList<diff_match_patch.Patch> ops = dmp.patch_make(o, diffs);
    	
    	int offset = 0;
   
    	for (diff_match_patch.Patch p : ops) {
        	int i1 = p.start1 + offset, length;
        	for (diff_match_patch.Diff d : p.diffs) {
        		length = d.text.length();
				if (d.operation == diff_match_patch.Operation.EQUAL) {
					i1 += length;
				} else if (d.operation == diff_match_patch.Operation.DELETE) {
					Operation newop = new Operation(Type.DELETE, i1, length, -1, null);
					details.add(newop);
					i1 += length;
				} else if (d.operation == diff_match_patch.Operation.INSERT) {
					String content = d.text;
					Operation newop = new Operation(Type.INSERT, i1, length, -1, content);
					details.add(newop);
				}
        	}
        	offset += p.length1 - p.length2;
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

    	MyersDiff diff2 = new MyersDiff(o, n);
    	System.out.println(diff2);
    	System.out.println("Correct: " + DiffChecker.check(o, n, diff2.getDiffs()));
    	//System.out.println(DiffChecker.applyOperations(o, diff1.getDiffs()));

    }

}
