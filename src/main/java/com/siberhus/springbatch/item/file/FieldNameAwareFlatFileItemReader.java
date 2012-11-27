package com.siberhus.springbatch.item.file;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.LineMapper;

import com.siberhus.springbatch.item.file.mapping.FieldNameAwareLineMapper;

public class FieldNameAwareFlatFileItemReader<T> extends FlatFileItemReader<T>{
	
	private FieldNameAwareLineMapper<T> lineMapper;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		super.setLinesToSkip(1);//first line is field name line.
		super.setSkippedLinesCallback(new FieldNameLineCallback());
	}
	
	@Override
	public void setLinesToSkip(int linesToSkip) {
		throw new IllegalStateException("This method was disabled due to internal API usage.");
	}
	
	@Override
	public void setSkippedLinesCallback(LineCallbackHandler skippedLinesCallback) {
		throw new IllegalStateException("This method was disabled due to internal API usage.");
	}
	
	@Override
	public void setLineMapper(LineMapper<T> lineMapper){
		super.setLineMapper(lineMapper);
		if(lineMapper instanceof FieldNameAwareLineMapper){
			this.lineMapper = (FieldNameAwareLineMapper<T>)lineMapper;
		}else{
			throw new IllegalArgumentException("lineMapper must be a class or subclass of "
					+FieldNameAwareLineMapper.class.getName());
		}
	}
	
	class FieldNameLineCallback implements LineCallbackHandler{
		@Override
		public void handleLine(String line) {
			lineMapper.setNames(line);
		}
	}
	
}
