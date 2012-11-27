package com.siberhus.springbatch.core.launch.support;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

public class DefaultDirectoryListingFilter implements FilenameFilter{
	
	private Set<String> extensions;
	
	public DefaultDirectoryListingFilter(){}
	
	public DefaultDirectoryListingFilter(Set<String> extensions){
		this.extensions = extensions;
	}
	
	public void setExtensions(String[] extensions){
		this.extensions = new HashSet<String>();
		for(String extension : extensions){
			this.extensions.add(extension.toUpperCase());
		}
	}
	
	@Override
	public boolean accept(File dir, String name) {
		String ext = FilenameUtils.getExtension(name).toUpperCase();
		if(extensions.contains(ext)){
			return true;
		}
		return false;
	}
	
}
