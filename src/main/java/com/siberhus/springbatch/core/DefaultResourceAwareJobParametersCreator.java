package com.siberhus.springbatch.core;

import java.io.IOException;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.core.io.Resource;

public class DefaultResourceAwareJobParametersCreator implements ResourceAwareJobParametersCreator{
	
	private String resourceName;
	
	public DefaultResourceAwareJobParametersCreator(String resourceName){
		this.resourceName = resourceName;
	}
	
	@Override
	public JobParameters create(Resource resource)throws IOException {
		return new JobParametersBuilder()
			.addString(resourceName, resource.getFile().getCanonicalPath())
			.toJobParameters();
	}
	
}
