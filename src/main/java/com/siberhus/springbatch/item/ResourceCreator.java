package com.siberhus.springbatch.item;

import java.io.IOException;

import org.springframework.core.io.Resource;

public interface ResourceCreator {
	
	Resource create(Resource example) throws IOException;
	
}
