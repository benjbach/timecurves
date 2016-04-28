package fr.aviz.progresio.server.diff;



import java.util.ArrayList;
import java.util.Collections;


/**
 * Does not work yet.
 * 
 * @author dragice
 *
 */
public class CleanRatcliffDiff extends RatcliffDiff {

	String o;

	public CleanRatcliffDiff(String o, String n) {
		super (o, n);
		this.o = o;
	}

	public Operation[] getDiffs() {
		return unfragment(o, super.getDiffs(), 0.25f);
	}

	//////////////////////////////////////////////////////////////////////////
	
	/**
	 * Unfragment by merging groups of deletes or inserts that are separated by
	 * only a few characters. The threshold is ratio * the length of the smallest operation.
	 * 
	 * Not used. Hoped it would improve RatcliffDiff but it does not.
	 * 
	 * @param ops
	 * @return
	 */
	public static Operation[] unfragment(String o, Operation[] ops, float ratio) {

		Operation[] ops2;
		boolean change;
int n = 0;
		do {
n++;
			ops2 = forceOrphanEdit(o, ops, ratio);
			change = !DiffUtils.equals(ops, ops2);
			ops = ops2;
		} while (change);
System.err.println(n + " iterations");
		
		return ops2;
	}

	private static Operation[] forceOrphanEdit(String o, Operation[] ops, float ratio) {
		
		ArrayList<Operation> ops2 = new ArrayList<Operation>();

		// Find stable orphans and replace them by edits
		
		for (int i=0; i<ops.length; i++) {		
			// an insert followed by a delete?
			if (i < ops.length -1 && ops[i].type == Type.INSERT && ops[i+1].type == Type.DELETE) {
				int f1 = ops[i].from;
				int l1 = ops[i].length;
				int f2 = ops[i+1].from;
				int l2 = ops[i+1].length;
				int editLength = l1 + l2;
				int f_orphan = f1;
				int l_orphan = f2 - f1;
				if (i > 0 && ops[i-1].type == Type.DELETE && ops[i-1].from == f1) {
					f_orphan += ops[i-1].length;
					l_orphan -= ops[i-1].length;
				}
				if (l_orphan > 0 && l_orphan < editLength * ratio) {
					// delete then insert the whole thing including the orphan
					ops2.add(new Operation(f_orphan, l_orphan + l2));
					ops2.add(new Operation(f_orphan, l1 + l_orphan, ops[i].contents + o.substring(f_orphan, f_orphan + l_orphan)));
					i++;
				} else {
					ops2.add(ops[i]);
				}
			} else {
				ops2.add(ops[i]);
			}
		}
		
		Collections.sort(ops2);
		ops = (Operation[])ops2.toArray(new Operation[ops2.size()]);
		ops2.clear();
		
		// Fix inserts

		int f1 = -1, l1 = -1;
		for (int i=0; i<ops.length; i++) {
			if (ops[i].type == Type.DELETE) {
				f1 = ops[i].from;
				l1 = ops[i].length;
				ops2.add(ops[i]);
			} else if (ops[i].type == Type.INSERT && f1 != -1) {
				int f2 = ops[i].from;
				int l2 = ops[i].length;
				if (f2 >= f1 && f2 < f1+l2) {
					ops2.add(new Operation(f1, l2, ops[i].contents));
				} else {
					ops2.add(ops[i]);
				}
			} else {
				ops2.add(ops[i]);
			}
		}

		ops = (Operation[])ops2.toArray(new Operation[ops2.size()]);
		ops2.clear();

		// Put all deletes before inserts
		
		for (int i=0; i<ops.length; i++) {		
			if (ops[i].type == Type.DELETE) {
				ops2.add(ops[i]);
			}
		}
		for (int i=0; i<ops.length; i++) {		
			if (ops[i].type == Type.INSERT) {
				ops2.add(ops[i]);
			}
		}
		for (int i=0; i<ops.length; i++) {		
			if (ops[i].type == Type.MOVE) {
				ops2.add(ops[i]);
			}
		}
		
		ops = (Operation[])ops2.toArray(new Operation[ops2.size()]);
		
		// Merge contiguous deletes and inserts
		
		ops = mergeContiguousOperations(ops);
				
		return ops;
	}
	
	private static Operation[] mergeContiguousOperations(Operation[] ops) {
		
		ArrayList<Operation> ops2 = new ArrayList<Operation>();

		// Merge contiguous deletes and inserts
		
		boolean merged;
		do {
			ops2.clear();
			merged = false;
			for (int i=0; i<ops.length; i++) {		
				if (i < ops.length -1 && ops[i].type == ops[i+1].type && ops[i].type != Type.MOVE) {
					int f1 = ops[i].from;
					int l1 = ops[i].length;
					int f2 = ops[i+1].from;
					int l2 = ops[i+1].length;
					if (ops[i].type == Type.DELETE && f1 + l1 == f2) {
						ops2.add(new Operation(f1, l1+l2));
						i++;
						merged = true;
					} else if (ops[i].type == Type.INSERT && f1 == f2) {
						ops2.add(new Operation(f1, l1+l2, ops[i].contents + ops[i+1].contents));
						i++;
						merged = true;
					} else {
						ops2.add(ops[i]);
					}
				} else {
					ops2.add(ops[i]);
				}
			}
			ops = (Operation[])ops2.toArray(new Operation[ops2.size()]);
		} while (merged);

		
		Collections.sort(ops2);
		ops = (Operation[])ops2.toArray(new Operation[ops2.size()]);
		
		return ops;
	}
}
