package fr.aviz.progresio.server.datastructure;

/**
 * @author conglei_shi(conglei.org)
 * 
 * This class is used for gson to serialize the class to json file 
 */
import java.util.ArrayList;

public class VersionGraph {
	
	public static class Node {
		/**
		 * The name of the node
		 */
		String n;
		
		/**
		 * The timestamp for each version
		 */
		long t;
				
		/** Node order according to similarity ordering (BarJoseph)
		 * 
		 */
		int orderPos;
		
		/** 
		 * The x coordinate for the mds result
		 */
		double x;
		
		/**
		 * the y coordinate for the mds result
		 */
		double y;
		
		/** User of wikipedia revision*/
		String user;
		public String getUser() { return user; }
		public void setUser(String user) { this.user = user;}

		/** Comment for wikipedia revision*/
		String comment;
		public String getComment() { return comment; }
		public void setComment(String comment) { this.comment = comment; }
		
		/** Size of wikipedia revision. bbach: not 100% sure what that means*/
		String revSize;
		public String getRevSize() { return revSize; }
		public void setRevSize(String size) { this.revSize = size; }
		String length;
		public void setLength(String length) { this.length = length; }
				
	
		
		public double getMdsX() {
			return x;
		}

		public void setMdsX(double mdsX) {
			this.x = mdsX;
		}

		public double getMdsY() {
			return y;
		}

		public void setMdsY(double mdsY) {
			this.y = mdsY;
		}
		
		
	    
		public void setOrderPos(int orderPos){
			this.orderPos = orderPos;
		}
		
		public int getOrderPos(){
			return orderPos;
		}
		
		public String getName() {
			return n;
		}
		public void setName(String name) {
			this.n = name;
		}
		public long getTimePoint() {
			return t;
		}
		public void setTimePoint(long timePoint) {
			this.t = timePoint;
		}

		public Node(String name, long timePoint, int cluster, String url,
				int orderPos, double mdsX, double mdsY, double isoX, double isoY) {
			super();
			this.n = name;
			this.t = timePoint;
			this.orderPos = orderPos;
			this.x = mdsX;
			this.y = mdsY;
		}


		

		
	}
	
	public static class Edge{
		/**
		 * The source node id according to the index in the node arraylist
		 */
		String s;
		
		/**
		 * The target node id according to the index in the node arraylist
		 */
		String t;
		
		/**
		 * The value of the similarity between the two node.
		 */
		double v;

		public String getSource() {
			return s;
		}
		public void setSource(String source) {
			this.s = source;
		}
		public String getTarget() {
			return t;
		}
		public void setTarget(String target) {
			this.t = target;
		}
		public double getValue() {
			return v;
		}
		public void setValue(double value) {
			this.v= value;
		}
		public Edge(String source, String target, double value, double isoValue) {
			super();
			this.s = source;
			this.t = target;
			this.v = value;
		}

	}
	
	/**
	 * the title of the visualization. (e.g. the title of the wikipedia page)
	 */
	String title;
	
	/**
	 * optional, used for the link to the web page
	 */
	String url;

	/**
	 * the list for the node. THE ORDER OF THE NODES MATTERS
	 */
	ArrayList<Node> nodes;
	
	/**
	 * the list for the edges.
	 */
	ArrayList<Edge> edges;
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}
	public ArrayList<Edge> getEdges() {
		return edges;
	}
	public void setEdges(ArrayList<Edge> edges) {
		this.edges = edges;
	}
	public VersionGraph(ArrayList<Node> nodes, ArrayList<Edge> edges) {
		super();
		this.nodes = nodes;
		this.edges = edges;
	} 
	

}
