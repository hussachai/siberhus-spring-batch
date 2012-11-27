package com.siberhus.springbatch.item;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;


public abstract class AbstractItemProcessorResultListener<T,S> implements ItemProcessListener<T, S>, StepExecutionListener{
	
	private StepExecution stepExecution;
	
	@Override	
	public final ExitStatus afterStep(StepExecution stepExecution) {
		doAfterStep(stepExecution);
		return stepExecution.getExitStatus();
	}
	
	@Override
	public final void beforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		doBeforeStep(stepExecution);
	}
	
	@Override
	public final void beforeProcess(T item) {}
	
	@Override
	public final void afterProcess(T item, S result) {
		if(result!=null){
			if(result instanceof WarningMessageable){
				WarningMessages warnings = ((WarningMessageable)result).warningMessages();
				if(warnings!=null && warnings.size()>0){
					onWarning(stepExecution, result, warnings);
					return;
				}
			}
			onSuccess(stepExecution, result);
		}else{
			onError(stepExecution, item, null);
		}
	}
	
	@Override
	public final void onProcessError(T item, Exception e) {
		onError(stepExecution, item, e);
	}
	
	public abstract void doBeforeStep(StepExecution stepExecution);
	
	public abstract void doAfterStep(StepExecution stepExecution);
	
	public abstract void onError(StepExecution stepExecution, T item, Exception e);
	
	public abstract void onWarning(StepExecution stepExecution, S item, WarningMessages warningMessages);
	
	public abstract void onSuccess(StepExecution stepExecution, S result);
	
}
