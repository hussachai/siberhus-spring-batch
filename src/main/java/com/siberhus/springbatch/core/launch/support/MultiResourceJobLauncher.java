package com.siberhus.springbatch.core.launch.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.siberhus.springbatch.core.DefaultResourceAwareJobParametersCreator;
import com.siberhus.springbatch.core.ResourceAwareJobParametersCreator;

/**
 *
 * This class is not thread-safe
 * 
 * @author hussachai
 *
 */
public class MultiResourceJobLauncher implements InitializingBean {
	
	private static final Log logger = LogFactory.getLog(MultiResourceJobLauncher.class);
	
	private JobLauncher jobLauncher;
	
	private Job job;
	
	private Resource resource;
	
	private String resourceName;
	
	private ResourceAwareJobParametersCreator resourceAwareJobParametersCreator;
	
	private FilenameFilter directoryListingFilter;
	
	//if this value is not null, the resource will be moved here
	private Resource destination;
	
	private boolean shouldDeleteIfFinish = false;
	
	//StopOnError must be true,if the sequence of file,to be processing, is important.
	private boolean stopOnError = false;
	
	private Comparator<Resource> comparator = new Comparator<Resource>() {
		
		/**
		 * Compares resource filenames.
		 */
		public int compare(Resource r1, Resource r2) {
			return r1.getFilename().compareTo(r2.getFilename());
		}
		
	};
	
	public void setJobLauncher(JobLauncher jobLauncher){
		this.jobLauncher = jobLauncher;
	}
	
	public void setJob(Job job){
		this.job = job;
	}
	
	public void setResource(Resource resource){
		this.resource = resource;
	}
	
	public void setResourceName(String resourceName){
		this.resourceName = resourceName;
	}
	
	public void setResourceAwareJobParametersCreator(
			ResourceAwareJobParametersCreator resourceAwareJobParametersCreator) {
		this.resourceAwareJobParametersCreator = resourceAwareJobParametersCreator;
	}
	
	public void setDirectoryListingFilter(FilenameFilter directoryListingFilter){
		this.directoryListingFilter = directoryListingFilter;
	}
	public void setDestination(Resource destination) {
		this.destination = destination;
	}
	
	public void setShouldDeleteIfFinish(boolean shouldDeleteIfFinish) {
		this.shouldDeleteIfFinish = shouldDeleteIfFinish;
	}
	
	public void setStopOnError(boolean stopOnError){
		this.stopOnError = stopOnError;
	}
	
	/**
	 * @param comparator used to order the injected resources, by default
	 * compares {@link Resource#getFilename()} values.
	 */
	public void setComparator(Comparator<Resource> comparator) {
		this.comparator = comparator;
	}
	
	public void launch() throws JobExecutionException{
		Assert.notNull(job, "job must be set");
		launch(job);
	}
	
	public void launch(Job job) throws JobExecutionException{
		
		Assert.notNull(resource, "resource is a required property");
		
		Resource sources[] = null;
		
		File resourceFile = null;
		
		try {
			
			resourceFile = resource.getFile();
			
			if(resourceFile.exists()){
				if(resourceFile.isDirectory()){
					if(destination!=null){
						if(!destination.getFile().exists()){
							if(!destination.getFile().mkdir()){
								throw new IOException("Failed to create directory: "+destination);
							}
						}
						if(!destination.getFile().isDirectory()){
							throw new IllegalArgumentException("resource and destination must be the same file type [file|directory].");
						}
					}
				}
			}else{
				throw new FileNotFoundException("Resource: "+resource+" not found.");
			}
			
			if(resourceFile.isDirectory()){
				File files[] = resourceFile.listFiles(directoryListingFilter);
				sources = new Resource[files.length];
				for(int i=0;i<files.length;i++){
					sources[i] = new FileSystemResource(files[i].getCanonicalPath());//for now we support only FileSystemResource
				}
			}else{
				sources = new Resource[]{resource};
			}
			
		} catch (Exception e) {
			throw new JobExecutionException(e.getMessage(), e);
		}
		
		if (sources.length == 0) {
			logger.warn("No resources to read");
			return;
		}
		
		Arrays.sort(sources, comparator);
		
		for(int i=0;i<sources.length;i++){
			Resource source = sources[i];
			try{
				JobParameters jobParameters = resourceAwareJobParametersCreator.create(source);				
				JobExecution jobExecution = jobLauncher.run(job, jobParameters);
				if(jobExecution.getExitStatus()==ExitStatus.COMPLETED){
					if(shouldDeleteIfFinish){
						logger.info("Deleting file "+resourceFile);
					}else if(destination!=null){
						File destFile = destination.getFile();
						if(resourceFile.isDirectory()){					
							FileUtils.moveFileToDirectory(resourceFile, destFile, false);
						}else{
							FileUtils.moveFile(resourceFile, destFile);
						}
					}
				}
			}catch(Exception e){
				if(stopOnError){
					if(e instanceof JobExecutionException){
						throw (JobExecutionException)e;
					}else{
						throw new JobExecutionException(e.getMessage(), e);
					}
				}else{
					if(e instanceof JobInstanceAlreadyCompleteException
						|| e instanceof JobExecutionAlreadyRunningException
						|| e instanceof JobRestartException){
						logger.error(e.getMessage());
					}else{
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jobLauncher, "delegate is a required property");		
		Assert.notNull(directoryListingFilter, "directoryListingFilter is a required property");
		Assert.notNull(resourceName, "resourceName is a required property");
		if(resourceAwareJobParametersCreator==null){
			resourceAwareJobParametersCreator = new DefaultResourceAwareJobParametersCreator(resourceName);
		}
	}
	
}
