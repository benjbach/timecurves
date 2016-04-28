package fr.aviz.progresio.server.diff;



import java.util.ArrayList;


public interface Diff {

	/**
	 * The list of all currently available methods. Used by the JAnimationSettings panel.
	 */
	public static final String[] METHOD_NAMES =
		new String[] {"Best", "Pons", "Myers", "Ratcliff"};
	public static final Class[] METHODS =
		new Class[] {BestDiff.class, PonsDiff.class, MyersDiff.class, RatcliffDiff.class};
	
    /**
     * Operation types.
     * @author Jean-Daniel Fekete
     */
    public enum Type {
        /** Insertion operator */
        INSERT,
        /** Deletion operator */
        DELETE,
        /** Move operator */
        MOVE
    }

    /**
     * Detail of an operation.
     * 
     * @author Jean-Daniel Fekete
     */
    static public class Operation implements Comparable<Operation> {
        public Operation(Type type, int from, int length, int to, String contents) {
            this.type = type;
            this.from = from;
            this.length = length;
            this.to = to;
            this.contents = contents;
        }
        
        /**
         * Creates a deletion.
         * @param from the starting position
         * @param length the length
         */
        public Operation(int from, int length) {
            this(Type.DELETE, from, length, -1, null);
        }
        
        /**
         * Creates an insertion.
         * @param from the starting position
         * @param length the length
         * @param contents the contents
         */
        public Operation(int from, int length, String contents) {
            this(Type.INSERT, from, length, -1, contents);
            assert(length==contents.length());
        }
        
        /**
         * Creates a move.
         * @param from the starting position
         * @param length the length
         * @param to the destination
         */
        public Operation(int from, int length, int to) {
            this(Type.MOVE, from, length, to, null);
        }
        /** Operation type */
        public Type type;
        
        /** Starting position */
        public int from;
        
        /** Length */
        public int length;
        
        /** Destination from move, or -1 */
        public int to;
        
        /** Contents for insert or -1 */
        public String contents;
        
        public int compareTo(Operation op) {
        	if (from == op.from)
        		return type == Type.DELETE ? -1 : 1;
        	return from < op.from ? -1 : 1;
        }
        
        /**
         * {@inheritDoc}
         */
        public String toString() {
            switch(type) {
            case INSERT: return "insert("+from+","+length+","+contents+")";
            case DELETE: return "delete("+from+","+length+")";
            case MOVE:return "move("+from+","+length+","+to+")";
            default:
                throw new AssertionError("Unknown op" + type);
            }
        }

    }

    public Operation[] getDiffs();
    
}
