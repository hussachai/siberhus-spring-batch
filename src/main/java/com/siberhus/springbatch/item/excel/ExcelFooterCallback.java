package com.siberhus.springbatch.item.excel;

import org.apache.poi.ss.usermodel.Row;

/**
 * Callback interface for writing a footer to a file.
 * 
 * @author Hussachai Puripunpinyo
 *
 */
public interface ExcelFooterCallback {
	
	/**
	 * Write contents to a file using the supplied {@link Row}. It is not
	 * required to flush the writer inside this method.
	 */
	void writeFooter(Row row);
}
