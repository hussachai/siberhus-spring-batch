package com.siberhus.springbatch.core;

import java.io.IOException;

import org.springframework.batch.core.JobParameters;
import org.springframework.core.io.Resource;

public interface ResourceAwareJobParametersCreator {

	JobParameters create(Resource resource) throws IOException;
	
}
