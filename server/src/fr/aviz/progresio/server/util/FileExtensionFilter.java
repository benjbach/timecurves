package fr.aviz.progresio.server.util;

import java.io.File;
import java.io.FileFilter;

public class FileExtensionFilter implements FileFilter {

	private String ext = "";

	public FileExtensionFilter (String ext){
		this.ext = ext;
	}
	
	@Override
	public boolean accept(File f) {
		
		String extension = "";
		String fileName = f.getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
		    extension = fileName.substring(i+1);
		}
		
		return extension.equals(ext);
	}
}
