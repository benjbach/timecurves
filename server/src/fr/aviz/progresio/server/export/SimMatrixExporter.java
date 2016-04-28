package fr.aviz.progresio.server.export;

/**
 * The abstract class used to export different data (e.g. dynamic graphs, wiki pages, codes)
 * 
 * @author conglei_shi(conglei.org)
 * 
 */
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;


import fr.aviz.progresio.server.datastructure.VersionGraph;

public class SimMatrixExporter extends Exporter{
	
	public static class SimMatrix{
		public String[] name = null;
		public double[][] data = null;
	}
	
	public SimMatrixExporter(String filename){
		outputFileName = filename.split("\\.")[0];
	}
	
	public static void main(String[] args){
		Frame frame = new JFrame("Video Analysis");
		frame.setSize(10, 10);
		frame.setVisible(true);
		
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(new File(".").getAbsoluteFile());
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setVisible(true);
		int returnVal = fc.showOpenDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			File f = fc.getSelectedFile();
			SimMatrix matrix = readFromCSV(f.getAbsolutePath());
			SimMatrixExporter exporter = new SimMatrixExporter(f.getName());
			exporter.constructGraph(matrix);
			exporter.exportToJson();
		}
		System.exit(0);
		
	}
	
	public static SimMatrix readFromCSV(String fileName){
		SimMatrix matrix = new SimMatrix();
		DateFormat dFormat= new SimpleDateFormat("yyyy-MM-dd");
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(new File(fileName)));
			String line = bReader.readLine();
			ArrayList<double[]> arrayList = new ArrayList<double[]>();
			matrix.name = line.split(",");
			line = bReader.readLine();
			while (line!=null){
				String[] items = line.split(",");
				double[] array = new double[items.length];
				for (int i = 0; i < array.length; i++){
					if (items[i].isEmpty()) array[i] = 0;
					else {
						array[i] = Double.parseDouble(items[i]);
					}
				}
				arrayList.add(array);
				line = bReader.readLine();
			}
			int n = matrix.name.length;
			matrix.data = new double[n][n];
			for (int i = 0; i < n; i++){
				for (int j = 0 ; j < arrayList.size(); j++){
					try{
						matrix.data[i][j] = arrayList.get(j)[i];
					} catch (ArrayIndexOutOfBoundsException e) {
						matrix.data[i][j] = 0;
					}
					
				}
			}
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return matrix;
	}
	
	public void constructGraph(SimMatrix matrix){
		int n = matrix.name.length;
		ArrayList<VersionGraph.Node> nodes = new ArrayList<VersionGraph.Node>();
		ArrayList<VersionGraph.Edge> edges = new ArrayList<VersionGraph.Edge>();

		DateFormat dFormat= new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0 ; i < n ; i++){
			String name = matrix.name[i];
			name = name.replace("'", "");
			name = name.replace(".png", "");
			int time = Integer.parseInt(name.replace("frame-", ""));
			nodes.add(new VersionGraph.Node(
					name, (long) time, 0, "data/video/" + outputFileName + "/" + name + ".png", 0 ,0d,0d,0d,0d));
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				edges.add(new VersionGraph.Edge(String.valueOf(i), String.valueOf(j), matrix.data[i][j],0d));
				edges.add(new VersionGraph.Edge(String.valueOf(j), String.valueOf(i), matrix.data[i][j],0d));
			}
		}	
		
		
		graph = new VersionGraph(nodes,edges);
	}
	
}
