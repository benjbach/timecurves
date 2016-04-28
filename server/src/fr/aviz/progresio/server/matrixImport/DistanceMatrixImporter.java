package fr.aviz.progresio.server.matrixImport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import fr.aviz.progresio.server.Log;
import fr.aviz.progresio.server.datastructure.VersionGraph;
import fr.aviz.progresio.server.export.Exporter;
import fr.aviz.progresio.server.util.FileExtensionFilter;


/** Imports one or a set of distance matrices 
 * The command line parameters are
 * - inputDir: directory from where to read the matrix files
 * - outputDir: directory where to place the generated curve(s)
 * - method: 'bulk' or 'combine'. Bulk generates one curve per input matrix, 
 * 			'combine' places all curves in the same MDS space.
 * 
 * The input format for the matrices is a simple JSON :
 * "name": curve name
 * "factormatrix": distance matrix (not similarity!)
 * */

public class DistanceMatrixImporter extends Exporter {

	static int fileCount = 0;
	static String outputDir = "";
	static String inputDir = "";
	
	
	/** 
	 * Entry point
	 * @param args
	 */
	public static void main(String[] args)
	{
		DistanceMatrixImporter o = new DistanceMatrixImporter();
		System.out.println(">> Processing files in /" + args[0]);
		inputDir = args[0];
 		outputDir = args[1]; 
		if(!outputDir.endsWith("/")){
			outputDir += '/';
		}
		
		File here = new File("");
		File dir = new File(here.getAbsolutePath() + "/"+ inputDir);	
		System.out.println(dir);
		
		File[] files = dir.listFiles(new FileExtensionFilter("json"){});
		for(File f : files){
			o.processFile(f);			
		}
		System.out.println(">> Done: Files, exported to " + outputDir);
	}
	
	
	public void processFile(File f){

		if(!f.getName().endsWith(".json"))
			return;
			
		outputFileName = outputDir + f.getName().split("\\.")[0] + "_curves";
		Log.out("outputFileName: " +  outputFileName);
		ArrayList<VersionGraph.Node> nodes = new ArrayList<VersionGraph.Node>();
		ArrayList<VersionGraph.Edge> edges = new ArrayList<VersionGraph.Edge>();

		ArrayList<JSONObject> processedJSONs = new ArrayList<JSONObject>();
	
		addNetwork(nodes, edges, f, processedJSONs);
				
		graph = new VersionGraph(nodes,edges);

		exportToJson();		
		Log.out("Exported!");
	}
	

	public int curveCount = 0;
	public void addNetwork(
			ArrayList<VersionGraph.Node> nodes, 
			ArrayList<VersionGraph.Edge> edges, 
			File file,
			ArrayList<JSONObject> processedFiles)
	{


		BufferedReader bufferedReader;
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			
			while((line = bufferedReader.readLine())!=null){
				stringBuffer.append(line);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	 
		String content = stringBuffer.toString();
		content = content.replace('\t', ' ');
		
		Object obj = JSONValue.parse(content);
		JSONObject jo =(JSONObject) obj;
		processedFiles.add(jo);		

		JSONArray data = (JSONArray) jo.get("data");
		int data_num = data.size(); 

		JSONArray timelabels; 
		long time;
		String dataName;
		JSONObject dataObject;
		for(int di = 0 ; di < data.size(); di++){
			dataObject = (JSONObject) data.get(di);
			timelabels = (JSONArray) dataObject.get("timelabels"); 		
			// Try to get data name. If not specified number.
			try{
				dataName = (String) dataObject.get("name");
			}catch(Exception ex){
				dataName = "Data_"+di;
			}
			for (int i = 0 ; i < timelabels.size() ; i++){
				try {
					time = getTime((String) timelabels.get(i));
				} catch (ParseException e) {
					time = i;
				}
				nodes.add(new VersionGraph.Node(i + "#" + dataName , time, 0, "Image goes here", i ,0d,0d,0d,0d));
			}
		}
		
		// add edges
		JSONArray distances = (JSONArray) jo.get("distancematrix"); 		
		double d;
		for(int i=0;i<distances.size() ;i++){
			for(int j=i+1;j<distances.size();j++){
				d = ((Number) ((JSONArray) distances.get(i)).get(j)).floatValue();
				edges.add(new VersionGraph.Edge(String.valueOf(i), String.valueOf(j), d, 0d));
				edges.add(new VersionGraph.Edge(String.valueOf(j), String.valueOf(i), d, 0d));
			}
		}	
	}

	public long getTime(String s) throws ParseException{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(s).getTime();
	}
}
