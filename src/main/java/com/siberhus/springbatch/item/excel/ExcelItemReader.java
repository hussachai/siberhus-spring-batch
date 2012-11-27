package com.siberhus.springbatch.item.excel;

import java.io.BufferedReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.batch.item.ReaderNotOpenException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.siberhus.springbatch.item.excel.mapping.DefaultExcelRowMapper;

public class ExcelItemReader <T> extends AbstractItemCountingItemStreamItemReader<T> implements
		ResourceAwareItemReaderItemStream<T>, InitializingBean {

	private static final Log logger = LogFactory.getLog(ExcelItemReader.class);
	
	private Resource resource;
	
	private boolean fieldNameAware = true;
	
	private Sheet sheet;
	
	private Iterator<Row> rowIterator;
	
	private int rowCount = 0;
	
	private boolean noInput = false;
	
	private FieldSetMapper<T> fieldSetMapperForDefaultRowMapper;
	
	private ExcelRowMapper<T> rowMapper;
	
	private int rowsToSkip = 0;
	
	private ExcelRowCallbackHandler skippedLinesCallback;
	
	private boolean strict = false;
	
	private ExcelWorkbookFactory workbookFactory = new DefaultExcelWorkbookFactory();
	
	public ExcelItemReader() {
		setName(ClassUtils.getShortName(ExcelItemReader.class));
	}
	
	/**
	 * In strict mode the reader will throw an exception on
	 * {@link #open(org.springframework.batch.item.ExecutionContext)} if the
	 * input resource does not exist.
	 * @param strict false by default
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
	public void setFieldNameAware(boolean fieldNameAware){
		this.fieldNameAware = fieldNameAware;
	}
	
	/**
	 * @param skippedLinesCallback will be called for each one of the initial
	 * skipped lines before any items are read.
	 */
	public void setSkippedLinesCallback(ExcelRowCallbackHandler skippedLinesCallback) {
		this.skippedLinesCallback = skippedLinesCallback;
	}

	/**
	 * Public setter for the number of lines to skip at the start of a file. Can
	 * be used if the file contains a header without useful (column name)
	 * information, and without a comment delimiter at the beginning of the
	 * rows.
	 * 
	 * @param rowsToSkip the number of rows to skip
	 */
	public void setRowsToSkip(int rowsToSkip) {
		this.rowsToSkip = rowsToSkip;
	}

	/**
	 * Setter for row mapper.
	 * @param rowMapper maps row to item
	 */
	public void setRowMapper(ExcelRowMapper<T> rowMapper) {		
		this.rowMapper = rowMapper;
	}
	

	public void setFieldSetMapperForDefaultRowMapper(FieldSetMapper<T> fieldSetMapper){
		this.fieldSetMapperForDefaultRowMapper = fieldSetMapper;
	}
	
	/**
	 * Factory for the {@link BufferedReader} that will be used to extract rows
	 * from the file. The default is fine for plain text files, but this is a
	 * useful strategy for binary files where the standard BufferedReaader from
	 * java.io is limiting.
	 * 
	 * @param excelWorkbookFactory the excelWorkbookFactory to set
	 */
	public void setWorkbookFactory(ExcelWorkbookFactory workbookFactory) {
		this.workbookFactory = workbookFactory;
	}
	
	/**
	 * Public setter for the input resource.
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	@Override
	protected T doRead() throws Exception {
		if (noInput) {
			return null;
		}
		
		Row row = readRow();
		
		if (row == null) {
			return null;
		}
		else {
			try{
				return rowMapper.mapRow(row, rowCount);
			}
			catch(Exception ex){
				logger.error("Parsing error at row: " + rowCount + " in resource=" + 
						resource.getDescription() + ", input=[" + row + "]", ex);
				throw ex;
			}
		}
	}
	
	/**
	 * @return next row (skip comments).
	 */
	private Row readRow() {
		
		if (rowIterator == null) {
			throw new ReaderNotOpenException("Reader must be open before it can be read.");
		}
		
		Row row = null;
		
		try {
			
			row = rowIterator.next();
			
			rowCount++;
			
			return row;
			
		}catch (NoSuchElementException e) {
			return null;
		}		
	}
	
	@Override
	protected void doClose() throws Exception {
		rowCount = 0;
//		if (sheet != null) {
//		}
		//Closing is not necessary
	}
	
	@Override
	protected void doOpen() throws Exception {
		Assert.notNull(resource, "Input resource must be set");
		
		noInput = false;
		if (!resource.exists()) {
			if (strict) {
				throw new IllegalStateException("Input resource must exist (reader is in 'strict' mode): " + resource);
			}
			noInput = true;
			logger.warn("Input resource does not exist " + resource.getDescription());
			return;
		}

		if (!resource.isReadable()) {
			if (strict) {
				throw new IllegalStateException("Input resource must be readable (reader is in 'strict' mode): " + resource);
			}
			noInput = true;
			logger.warn("Input resource is not readable " + resource.getDescription());
			return;
		}
		
		sheet = workbookFactory.createAndRead(resource).getSheetAt(0);
		
		rowIterator = sheet.rowIterator();
		
		if(fieldNameAware){
			rowMapper.setNames(readRow());
		}
		
		for (int i = 0; i < rowsToSkip; i++) {
			Row row = readRow();
			if (skippedLinesCallback != null) {
				skippedLinesCallback.handleRow(row);
			}
		}
	}
	
	public void afterPropertiesSet() throws Exception {
//		Assert.notNull(rowMapper, "ExcelRowMapper is required");
		if(rowMapper==null){
			DefaultExcelRowMapper<T> defaultRowMapper = new DefaultExcelRowMapper<T>();
			if(fieldSetMapperForDefaultRowMapper==null){
				Assert.notNull(rowMapper, "ExcelRowMapper is required");
			}else{
				defaultRowMapper.setFieldSetMapper(fieldSetMapperForDefaultRowMapper);
			}
			defaultRowMapper.afterPropertiesSet();
			rowMapper = defaultRowMapper;			
		}
	}
	
	@Override
	protected void jumpToItem(int itemIndex) throws Exception {
		for (int i = 0; i < itemIndex; i++) {
			readRow();
		}
	}
	
}