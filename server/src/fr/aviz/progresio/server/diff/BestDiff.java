package fr.aviz.progresio.server.diff;



import java.lang.reflect.Constructor;


/**
 * 
 * This uses PonsDiff and delegates to MyersDiff when the result is not correct,
 * which currently happens when text moves.
 * 
 * @author dragice
 *
 */
public class BestDiff implements Diff {

	Operation[] ops;
	String output;

	public BestDiff(String o, String n) {

		output = "";

		if (!tryMethod(PonsDiff.class, o, n)) {
//			if (!tryMethod(CleanRatcliffDiff.class, o, n))
				if (!tryMethod(MyersDiff.class, o, n))
					tryMethod(RatcliffDiff.class, o, n);
			System.err.println(output);
		}
	}

	public boolean tryMethod(Class method, String o, String n) {
		output += "Trying " + method.getSimpleName();
		try {
			Constructor constructor = method.getConstructor(new Class[]{String.class, String.class});
			Diff diff = (Diff)constructor.newInstance(o, n);
			ops = diff.getDiffs();
			if (!DiffChecker.check(o, n, ops)) { 
				output += ": diff incorrect -> ";
				return false;
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			output += ": got an exception -> ";
			return false;
		}
		output += ": OK.";
		return true;
	}

	@Override
	public Operation[] getDiffs() {
		return ops;
	}

	public String toString() {
		return DiffUtils.toString(getDiffs());
	}

}
