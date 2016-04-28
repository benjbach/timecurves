package fr.aviz.progresio.server.util;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.python.modules.synchronize;

public class MiscUtils {

	/**
	 * Downloads the content of a URL and returns it as a string.
	 * 
	 * @param url
	 * @return
	 */
	public synchronized static String getURLContent(String url) {
		try {
//			Thread.sleep(1000);
			URLConnection yc = (new URL(url)).openConnection();
			yc.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String str = "";
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				str += inputLine + "\n";
			}
			in.close();
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Cleans up the string to use as a filename.
	 * 
	 * Illegal or problematic characters are replaced with "_".
	 */
	public static String stringToFilename(String string) {
		string = string.replace("/", "_");
		string = string.replace(":", "_");
		string = string.replace(".", "_");
		string = string.replace("\\", "_");
		string = string.replace("*", "_");
		string = string.replace("?", "_");
		string = string.replace("\"", "_");
		string = string.replace("<", "_");
		string = string.replace(">", "_");
		string = string.replace("|", "_");
		string = string.replace("%", "_");
		string = string.replace("#", "_");
		string = string.replace("$", "_");
		string = string.replace(" ", "_");
		return string;
	}
	
	/**
	 * Reads a file into a string.
	 * 
	 * @param file
	 * @return
	 */
	public static String readFileAsString(File file) {
	    byte[] buffer = new byte[(int)file.length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(file));
	        f.read(buffer);
	    } catch (Exception e) {
	    	e.printStackTrace();
	        if (f != null)
	        	try {f.close();} catch (Exception e2) {}
	        return null;
	    }
	    return new String(buffer);
	}

	/**
	 * Saves a string into a file.
	 * @param file
	 * @param s
	 * @return
	 */
	public static boolean saveStringIntoFile(File file, String s) {
		return saveStringIntoFile(file, s, false);
	}
	
	/**
	 * Saves a string into a file.
	 * @param file
	 * @param s
	 * @return
	 */
	public static boolean saveStringIntoFile(File file, String s, boolean append) {
		BufferedOutputStream f = null;
		if (append && file.exists()) {
			String existingContent = readFileAsString(file);
			s = existingContent + s;
		}
		try {
			f = new BufferedOutputStream(new FileOutputStream(file));
			f.write(s.getBytes());
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (f != null)
	        	try {f.close();} catch (Exception e2) {}
	        return false;
		}
		return true;
	}
	
	/**
	 * Copies a file.
	 * 
	 * @param from
	 * @param to
	 */
	public static void copyFile(File from, File to) {
		String s = readFileAsString(from);
		saveStringIntoFile(to, s);
	}
	
	/**
	 * Deletes a directory and all its content.
	 * 
	 * @param path
	 * @return
	 */
	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}
	
	/**
	* Serialize an Object through a GZIPOutputStream .
	* @param vlo The object to be serialized .
	* @param The file where the object will be serialized .
	* From http://andreinc.net/2010/12/12/serialize-java-objects-using-gzip-streams-gzipinputstream-and-gzipoutputstream/
	*/
	public static void saveGZipObject(Object vlo, String fileName) {
	    FileOutputStream fos = null;
	    GZIPOutputStream gos = null;
	    ObjectOutputStream oos = null;
	    try {
	        fos = new FileOutputStream(fileName);
	        gos = new GZIPOutputStream(fos);
	        oos = new ObjectOutputStream(gos);
	        oos.writeObject(vlo);
	        oos.flush();
	        oos.close();
	        gos.close();
	        fos.close();
	    } catch(IOException ioe) {
	        ioe.printStackTrace();
	    }
	}

	/**
	 * From http://andreinc.net/2010/12/12/serialize-java-objects-using-gzip-streams-gzipinputstream-and-gzipoutputstream/
	 * @param fileName
	 * @return
	 */
	public static Object loadGZipObject(String fileName) {
	    Object obj = null;
	    FileInputStream fis = null;
	    GZIPInputStream gis = null;
	    ObjectInputStream ois = null;
	    try {
	        fis = new FileInputStream(fileName);
	        gis = new GZIPInputStream(fis);
	        ois = new ObjectInputStream(gis);
	        obj = ois.readObject();
	        ois.close();
	        gis.close();
	        fis.close();
	    } catch(IOException ioe) {
	        ioe.printStackTrace();
	    } catch(ClassNotFoundException cnfe) {
	        cnfe.printStackTrace();
	    }
	    return obj;
	}
}
