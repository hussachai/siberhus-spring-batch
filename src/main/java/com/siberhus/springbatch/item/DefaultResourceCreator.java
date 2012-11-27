package com.siberhus.springbatch.item;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class DefaultResourceCreator implements ResourceCreator, InitializingBean {
	
	private boolean createDirectory = true;
	private String prefix;
	private String suffix;
	private String subdirectory;
	
	public DefaultResourceCreator(){}
	
	public DefaultResourceCreator(String subdirectory){
		setSubdirectory(subdirectory);
	}
	
	@Override
	public Resource create(Resource example) throws IOException{
		
		String parent = example.getFile().getParent();
		String targetDirPath = parent+File.separator+subdirectory;
		File targetDir = new File(targetDirPath);
		if(!targetDir.exists()){
			if(createDirectory){
				targetDir.mkdir();
			}
		}
		String filename = example.getFilename();
//		filename = FilenameUtils.getBaseName(filename);
//		String ext = FilenameUtils.getExtension(filename);
//		if(prefix!=null){
//			filename = prefix + filename;
//		}
//		if(suffix!=null){
//			filename = filename + suffix;
//		}
		return new FileSystemResource(targetDirPath +
				File.separator + filename);
	}
	
	public void setCreateDirectory(boolean createDirectory){
		this.createDirectory = createDirectory;
	}
	
	public void setSubdirectory(String subdirectory) {
		this.subdirectory = subdirectory;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(subdirectory, "subdirectory is a required field.");
		if(subdirectory.startsWith(File.separator)){
			subdirectory = subdirectory.substring(1);
		}
		if(subdirectory.endsWith(File.separator)){
			subdirectory = subdirectory.substring(subdirectory.length());
		}
	}
	
}
