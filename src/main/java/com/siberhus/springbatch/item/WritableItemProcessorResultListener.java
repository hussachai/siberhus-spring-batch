package com.siberhus.springbatch.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepListenerFailedException;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 
 * 
 * The implementation is <b>not</b> thread-safe.
 * 
 * @author hussachai
 */
public class WritableItemProcessorResultListener<T,S> extends AbstractItemProcessorResultListener<T, S>
	implements InitializingBean {
	
	protected static final Log logger = LogFactory.getLog(WritableItemProcessorResultListener.class);
	
	private int successChunkSize = 1;
	
	private List<S> successChunk = new ArrayList<S>();
	
	private ResourceAwareItemWriterItemStream<S> successWriter; 
	
	private ResourceCreator successResourceCreator;
	
	private int errorChunkSize = 1;
	
	private List<T> errorChunk = new ArrayList<T>();
	
	private ResourceAwareItemWriterItemStream<T> errorWriter;
	
	private ResourceCreator errorResourceCreator;
	
	private int warningChunkSize = 1;
	
	private List<S> warningChunk = new ArrayList<S>();
	
	private ResourceAwareItemWriterItemStream<S> warningWriter;
	
	private ResourceCreator warningResourceCreator;
	
	private Resource resource;
	
	public void setResource(Resource resource){
		this.resource = resource;
	}
	
	public void setSuccessChunkSize(int successChunkSize) {
		this.successChunkSize = successChunkSize;
	}
	
	public void setErrorChunkSize(int errorChunkSize) {
		this.errorChunkSize = errorChunkSize;
	}
	
	public void setWarningChunkSize(int waringChunkSize){
		this.warningChunkSize = waringChunkSize;
	}
	
	public void setSuccessWriter(ResourceAwareItemWriterItemStream<S> successWriter) {
		this.successWriter = successWriter;
	}

	public void setErrorWriter(ResourceAwareItemWriterItemStream<T> errorWriter) {
		this.errorWriter = errorWriter;
	}
	
	public void setWarningWriter(ResourceAwareItemWriterItemStream<S> warningWriter){
		this.warningWriter = warningWriter;
	}
	
	public void setSuccessResourceCreation(ResourceCreator successResourceCreator) {
		this.successResourceCreator = successResourceCreator;
	}

	public void setErrorResourceCreator(ResourceCreator errorResourceCreator) {
		this.errorResourceCreator = errorResourceCreator;
	}

	public void setWarningResourceCreator(ResourceCreator warningResourceCreator) {
		this.warningResourceCreator = warningResourceCreator;
	}
	
	@Override
	public void doBeforeStep(StepExecution stepExecution) {
		Assert.notNull(resource, "Resource is a required property");		
		try{
			if(successWriter!=null){
				successWriter.setResource(successResourceCreator.create(resource));
				successWriter.open(stepExecution.getExecutionContext());
			}
			if(errorWriter!=null){
				errorWriter.setResource(errorResourceCreator.create(resource));
				errorWriter.open(stepExecution.getExecutionContext());
			}
			if(warningWriter!=null){
				warningWriter.setResource(warningResourceCreator.create(resource));
				warningWriter.open(stepExecution.getExecutionContext());
			}
		}catch(IOException e){
			throw new StepListenerFailedException("Failed to setup writer", e);
		}
	}
	
	@Override
	public void doAfterStep(StepExecution stepExecution) {		
		if(successWriter!=null){
			if(successChunk.size()>0){
				try {
					successWriter.write(successChunk);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			successWriter.close();
			successChunk.clear();
		}
		if(errorWriter!=null){
			if(errorChunk.size()>0){
				try {
					errorWriter.write(errorChunk);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			errorWriter.close();
			errorChunk.clear();
		}
		if(warningWriter!=null){
			if(warningChunk.size()>0){
				try{
					warningWriter.write(warningChunk);
				}catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			warningWriter.close();
			warningChunk.clear();
		}
	}
	
	@Override
	public void onError(StepExecution stepExecution, T item, Exception e) {
		
		if(errorWriter==null) return;
		
		try {
			errorChunk.add(item);
			if(errorChunk.size()==errorChunkSize){
				errorWriter.write(errorChunk);
				errorChunk.clear();
			}
		} catch (Exception e1) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void onWarning(StepExecution stepExecution, S item,
			WarningMessages messages) {
		
		if(warningWriter==null) return;
		
		try {
			warningChunk.add(item);
			if(warningChunk.size()==warningChunkSize){
				warningWriter.write(warningChunk);
				warningChunk.clear();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}
	
	@Override
	public void onSuccess(StepExecution stepExecution, S result) {
		
		if(successWriter==null) return;
		
		try {
			successChunk.add(result);
			if(successChunk.size()==successChunkSize){
				successWriter.write(successChunk);
				successChunk.clear();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		if(successResourceCreator==null){
			successResourceCreator = new DefaultResourceCreator("success");
		}
		if(errorResourceCreator==null){
			errorResourceCreator = new DefaultResourceCreator("error");
		}
		if(warningResourceCreator==null){
			warningResourceCreator = new DefaultResourceCreator("warning");
		}
	}
	
	
}

