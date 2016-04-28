package fr.aviz.progresio.server.export;

/**
 * The abstract class used to export different data (e.g. dynamic graphs, wiki pages, codes)
 * 
 * @author conglei_shi(conglei.org)
 * 
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import mdsj.ClassicalScaling;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.google.gson.Gson;

import fr.aviz.progresio.server.Log;
import fr.aviz.progresio.server.datastructure.VersionGraph;

public abstract class Exporter {
	
	protected VersionGraph graph= null;
	
	protected String outputFileName = "";
	
	/**
	 * the directory where the data locates
	 */
	protected File outputfile;
	
		
	/**
	 * export the graph into json format
	 * @param file
	 */
	public void exportToJson(){
		try {
			reorder();
			
			File here = new File("");
			
			outputfile = new File(here.getAbsolutePath() + "/" + outputFileName +".curve");
			Gson gson = new Gson();
			graph.setEdges(new ArrayList<VersionGraph.Edge>());
		
			PrintWriter out = new PrintWriter(new FileWriter(outputfile));
			out.print(gson.toJson(graph));
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public abstract void constructGraph();
	/**
	 * Reorders revisions according their similarity.
	 *  + added the calculation of mds (conglei.org))
	 *  + added the calculation of IsoMap (conglei.org)
	 * @author benjamin.bach@inria.fr
	 */
	public void reorder() throws Exception {

		// Create value matrix
		DoubleMatrix2D valueMat = new DenseDoubleMatrix2D(graph.getNodes().size(),graph.getNodes().size());		
		double[][] matrix = new double[graph.getNodes().size()][graph.getNodes().size()];
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (VersionGraph.Node node: graph.getNodes()){
			min = Math.min(min, node.getTimePoint());
			max = Math.max(max, node.getTimePoint());
		}
		valueMat.assign(1);

		int n,m;
		for(VersionGraph.Edge e : graph.getEdges())
		{
			n = Integer.parseInt(e.getSource());
			m = Integer.parseInt(e.getTarget());
			matrix[n][m] = e.getValue();
			matrix[m][n] = e.getValue();
		}
		double[][] mdsResult = new double[2][matrix.length];

		Log.out("Start scaling: ");
		ClassicalScaling.fullmds(matrix, mdsResult);
		
		/**
		 * Used to draw the mds result.
		 * @author conglei.org
		 */		
		// Set new node order
		ArrayList<VersionGraph.Node> nodes = new ArrayList<VersionGraph.Node>();
		nodes.addAll(graph.getNodes());
		for(int i=0 ; i< nodes.size() ; i++ ){
			nodes.get(i).setMdsX(Double.compare(mdsResult[0][i],Double.NaN) == 0? 0 : mdsResult[0][i]);
			nodes.get(i).setMdsY(Double.compare(mdsResult[1][i],Double.NaN) == 0? 0 : mdsResult[1][i]);
		}
		
		//rotating algorithm
		double[] orgX = new double[nodes.size()];
		double[] orgY = new double[nodes.size()];
		double[] rotatedX = new double[nodes.size()];
		int minItemIndex = 0;
		long minTimeStamp = Long.MAX_VALUE;
		for (int i = 0; i < nodes.size(); i++){
			if (minTimeStamp > nodes.get(i).getTimePoint()){
				minItemIndex = i;
				minTimeStamp = nodes.get(i).getTimePoint();
			};
			orgX[i] = nodes.get(i).getMdsX();
			orgY[i] = nodes.get(i).getMdsY();
		}		
		int minIndexXOrder = orgX.length;
		int minAngle = 0;
		for (int angle = 0; angle <=360; angle++){
			double realAngle = angle / 180.0 * Math.PI;
			ArrayList<Double> list = new ArrayList<Double>();
			for (int i = 0; i < orgX.length; i++){
				rotatedX[i] = orgX[i] * Math.cos(realAngle) - orgY[i] * Math.sin(realAngle);
				list.add(rotatedX[i]);
			}
			double temp = list.get(minItemIndex);
			Collections.sort(list);
			int newOrder = list.indexOf(temp);
			if (newOrder < minIndexXOrder){
				minIndexXOrder = newOrder;
				minAngle = angle;
			}
			if (newOrder == 0) break;
		}		
		for (int i = 0; i < orgX.length; i++){
			nodes.get(i).setMdsX(orgX[i] * Math.cos(minAngle / 180.0 * Math.PI) - orgY[i] * Math.sin(minAngle / 180.0 * Math.PI));
			nodes.get(i).setMdsY(orgY[i] * Math.cos(minAngle / 180.0 * Math.PI) + orgX[i] * Math.sin(minAngle / 180.0 * Math.PI));
		}
	}
	
	private void dumpSimMatrix(double[][] matrix,String name) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFileName+"_"+ name +"_matrix.csv")));
			String line = "";
			//dump the headline
			line = "version";
			for (int i = 0; i < matrix.length; i++){
				line += "," + graph.getNodes().get(i).getTimePoint();
			}
			line += "\n";
			bw.write(line);
			for (int i = 0; i < matrix.length; i++){
					line = "" + graph.getNodes().get(i).getTimePoint();
				for (int j = 0; j < matrix[i].length; j++){
					line += "," + matrix[i][j];
				}
					line += "\n";
					bw.write(line);
			}
			bw.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	public String jsonFileName(){
		return outputfile.getAbsolutePath();
	}
	
}
