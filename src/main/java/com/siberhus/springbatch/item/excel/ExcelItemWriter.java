package com.siberhus.springbatch.item.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.WriterNotOpenException;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.util.ExecutionContextUserSupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.siberhus.springbatch.format.TypeFormat;

/**
 * 
 * This class does not support restart operation yet.
 * 
 * @author hussachai
 *
 * @param <T>
 */
public class ExcelItemWriter <T> extends ExecutionContextUserSupport implements ResourceAwareItemWriterItemStream<T>,
	InitializingBean {
	
	protected static final Log logger = LogFactory.getLog(ExcelItemWriter.class);
	
	private Resource resource;
	
	private boolean initialized = false;
	
	private Workbook workbook;
	
	private String[] names;
	
	private String sheetName = "Untitled";
	
	private Sheet sheet;
	
	private long linesWritten = 0;
	
	private int currentRowIndex = 0;
	
	private OutputStream outputStream;
	
	private boolean shouldDeleteIfEmpty = true;
	
	private ExcelHeaderCallback headerCallback;
	
	private ExcelFooterCallback footerCallback;
	
	private ExcelWorkbookFactory workbookFactory = new DefaultExcelWorkbookFactory();
	
	private FieldExtractor<T> fieldExtractor;
	
	//If this property is set, the output will be converted to string
	private Map<Class<?>, ? extends TypeFormat<T>> customTypeFormats;
	
	public ExcelItemWriter(){
		setName(ClassUtils.getShortName(ExcelItemWriter.class));
	}
	
	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public void setNames(String[] names) {
		this.names = names;
	}
	
	public void setSheetName(String sheetName){
		this.sheetName = sheetName;
	}
	
	/**
	 * headerCallback will be called before writing the first item to file.
	 * Newline will be automatically appended after the header is written.
	 */
	public void setHeaderCallback(ExcelHeaderCallback headerCallback) {
		this.headerCallback = headerCallback;
	}

	/**
	 * footerCallback will be called after writing the last item to file, but
	 * before the file is closed.
	 */
	public void setFooterCallback(ExcelFooterCallback footerCallback) {
		this.footerCallback = footerCallback;
	}
	
	public void setShouldDeleteIfEmpty(boolean shouldDeleteIfEmpty) {
		this.shouldDeleteIfEmpty = shouldDeleteIfEmpty;
	}
	
	/**
	 * 
	 * @param workbookFactory the workbookFactory to set
	 */
	public void setWorkbookFactory(ExcelWorkbookFactory workbookFactory) {
		this.workbookFactory = workbookFactory;
	}
	
	public void setFieldExtractor(FieldExtractor<T> fieldExtractor) {
		this.fieldExtractor = fieldExtractor;
	}
	
	public void setCustomTypeFormats(Map<Class<?>, ? extends TypeFormat<T>> customTypeFormats){
		this.customTypeFormats = customTypeFormats;
	}
	
	@Override
	public void close() throws ItemStreamException {
		
		if(workbook==null) return;
		
		try{
			if (footerCallback != null) {
				footerCallback.writeFooter(createNextRow());
			}
			
			if(shouldDeleteIfEmpty && linesWritten == 0){
				return;
			}
			
			try {
				File file = resource.getFile();
				outputStream = new FileOutputStream(file.getAbsolutePath(), false);
				workbook.write(outputStream);
			} catch (IOException e) {
				throw new ItemStreamException("Failed to write workbook to file", e);
			}
		}finally{
			IOUtils.closeQuietly(outputStream);
			//reset
			initialized = false;
			linesWritten = 0;
			currentRowIndex = 0;
		}
	}
	
	@Override
	public void open(ExecutionContext executionContext)
			throws ItemStreamException {
		Assert.notNull(resource, "The resource must be set");
		if(!initialized){
			doOpen(executionContext);
		}
		
	}
	
	protected void doOpen(ExecutionContext executionContext) 
			throws ItemStreamException {
		
		try {
			workbook = workbookFactory.create(resource);
			sheet = workbook.createSheet(sheetName);			
			initialized = true;
		} catch (IOException e) {
			throw new ItemStreamException("Failed to initialize writer", e);
		}
		
		if (headerCallback != null) {
			headerCallback.writeHeader(createNextRow());
		}
	}
	
	@Override
	public void update(ExecutionContext executionContext)
			throws ItemStreamException {
	}
	
	
	@Override
	public void write(List<? extends T> items) throws Exception {
		if (!initialized) {
			throw new WriterNotOpenException("Writer must be open before it can be written to");
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Writing to excel file with " + items.size() + " items.");
		}
		
		Object cellValues[] = null;
		for (T item : items) {
			cellValues = fieldExtractor.extract(item);
			linesWritten++;
		}
		Row row = createNextRow();
		if(customTypeFormats!=null){
			for(int i=0;i<cellValues.length;i++){
				Object cellValue = cellValues[i];
				System.out.println("cellValue: "+cellValue);
				if(cellValue==null){
					row.createCell(i, Cell.CELL_TYPE_BLANK);
					continue;
				}
				String stringValue;
				TypeFormat typeFormat = customTypeFormats.get(cellValue.getClass());
				System.out.println("typeFormat: "+typeFormat);
				if(typeFormat!=null){
					stringValue = typeFormat.format(cellValue);
				}else{
					stringValue = cellValue.toString();
				}
				System.out.println("stringValue: "+stringValue);
				row.createCell(i, Cell.CELL_TYPE_STRING).setCellValue(stringValue);
			}
			return;
		}
		for(int i=0;i<cellValues.length;i++){
			Object cellValue = cellValues[i];
			if(cellValue==null){
				row.createCell(i, Cell.CELL_TYPE_BLANK);
			}else if(cellValue instanceof String){
				row.createCell(i, Cell.CELL_TYPE_STRING)
					.setCellValue(cellValue.toString());
			}else if(cellValue instanceof Number){
				row.createCell(i, Cell.CELL_TYPE_NUMERIC)
					.setCellValue(((Number) cellValue).doubleValue());
			}else if(cellValue instanceof Date){
				row.createCell(i, Cell.CELL_TYPE_NUMERIC)
					.setCellValue((Date)cellValue);
			}else if(cellValue instanceof Calendar){
				row.createCell(i, Cell.CELL_TYPE_NUMERIC)
					.setCellValue((Calendar)cellValue);
			}else if(cellValue instanceof Boolean){
				row.createCell(i, Cell.CELL_TYPE_BOOLEAN)
					.setCellValue((Boolean)cellValue);
			}else{
				row.createCell(i, Cell.CELL_TYPE_STRING)
					.setCellValue(cellValue.toString());
			}
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(names!=null){
			if(headerCallback==null){
				headerCallback = new DefaultExcelHeaderCallback();
			}
			if(fieldExtractor==null){
				BeanWrapperFieldExtractor<T> fieldExtractor = new BeanWrapperFieldExtractor<T>();
				fieldExtractor.setNames(names);
				this.fieldExtractor = fieldExtractor;
			}
		}
		Assert.notNull(fieldExtractor, "An FieldExtractor must be provided.");
	}
	
	private Row createNextRow(){
		Row row = sheet.createRow(currentRowIndex);
		currentRowIndex++;
		return row;
	}
	
	class DefaultExcelHeaderCallback implements ExcelHeaderCallback{

		@Override
		public void writeHeader(Row row) {
			for(int i=0;i<names.length;i++){
				Cell cell = row.createCell(i);
				cell.setCellValue(names[i]);
			}
		}
		
	}
}
