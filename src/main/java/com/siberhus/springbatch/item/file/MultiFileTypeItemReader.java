package com.siberhus.springbatch.item.file;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class MultiFileTypeItemReader <T> implements ResourceAwareItemReaderItemStream<T>, InitializingBean{
	
	private static final Log logger = LogFactory.getLog(MultiFileTypeItemReader.class);
	
	private Resource resource;
	
	//delegates is a map that has file extension as a key and beanName as a value.  
	private Map<String, ResourceAwareItemReaderItemStream<? extends T>> delegateMap = new HashMap<String, ResourceAwareItemReaderItemStream<? extends T>>(); 
	
	private ResourceAwareItemReaderItemStream<? extends T> delegate;
	
	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public void setDelegateMap(Map<String, ResourceAwareItemReaderItemStream<? extends T>> delegateMap){		
		this.delegateMap = delegateMap;
	}
	
	@Override
	public void open(ExecutionContext executionContext)
			throws ItemStreamException {
		Assert.notNull(resource, "Resource must be set");
		
		String filename = resource.getFilename();
		String ext = FilenameUtils.getExtension(filename).toUpperCase();
		delegate = delegateMap.get(ext);
		if(delegate==null){
			throw new ItemStreamException("No itemReader was registered for file type: "+ext);
		}
		delegate.setResource(resource);
		delegate.open(executionContext);
	}
	
	@Override
	public T read() throws Exception, UnexpectedInputException, ParseException {
		return delegate.read();
	}
	
	@Override
	public void update(ExecutionContext executionContext)
			throws ItemStreamException {
		delegate.update(executionContext);
	}

	@Override
	public void close() throws ItemStreamException {
		delegate.close();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(delegateMap, "delegateMap is a required field");			
		
		Map<String, ResourceAwareItemReaderItemStream<? extends T>> newDelegateMap = new HashMap<String, ResourceAwareItemReaderItemStream<? extends T>>();
		for(String key : delegateMap.keySet()){
			newDelegateMap.put(key.toUpperCase(), delegateMap.get(key));
		}
		this.delegateMap = newDelegateMap;
		
	}
	
	
	
}
